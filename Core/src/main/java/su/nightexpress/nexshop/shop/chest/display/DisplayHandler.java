package su.nightexpress.nexshop.shop.chest.display;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
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
import java.util.stream.IntStream;

public class DisplayHandler extends SimpleManager<ShopPlugin> {

    private final ChestShopModule           module;
    private final Map<String, Set<Integer>> entityIdMap;
    private final boolean                   useDisplays;

    public DisplayHandler(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
        this.entityIdMap = new HashMap<>();
        this.useDisplays = ChestUtils.canUseDisplayEntities();
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onShutdown() {
        this.entityIdMap.values().forEach(this::destroyEntity);
        this.entityIdMap.clear();
    }

    public void update() {
        this.module.getActiveShops().forEach(this::update);
    }

    public void update(@NotNull ChestShop shop) {
        Set<Integer> origin = this.entityIdMap.get(shop.getId());
        Set<Integer> copy = origin == null ? Collections.emptySet() : new HashSet<>(origin);
        if (origin != null) origin.clear();

        this.create(shop);
        this.destroyEntity(copy);
    }

    public int nextEntityId() {
        return EntityUtil.nextEntityId();
    }

    public void create(@NotNull ChestShop shop) {
        World world = shop.getWorld();
        if (world == null) return; // Can be null because async display task, may get inactive shops of just unloaded world.

        BlockPos blockPos = shop.getBlockPos();
        if (!blockPos.isChunkLoaded(world)) return;

        Location location = blockPos.toLocation(world);
        double distance = ChestConfig.DISPLAY_VISIBLE_DISTANCE.get();

        Set<Player> players = world.getPlayers().stream()
            .filter(player -> player.getLocation().distance(location) <= distance)
            .collect(Collectors.toSet());
        if (players.isEmpty()) return;


        Set<Integer> entityIds = this.entityIdMap.computeIfAbsent(shop.getId(), k -> new HashSet<>());
        ChestProduct product = shop.getRandomProduct();

        if (ChestConfig.DISPLAY_HOLOGRAM_ENABLED.get() && shop.isHologramEnabled()) {
            PlaceholderMap placeholderMap = new PlaceholderMap(shop.getPlaceholders());
            for (TradeType tradeType : TradeType.values()) {
                placeholderMap.add(Placeholders.GENERIC_PRODUCT_PRICE.apply(tradeType), () -> {
                    return product == null ? "-" : product.getCurrency().format(product.getPricer().getPrice(tradeType));
                });
            }
            placeholderMap.add(Placeholders.GENERIC_PRODUCT_NAME, () -> product == null ? "-" : ItemUtil.getItemName(product.getPreview()));

            List<String> text = new ArrayList<>();
            for (String line : shop.getDisplayText()) {
                text.add(0, placeholderMap.replacer().apply(line));
            }

            Location hologramLocation = shop.getDisplayTextLocation();
            for (String line : text) {
                entityIds.add(this.spawnHologram(players, hologramLocation, line));
                hologramLocation = hologramLocation.add(0, ChestConfig.DISPLAY_HOLOGRAM_LINE_GAP.get(), 0);
            }
        }

        ItemStack displayProduct = product == null ? null : product.getPreview();//shop.getDisplayProduct();
        if (displayProduct != null) {
            entityIds.add(this.spawnItem(players, shop.getDisplayItemLocation(), displayProduct));
        }

        if (shop.isShowcaseEnabled()) {
            ItemStack showcase = ChestUtils.getCustomShowcaseOrDefault(shop);
            if (showcase != null) {
                entityIds.add(this.spawnShowcase(players, shop.getDisplayShowcaseLocation(), showcase));
            }
        }
    }

    public void remove(@NotNull ChestShop shop) {
        this.remove(shop.getId());
    }

    public void remove(@NotNull String id) {
        Set<Integer> set = this.entityIdMap.remove(id);
        if (set == null) return;

        set.forEach(this::destroyEntity);
    }

    public int spawnItem(@NotNull Set<Player> players, @NotNull Location location, @NotNull ItemStack item) {
        int entityID = this.nextEntityId();
        EntityType type = /*this.useDisplays ? EntityType.ITEM_DISPLAY : */EntityType.DROPPED_ITEM;

        PacketContainer spawnPacket = this.createSpawnPacket(type, location, entityID);
        PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
            //if (type == EntityType.DROPPED_ITEM) {
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item);
            /*}
            else {
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 1); // billboard
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(23, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item); // billboard
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(24, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 7); // billboard
            }*/
        });

        players.forEach(player -> {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, dataPacket);
        });

        return entityID;
    }

    public int spawnShowcase(@NotNull Set<Player> players, @NotNull Location location, @NotNull ItemStack item) {
        int entityID = this.nextEntityId();
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
            List<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList<>();
            list.add(new com.comphenix.protocol.wrappers.Pair<>(EnumWrappers.ItemSlot.HEAD, item));

            armorPacket.getIntegers().write(0, entityID);
            armorPacket.getSlotStackPairLists().writeSafely(0, list);
        }

        players.forEach(player -> {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, dataPacket);
            if (type == EntityType.ARMOR_STAND) ProtocolLibrary.getProtocolManager().sendServerPacket(player, armorPacket);
        });

        return entityID;
    }

    public int spawnHologram(@NotNull Set<Player> players, @NotNull Location location, @NotNull String text) {
        int entityID = this.nextEntityId();
        EntityType type = this.useDisplays ? EntityType.TEXT_DISPLAY : EntityType.ARMOR_STAND;

        PacketContainer spawnPacket = this.createSpawnPacket(type, location, entityID);

        players.forEach(player -> {
            String userText = text;
            if (Plugins.hasPlaceholderAPI()) {
                userText = PlaceholderAPI.setPlaceholders(player, userText);
            }
            String translated = NightMessage.asLegacy(userText);

            PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
                Object component = WrappedChatComponent.fromChatMessage(translated)[0].getHandle();

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

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, dataPacket);
        });

        return entityID;
    }

    public void destroyEntity(int... ids) {
        /*PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, IntStream.of(ids).boxed().toList());
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(destroyPacket);*/

        this.destroyEntity(IntStream.of(ids).boxed().toList());
    }

    public void destroyEntity(@NotNull Collection<Integer> ids) {
        PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, new ArrayList<>(ids));
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(destroyPacket);
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
}
