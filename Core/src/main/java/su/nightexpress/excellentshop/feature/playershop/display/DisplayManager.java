package su.nightexpress.excellentshop.feature.playershop.display;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.packet.display.*;
import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestProduct;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.excellentshop.feature.playershop.impl.Showcase;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisplayManager {

    private final ChestShopModule          module;
    private final DisplayAdapter           adapter;
    private final Map<String, FakeDisplay> displayMap;

    private final double viewRange;
    private final int    itemChangeInterval;

    public DisplayManager(@NonNull ChestShopModule module, @NonNull DisplayAdapter adapter) {
        this.module = module;
        this.adapter = adapter;
        this.displayMap = new ConcurrentHashMap<>();

        this.viewRange = adapter.getSettings().getVisibleDistance();
        this.itemChangeInterval = adapter.getSettings().getItemChangeInterval();
    }

    public void clear() {
        this.displayMap.values().forEach(list -> this.adapter.broadcastDestroyPacket(list.getIDs()));
        this.displayMap.clear();
    }

    public void removeForViewer(@NonNull Player player) {
        this.displayMap.values().forEach(display -> this.removeForViewer(player, display));
    }

    public void removeForViewer(@NonNull Player player, @NonNull FakeDisplay display) {
        display.removeViewer(player);
        this.adapter.sendDestroyPacket(player, display.getIDs());
    }

    public void remove(@NonNull ChestShop shop) {
        FakeDisplay display = this.displayMap.remove(shop.getId());
        if (display == null) return;

        this.adapter.broadcastDestroyPacket(display.getIDs());
    }

    public void remake(@NonNull ChestShop shop) {
        this.remove(shop);
        this.render(shop);
    }

    @Nullable
    private FakeDisplay createIfAbsent(@NonNull ChestShop shop) {
        if (!shop.isAccessible()) return null;

        String shopId = shop.getId();
        FakeDisplay has = this.displayMap.get(shopId);
        if (has != null) return has;

        FakeDisplay display = new FakeDisplay();

        Block block = shop.getBlock();
        double height = block.getBoundingBox().getHeight();
        double heightGap = 1D - height;
        double textHeigh = shop.hasProducts() ? 1.5D : 1D;

        Location shopLocation = LocationUtil.setCenter2D(block.getLocation());
        Location textLocation = shopLocation.clone().add(0, (textHeigh - heightGap) + 0.3, 0);
        Location itemLocation = shopLocation.clone().add(0, height, 0);
        Location showcaseLocation = shopLocation.clone().add(0, (-0.35D - heightGap) + 1.7, 0);

        if (this.adapter.getSettings().isHologramEnabled()) {
            display.addFakeEntity(FakeType.TEXT, textLocation);
        }
        display.addFakeEntity(FakeType.ITEM, itemLocation);
        display.addFakeEntity(FakeType.SHOWCASE, showcaseLocation);

        this.displayMap.put(shopId, display);
        return display;
    }

    public void render(@NonNull ChestShop shop) {
        if (!shop.isAccessible()) return;

        FakeDisplay display = this.createIfAbsent(shop);
        if (display == null) return;

        Block block = shop.getBlock();
        World world = block.getWorld();
        Location location = block.getLocation();

        List<Player> players = new ArrayList<>(world.getPlayers());
        players.removeIf(player -> {
            if (player.getLocation().distance(location) <= this.viewRange) return false;

            this.removeForViewer(player, display);
            return true;
        });
        if (players.isEmpty()) return;

        int itemInterval = Math.max(1, this.itemChangeInterval);
        int pindex = display.getTickCount() % itemInterval == 0 ? display.nextProductIndex() : display.getProductIndex();
        if (pindex >= shop.getValidProducts().size()) pindex = display.resetProductIndex();

        ChestProduct product = shop.getProductByIndex(pindex);
        Showcase showcase = shop.getShowcase();

        players.forEach(player -> {
            boolean needSpawn = !display.isViewer(player);

            if (this.adapter.getSettings().isHologramEnabled() && shop.isHologramEnabled()) {
                display.fakeEntity(FakeType.TEXT).ifPresent(entity -> {
                    String text = this.getDisplayText(player, shop, product);
                    this.adapter.sendHologramPackets(player, entity, needSpawn, text);
                });
            }

            if (product != null) {
                display.fakeEntity(FakeType.ITEM).ifPresent(entity -> {
                    this.adapter.sendItemPackets(player, entity, needSpawn, product.getPreview());
                });
            }

            if (showcase != null && product != null) {
                display.fakeEntity(FakeType.SHOWCASE).ifPresent(entity -> {
                    this.adapter.sendShowcasePackets(player, entity, needSpawn, showcase.getDisplayItem().getItemStack());
                });
            }

            display.addViewer(player);
        });

        display.tick(); // Count ticks for item carousel
    }

    @NonNull
    private String getDisplayText(@NonNull Player player, @NonNull ChestShop shop, @Nullable ChestProduct product) {
        List<String> text;
        DisplaySettings settings = this.adapter.getSettings();

        if (!shop.hasProducts()) {
            text = settings.getUnconfiguredShopHologram();
        }
        else if (shop.isRentable() && !shop.isRented()) {
            text = settings.getRentableShopHologram();
        }
        else {
            text = shop.isAdminShop() ? settings.getAdminShopHologram() : settings.getPlayerShopHologram();
        }

        PlaceholderContext.Builder builder = PlaceholderContext.builder()
            .with(shop.placeholders())
            .andThen(CommonPlaceholders.forPlaceholderAPI(player));

        if (product != null) {
            builder.with(product.placeholders());
            text = this.module.formatProductLore(product, text, player);
        }

        return builder.build().apply(String.join(TagWrappers.BR, text));
    }
}
