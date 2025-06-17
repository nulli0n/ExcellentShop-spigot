package su.nightexpress.nexshop.shop.chest.display;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.display.impl.FakeDisplay;
import su.nightexpress.nexshop.shop.chest.display.impl.FakeEntity;
import su.nightexpress.nexshop.shop.chest.display.impl.FakeType;
import su.nightexpress.nexshop.shop.chest.display.handler.DisplayHandler;
import su.nightexpress.nexshop.shop.chest.display.handler.PacketEventsHandler;
import su.nightexpress.nexshop.shop.chest.display.handler.ProtocolLibHandler;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.impl.Showcase;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisplayManager extends AbstractManager<ShopPlugin> {

    private final ChestShopModule          module;
    private final Map<String, FakeDisplay> displayMap;

    private final double lineGap;
    private final double viewRange;

    private DisplayHandler<?> handler;

    public DisplayManager(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;

        this.displayMap = new ConcurrentHashMap<>();

        this.lineGap = ChestConfig.DISPLAY_HOLOGRAM_LINE_GAP.get();
        this.viewRange = ChestConfig.DISPLAY_VISIBLE_DISTANCE.get();
    }

    @Override
    protected void onLoad() {
        this.handler = this.createHandler();

        if (this.hasHandler()) {
            this.addAsyncTask(this::renderAll, ChestConfig.DISPLAY_UPDATE_INTERVAL.get());
        }
    }

    @Override
    protected void onShutdown() {
        this.displayMap.values().forEach(list -> this.handler.broadcastDestroyPacket(list.getIDs()));
        this.displayMap.clear();
        this.handler = null;
    }

    @Nullable
    private DisplayHandler<?> createHandler() {
        if (Plugins.isInstalled(HookId.PACKET_EVENTS) && Version.isPaper()) {
            return new PacketEventsHandler();
        }
        else if (Plugins.isLoaded(HookId.PROTOCOL_LIB)) {
            return new ProtocolLibHandler();
        }
        return null;
    }

    public boolean hasHandler() {
        return this.handler != null;
    }

    public void renderAll() {
        if (!this.hasHandler()) return;

        this.module.lookup().getAll().forEach(this::render);
    }

    public void removeForViewer(@NotNull Player player) {
        this.displayMap.values().forEach(display -> this.removeForViewer(player, display));
    }

    public void removeForViewer(@NotNull Player player, @NotNull FakeDisplay display) {
        display.removeViewer(player);
        this.handler.sendDestroyPacket(player, display.getIDs());
    }

    public void remove(@NotNull ChestShop shop) {
        FakeDisplay display = this.displayMap.remove(shop.getId());
        if (display == null) return;

        this.handler.broadcastDestroyPacket(display.getIDs());
    }

    public void remake(@NotNull ChestShop shop) {
        this.remove(shop);
        this.render(shop);
    }

    @Nullable
    private FakeDisplay createIfAbsent(@NotNull ChestShop shop) {
        if (!this.hasHandler()) return null;
        if (!shop.isChunkLoaded()) return null;

        String shopId = shop.getId();
        FakeDisplay has = this.displayMap.get(shopId);
        if (has != null) return has;

        FakeDisplay display = new FakeDisplay();

        List<String> originText = shop.getDisplayText();

        Block block = shop.location().getBlock();
        double height = block.getBoundingBox().getHeight();
        double heightGap = 1D - height;

        Location shopLocation = LocationUtil.setCenter2D(block.getLocation());
        Location textLocation = shopLocation.clone().add(0, 1.5D - heightGap, 0);
        Location itemLocation = shopLocation.clone().add(0, height, 0);
        Location showcaseLocation = shopLocation.clone().add(0, -0.35D - heightGap, 0);

        if (ChestUtils.canUseDisplayEntities()) {
            textLocation.add(0, 0.3, 0);
            showcaseLocation.add(0, 1.7, 0);
        }

        // Allocate ID values for our fake entities, so there is no clash with new server entities.

        for (int index = 0; index < originText.size(); index++) {
            Location hologramLocation = textLocation.clone().add(0, this.lineGap * index, 0);
            display.addFakeEntity(FakeType.TEXT, hologramLocation);
        }

        display.addFakeEntity(FakeType.ITEM, itemLocation);
        display.addFakeEntity(FakeType.SHOWCASE, showcaseLocation);

        this.displayMap.put(shopId, display);
        return display;
    }

    public void render(@NotNull ChestShop shop) {
        if (!this.hasHandler()) return;
        if (!shop.isChunkLoaded()) return;

        FakeDisplay display = this.createIfAbsent(shop);
        if (display == null) return;

        World world = shop.location().getWorld();
        Location location = shop.location().getLocation();

        List<Player> players = new ArrayList<>(world.getPlayers());
        players.removeIf(player -> {
            if (player.getLocation().distance(location) <= this.viewRange) return false;

            this.removeForViewer(player, display);
            return true;
        });
        if (players.isEmpty()) return;

        int itemInterval = Math.max(1, ChestConfig.DISPLAY_ITEM_CHANGE_INTERVAL.get());
        int pindex = display.getTickCount() % itemInterval == 0 ? display.nextProductIndex() : display.getProductIndex();
        if (pindex >= shop.getValidProducts().size()) pindex = display.resetProductIndex();

        ChestProduct product = shop.getProductByIndex(pindex);
        List<String> text = shop.getDisplayText(product);
        Showcase showcase = shop.getShowcase();

        players.forEach(player -> {
            boolean needSpawn = !display.isViewer(player);

            if (text != null && !text.isEmpty()) {
                List<String> hologramText = Replacer.create().replacePlaceholderAPI(player).apply(text);
                List<FakeEntity> holograms = display.getFakeEntities(FakeType.TEXT);
                for (int index = 0; index < hologramText.size(); index++) {
                    if (index >= holograms.size()) break;

                    String line = hologramText.get(index);
                    FakeEntity entity = holograms.get(index);
                    this.handler.createHologramPackets(player, entity, needSpawn, line);
                }
            }

            if (product != null) {
                display.getFakeEntities(FakeType.ITEM).forEach(fakeEntity -> {
                    this.handler.createItemPackets(player, fakeEntity, needSpawn, product.getPreview());
                });
            }


            if (showcase != null && product != null) {
                display.getFakeEntities(FakeType.SHOWCASE).forEach(fakeEntity -> {
                    this.handler.createShowcasePackets(player, fakeEntity, needSpawn, showcase.getDisplayItem().getItemStack());
                });
            }

            display.addViewer(player);
        });

        display.tick(); // Count ticks for item carousel
    }
}
