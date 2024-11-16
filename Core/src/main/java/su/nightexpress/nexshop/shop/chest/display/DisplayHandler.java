package su.nightexpress.nexshop.shop.chest.display;


import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.util.BlockPos;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DisplayHandler<T> extends SimpleManager<ShopPlugin> {

    protected final ChestShopModule         module;
    protected final Map<String, EntityList> entityMap;

    protected boolean useDisplays;
    protected double  lineGap;

    public DisplayHandler(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
        this.entityMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.useDisplays = ChestUtils.canUseDisplayEntities();
        this.lineGap = ChestConfig.DISPLAY_HOLOGRAM_LINE_GAP.get();
    }

    @Override
    protected void onShutdown() {
        this.entityMap.values().forEach(list -> {
            this.broadcastPacket(this.createDestroyPacket(list.getIDs()));
        });
        this.entityMap.clear();
    }

    public void update() {
        this.module.getActiveShops().forEach(this::refresh);
    }

    public void handleQuit(@NotNull Player player) {
        this.entityMap.values().forEach(entityList -> entityList.removePlayer(player));
    }

    protected static class EntityList {

        private final Set<UUID>            createdFor;
        private final List<HologramEntity> holograms;

        private ProductEntity product;
        private ShowcaseEntity showcase;

        public EntityList() {
            this.createdFor = new HashSet<>();
            this.holograms = new ArrayList<>();
        }

        public void addPlayer(@NotNull Player player) {
            this.createdFor.add(player.getUniqueId());
        }

        public void removePlayer(@NotNull Player player) {
            this.createdFor.remove(player.getUniqueId());
        }

        public boolean canSee(@NotNull Player player) {
            return this.createdFor.contains(player.getUniqueId());
        }

        @NotNull
        public List<HologramEntity> getHolograms() {
            return holograms;
        }

        @Nullable
        public ProductEntity getProduct() {
            return this.product;
        }

        @Nullable
        public ShowcaseEntity getShowcase() {
            return this.showcase;
        }

        @NotNull
        public Set<Integer> getIDs() {
            Set<Integer> idList = new HashSet<>();
            this.holograms.forEach(hologramEntity -> idList.add(hologramEntity.entityID));
            if (this.product != null) idList.add(product.entityID);
            if (this.showcase != null) idList.add(showcase.entityID);
            return idList;
        }

        public void clear() {
            this.holograms.clear();
            this.product = null;
            this.showcase = null;
        }
    }

    protected record HologramEntity(int entityID, Location position) {}

    protected record ProductEntity(int entityID) {}

    protected record ShowcaseEntity(int entityID) {}

    public void refresh(@NotNull ChestShop shop) {
        this.createIfAbsent(shop);

        EntityList entityList = this.entityMap.get(shop.getId());
        if (entityList == null) return;

        World world = shop.getWorld();
        if (world == null) return;

        BlockPos blockPos = shop.getBlockPos();
        if (!blockPos.isChunkLoaded(world)) return;

        Location location = blockPos.toLocation(world);
        double distance = ChestConfig.DISPLAY_VISIBLE_DISTANCE.get();

        List<Player> players = world.getPlayers();
        if (players.isEmpty()) return;

        List<String> originText = new ArrayList<>();
        ChestProduct product = shop.getRandomProduct();

        if (ChestConfig.DISPLAY_HOLOGRAM_ENABLED.get() && shop.isHologramEnabled()) {
            Replacer replacer = new Replacer();

            for (TradeType tradeType : TradeType.values()) {
                replacer.replace(Placeholders.GENERIC_PRODUCT_PRICE.apply(tradeType), () -> {
                    return product == null ? "-" : product.getCurrency().format(product.getPricer().getPrice(tradeType));
                });
            }
            replacer.replace(Placeholders.GENERIC_PRODUCT_NAME, () -> product == null ? "" : ItemUtil.getItemName(product.getPreview()));

            originText.addAll(shop.getHologramText(product));
            originText.replaceAll(shop.replacePlaceholders());
            originText.replaceAll(replacer::getReplacedRaw);
        }

        players.forEach(player -> {
            if (player.getLocation().distance(location) > distance) {
                entityList.removePlayer(player);
                this.sendPacket(player, this.createDestroyPacket(entityList.getIDs()));
                return;
            }

            boolean create = !entityList.canSee(player);

            List<String> text = new ArrayList<>(originText);
            if (Plugins.hasPlaceholderAPI()) {
                text = PlaceholderAPI.setPlaceholders(player, text);
            }

            for (int index = 0; index < entityList.getHolograms().size(); index++) {
                String line = text.size() > index ? text.get(index) : null;
                if (line == null || line.isBlank()) continue;

                HologramEntity entity = entityList.getHolograms().get(index);
                this.createHologramPackets(player, entity.entityID, create, entity.position, line);
            }

            ItemStack displayProduct = product == null ? null : product.getPreview();
            if (displayProduct != null) {
                this.createItemPackets(player, entityList.product.entityID, create, shop.getDisplayItemLocation(), displayProduct);
            }

            if (shop.isShowcaseEnabled()) {
                ItemStack showcase = ChestUtils.getCustomShowcaseOrDefault(shop);
                if (showcase != null) {
                    this.createShowcasePackets(player, entityList.showcase.entityID, create, shop.getDisplayShowcaseLocation(), showcase);
                }
            }

            entityList.addPlayer(player);
        });
    }

    private void createIfAbsent(@NotNull ChestShop shop) {
        if (this.entityMap.containsKey(shop.getId())) return;

        EntityList list = this.entityMap.computeIfAbsent(shop.getId(), k -> new EntityList());

        List<String> originText = shop.getDisplayText();
        double currentGap = 0;

        for (int index = 0; index < originText.size(); index++) {
            int entityID = EntityUtil.nextEntityId();
            Location pos = shop.getDisplayTextLocation();

            list.holograms.add(new HologramEntity(entityID, pos.add(0, currentGap, 0)));
            currentGap += this.lineGap;
        }

        list.showcase = new ShowcaseEntity(EntityUtil.nextEntityId());
        list.product = new ProductEntity(EntityUtil.nextEntityId());
    }

    public void create(@NotNull ChestShop shop) {
        this.createIfAbsent(shop);
    }

    public void remove(@NotNull ChestShop shop) {
        EntityList list = this.entityMap.remove(shop.getId());
        if (list == null) return;

        this.broadcastPacket(this.createDestroyPacket(list.getIDs()));
    }

    private void createItemPackets(@NotNull Player player, int entityID, boolean create, @NotNull Location location, @NotNull ItemStack item) {
        EntityType type = EntityType.DROPPED_ITEM;
        this.getItemPackets(entityID, create, type, location, item).forEach(packet -> this.sendPacket(player, packet));
    }

    private void createShowcasePackets(@NotNull Player player, int entityID, boolean create, @NotNull Location location, @NotNull ItemStack item) {
        EntityType type = this.useDisplays ? EntityType.ITEM_DISPLAY : EntityType.ARMOR_STAND;
        this.getShowcasePackets(entityID, create, type, location, item).forEach(packet -> this.sendPacket(player, packet));
    }

    private void createHologramPackets(@NotNull Player player, int entityID, boolean create, @NotNull Location location, @NotNull String textLine) {
        EntityType type = this.useDisplays ? EntityType.TEXT_DISPLAY : EntityType.ARMOR_STAND;

        this.getHologramPackets(entityID, create, type, location, textLine).forEach(packet -> this.sendPacket(player, packet));
    }

    protected abstract void broadcastPacket(@NotNull T packet);

    protected abstract void sendPacket(@NotNull Player player, @NotNull T packet);

    @NotNull
    protected abstract T createSpawnPacket(@NotNull EntityType entityType, @NotNull Location location, int entityID);

    @NotNull
    protected abstract T createDestroyPacket(@NotNull Set<Integer> list);

    @NotNull
    protected abstract List<T> getHologramPackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull String textLine);

    @NotNull
    protected abstract List<T> getShowcasePackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull ItemStack item);

    @NotNull
    protected abstract List<T> getItemPackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull ItemStack item);
}
