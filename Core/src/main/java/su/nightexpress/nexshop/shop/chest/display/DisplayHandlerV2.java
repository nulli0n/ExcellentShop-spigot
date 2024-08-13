package su.nightexpress.nexshop.shop.chest.display;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
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
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DisplayHandlerV2 extends SimpleManager<ShopPlugin> {

    private final ChestShopModule module;
    private final ProtocolManager protocolManager;
    private final Map<String, Set<Integer>> entityIdMap;

    private boolean useDisplays;
    private double  lineGap;

    public DisplayHandlerV2(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
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

    private record PacketBundle(Player player, List<PacketContainer> containers) {

        public void add(PacketContainer container) {
            this.containers.add(container);
        }
    }

    private enum Action {
        REMOVE, ADD, REFRESH
    }

    public void update() {
        List<PacketBundle> bundles = new ArrayList<>();
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
    private List<PacketBundle> getBundles(@NotNull ChestShop shop, @NotNull Action action) {
        List<PacketBundle> bundles = new ArrayList<>();

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
            placeholders.add(Placeholders.GENERIC_PRODUCT_NAME, () -> product == null ? "-" : ItemUtil.getItemName(product.getIcon()));

            originText.addAll(shop.getHologramText(product));
            originText.replaceAll(placeholders.replacer());
        }

        players.forEach(player -> {
            PacketBundle bundle = new PacketBundle(player, new ArrayList<>());

            if (action == Action.ADD || action == Action.REFRESH) {
                List<String> text = new ArrayList<>(originText);
                if (Plugins.hasPlaceholderAPI()) {
                    text = PlaceholderAPI.setPlaceholders(player, text);
                }

                Location clone = shop.getDisplayTextLocation(); // Cloned to keep the location in list unmodified for next players.
                for (String line : text) {
                    bundle.containers.addAll(this.createHologramPackets(idList, clone, line));
                }

                ItemStack displayProduct = product == null ? null : product.getIcon();
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

    private void sendAction(@NotNull Action action, @NotNull List<PacketBundle> bundles) {
        // Synchronize all the packets sending with the main thread to reduce hologram flicker.
        if (action == Action.REFRESH && !this.plugin.getServer().isPrimaryThread()) {
            this.plugin.runTask(task -> this.sendBundles(bundles));
        }
        // Otherwise do it in the current thread.
        else {
            this.sendBundles(bundles);
        }
    }

    private void sendBundles(@NotNull List<PacketBundle> bundles) {
        bundles.forEach(bundle -> bundle.containers().forEach(container -> {
            this.protocolManager.sendServerPacket(bundle.player, container);
        }));
    }

    public List<PacketContainer> createItemPackets(@NotNull Set<Integer> idList, @NotNull Location location, @NotNull ItemStack item) {
        List<PacketContainer> list = new ArrayList<>();
        int entityID = EntityUtil.nextEntityId();
        EntityType type = EntityType.DROPPED_ITEM;

        PacketContainer spawnPacket = this.createSpawnPacket(type, location, entityID);
        PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item);
        });

        list.add(spawnPacket);
        list.add(dataPacket);
        idList.add(entityID);

        return list;
    }

    public List<PacketContainer> createShowcasePackets(@NotNull Set<Integer> idList, @NotNull Location location, @NotNull ItemStack item) {
        List<PacketContainer> list = new ArrayList<>();
        int entityID = EntityUtil.nextEntityId();
        EntityType type = this.useDisplays ? EntityType.ITEM_DISPLAY : EntityType.ARMOR_STAND;

        PacketContainer spawnPacket = this.createSpawnPacket(type, location, entityID);

        PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
            if (type == EntityType.ARMOR_STAND) {
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); //invis
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), false); //custom name visible
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (/*0x01 | */0x08 | 0x10)); //isSmall, noBasePlate, set Marker

            }
            else {
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(12, WrappedDataWatcher.Registry.get(Vector3f.class)), new Vector3f(0.7f, 0.7f, 0.7f)); // scale
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(23, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item); // slot
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(24, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 5); // mode HEAD
            }
        });

        PacketContainer armorPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        if (type == EntityType.ARMOR_STAND) {
            List<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> list2 = new ArrayList<>();
            list2.add(new com.comphenix.protocol.wrappers.Pair<>(EnumWrappers.ItemSlot.HEAD, item));

            armorPacket.getIntegers().write(0, entityID);
            armorPacket.getSlotStackPairLists().writeSafely(0, list2);
        }

        list.add(spawnPacket);
        list.add(dataPacket);
        if (type == EntityType.ARMOR_STAND) list.add(armorPacket);

        idList.add(entityID);

        return list;
    }

    @NotNull
    private List<PacketContainer> createHologramPackets(@NotNull Set<Integer> idList, @NotNull Location location, @NotNull String textLine) {
        List<PacketContainer> list = new ArrayList<>();
        int entityID = EntityUtil.nextEntityId();
        EntityType type = this.useDisplays ? EntityType.TEXT_DISPLAY : EntityType.ARMOR_STAND;

        PacketContainer spawnPacket = createSpawnPacket(type, location, entityID);

        Object component = WrappedChatComponent.fromJson(NightMessage.asJson(textLine)).getHandle();
        PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
            // Armor Stands (legacy)
            if (type == EntityType.ARMOR_STAND) {
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); //invis
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), Optional.of(component)); //display name
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true); //custom name visible
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x08 | 0x10)); //isSmall, noBasePlate, set Marker
            }
            // Displays (modern)
            else {
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 1); // billboard
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(23, WrappedDataWatcher.Registry.getChatComponentSerializer()), component);
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(27, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x1); // shadow
            }
        });

        list.add(spawnPacket);
        list.add(dataPacket);

        location.add(0, this.lineGap, 0);
        idList.add(entityID);

        return list;
    }

    @NotNull
    private PacketContainer createSpawnPacket(@NotNull EntityType entityType, @NotNull Location location, int entityID) {
        PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        spawnPacket.getIntegers().write(0, entityID);
        spawnPacket.getUUIDs().write(0, UUID.randomUUID());
        spawnPacket.getEntityTypeModifier().write(0, entityType);
        spawnPacket.getDoubles().write(0, location.getX());
        spawnPacket.getDoubles().write(1, location.getY());
        spawnPacket.getDoubles().write(2, location.getZ());
        return spawnPacket;
    }

    @NotNull
    private PacketContainer createMetadataPacket(int entityID, @NotNull Consumer<WrappedDataWatcher> consumer) {
        PacketContainer dataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        WrappedDataWatcher metadata = new WrappedDataWatcher();

        consumer.accept(metadata);

        List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
        metadata.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
            WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
            wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
        });

        dataPacket.getDataValueCollectionModifier().write(0, wrappedDataValueList);
        dataPacket.getIntegers().write(0, entityID);

        return dataPacket;
    }

    private void destroyEntity(@NotNull Set<Integer> idList) {
        this.protocolManager.broadcastServerPacket(this.createDestroyPacket(idList));
    }

//    @NotNull
//    private PacketContainer createDestroyPacket(@NotNull Set<Integer> idList) {
//        return this.createDestroyPacket(idList);
//    }

    @NotNull
    private PacketContainer createDestroyPacket(@NotNull Set<Integer> list) {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        container.getIntLists().write(0, new ArrayList<>(list));

        return container;
    }
}
