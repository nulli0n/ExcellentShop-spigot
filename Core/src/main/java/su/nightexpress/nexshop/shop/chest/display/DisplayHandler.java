package su.nightexpress.nexshop.shop.chest.display;


import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.Placeholders;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.util.BlockPos;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.*;
import java.util.stream.Collectors;

public abstract class DisplayHandler<T> extends SimpleManager<ShopPlugin> {

    protected final ChestShopModule           module;
    protected final Map<String, Set<Integer>> entityIdMap;

    protected boolean useDisplays;
    protected double  lineGap;

    public DisplayHandler(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
        this.entityIdMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.useDisplays = ChestUtils.canUseDisplayEntities();
        this.lineGap = ChestConfig.DISPLAY_HOLOGRAM_LINE_GAP.get();
    }

    @Override
    protected void onShutdown() {
        this.entityIdMap.values().forEach(this::destroyEntity);
        this.entityIdMap.clear();
    }

    protected record PacketBundle<T>(Player player, List<T> containers) {

        public void add(T container) {
            this.containers.add(container);
        }
    }

    protected enum Action {
        REMOVE, ADD, REFRESH
    }

    public void update() {
        List<PacketBundle<T>> bundles = new ArrayList<>();
        this.module.getActiveShops().forEach(shop -> bundles.addAll(this.getBundles(shop, Action.REFRESH)));
        this.sendAction(Action.REFRESH, bundles);
    }

    public void update(@NotNull ChestShop shop) {
        this.sendAction(Action.REFRESH, this.getBundles(shop, Action.REFRESH));
    }

    public void create(@NotNull ChestShop shop) {
        this.sendAction(Action.ADD, this.getBundles(shop, Action.ADD));
    }

    public void remove(@NotNull ChestShop shop) {
        this.sendAction(Action.REMOVE, this.getBundles(shop, Action.REMOVE));
    }

    @NotNull
    private List<PacketBundle<T>> getBundles(@NotNull ChestShop shop, @NotNull Action action) {
        List<PacketBundle<T>> bundles = new ArrayList<>();

        World world = shop.getWorld();
        if (world == null) return bundles;

        BlockPos blockPos = shop.getBlockPos();
        if (!blockPos.isChunkLoaded(world)) return bundles;

        Location location = blockPos.toLocation(world);
        double distance = ChestConfig.DISPLAY_VISIBLE_DISTANCE.get();

        Set<Player> players = world.getPlayers().stream()
            .filter(player -> player.getLocation().distance(location) <= distance)
            .collect(Collectors.toSet());
        if (players.isEmpty()) return bundles;

        Set<Integer> idList = this.entityIdMap.computeIfAbsent(shop.getId(), k -> new HashSet<>());
        List<String> originText = new ArrayList<>();
        Set<Integer> previousIds = new HashSet<>();
        ChestProduct product = shop.getRandomProduct();

        if (action == Action.REMOVE || action == Action.REFRESH) {
            previousIds.addAll(idList);
            idList.clear();
        }

        if (ChestConfig.DISPLAY_HOLOGRAM_ENABLED.get() && shop.isHologramEnabled() && (action == Action.ADD || action == Action.REFRESH)) {
            PlaceholderMap placeholders = new PlaceholderMap(shop.getPlaceholders());
            for (TradeType tradeType : TradeType.values()) {
                placeholders.add(Placeholders.GENERIC_PRODUCT_PRICE.apply(tradeType), () -> {
                    return product == null ? "-" : product.getCurrency().format(product.getPricer().getPrice(tradeType));
                });
            }
            placeholders.add(Placeholders.GENERIC_PRODUCT_NAME, () -> product == null ? "-" : ItemUtil.getItemName(product.getPreview()));

            originText.addAll(shop.getHologramText(product));
            originText.replaceAll(placeholders.replacer());
        }

        players.forEach(player -> {
            PacketBundle<T> bundle = new PacketBundle<>(player, new ArrayList<>());

            if (action == Action.ADD || action == Action.REFRESH) {
                List<String> text = new ArrayList<>(originText);
                if (Plugins.hasPlaceholderAPI()) {
                    text = PlaceholderAPI.setPlaceholders(player, text);
                }

                Location clone = shop.getDisplayTextLocation(); // Cloned to keep the location in list unmodified for next players.
                for (String line : text) {
                    bundle.containers.addAll(this.createHologramPackets(idList, clone, line));
                }

                ItemStack displayProduct = product == null ? null : product.getPreview();
                if (displayProduct != null) {
                    bundle.containers.addAll(this.createItemPackets(idList, shop.getDisplayItemLocation(), displayProduct));
                }

                if (shop.isShowcaseEnabled()) {
                    ItemStack showcase = ChestUtils.getCustomShowcaseOrDefault(shop);
                    if (showcase != null) {
                        bundle.containers.addAll(this.createShowcasePackets(idList, shop.getDisplayShowcaseLocation(), showcase));
                    }
                }
            }

            if (action == Action.REMOVE || action == Action.REFRESH) {
                bundle.add(this.createDestroyPacket(previousIds));
            }

            bundles.add(bundle);
        });

        return bundles;
    }

    private void sendAction(@NotNull Action action, @NotNull List<PacketBundle<T>> bundles) {
        // Synchronize all the packets sending with the main thread to reduce hologram flicker.
        if (action == Action.REFRESH && !this.plugin.getServer().isPrimaryThread()) {
            this.plugin.runTask(task -> this.sendBundles(bundles));
        }
        // Otherwise do it in the current thread.
        else {
            this.sendBundles(bundles);
        }
    }

    private void sendBundles(@NotNull List<PacketBundle<T>> bundles) {
        bundles.forEach(bundle -> bundle.containers().forEach(container -> {
            this.sendPacket(bundle.player, container);
        }));
    }

    @NotNull
    private List<T> createItemPackets(@NotNull Set<Integer> idList, @NotNull Location location, @NotNull ItemStack item) {
        int entityID = EntityUtil.nextEntityId();
        EntityType type = EntityType.DROPPED_ITEM;
        List<T> list = this.getItemPackets(entityID, type, location, item);

        idList.add(entityID);

        return list;
    }

    @NotNull
    private List<T> createShowcasePackets(@NotNull Set<Integer> idList, @NotNull Location location, @NotNull ItemStack item) {
        int entityID = EntityUtil.nextEntityId();
        EntityType type = this.useDisplays ? EntityType.ITEM_DISPLAY : EntityType.ARMOR_STAND;
        List<T> list = this.getShowcasePackets(entityID, type, location, item);

        idList.add(entityID);
        return list;
    }

    @NotNull
    private List<T> createHologramPackets(@NotNull Set<Integer> idList, @NotNull Location location, @NotNull String textLine) {
        int entityID = EntityUtil.nextEntityId();
        EntityType type = this.useDisplays ? EntityType.TEXT_DISPLAY : EntityType.ARMOR_STAND;

        List<T> list = new ArrayList<>(this.getHologramPackets(entityID, type, location, textLine));

        location.add(0, this.lineGap, 0);
        idList.add(entityID);

        return list;
    }

    private void destroyEntity(@NotNull Set<Integer> idList) {
        this.broadcastPacket(this.createDestroyPacket(idList));
    }

    protected abstract void broadcastPacket(@NotNull T packet);

    protected abstract void sendPacket(@NotNull Player player, @NotNull T packet);

    @NotNull
    protected abstract T createSpawnPacket(@NotNull EntityType entityType, @NotNull Location location, int entityID);

    @NotNull
    protected abstract T createDestroyPacket(@NotNull Set<Integer> list);

    @NotNull
    protected abstract List<T> getHologramPackets(int entityID, @NotNull EntityType type, @NotNull Location location, @NotNull String textLine);

    @NotNull
    protected abstract List<T> getShowcasePackets(int entityID, @NotNull EntityType type, @NotNull Location location, @NotNull ItemStack item);

    @NotNull
    protected abstract List<T> getItemPackets(int entityID, @NotNull EntityType type, @NotNull Location location, @NotNull ItemStack item);
}
