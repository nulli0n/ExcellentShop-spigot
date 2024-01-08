package su.nightexpress.nexshop.shop.chest.display;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
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
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.Placeholders;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DisplayHandler {

    private static final Class<?>      NMS_ENTITY          = Reflex.getClass("net.minecraft.world.entity", "Entity");
    private static final String        ENTITY_COUNTER_NAME = Version.isAtLeast(Version.V1_19_R3) ? "d" : "c";
    public static final  AtomicInteger ENTITY_COUNTER      = (AtomicInteger) Reflex.getFieldValue(NMS_ENTITY, ENTITY_COUNTER_NAME);

    private final ChestShopModule           module;
    private final Map<String, Set<Integer>> entityIdMap;
    private final DisplayUpdateTask         updateTask;

    public DisplayHandler(@NotNull ExcellentShop plugin, @NotNull ChestShopModule module) {
        this.module = module;
        this.entityIdMap = new HashMap<>();
        this.updateTask = new DisplayUpdateTask(plugin, this);
    }

    public void setup() {
        this.updateTask.start();
    }

    public void shutdown() {
        this.updateTask.stop();
        this.entityIdMap.values().forEach(set -> set.forEach(this::destroyEntity));
        this.entityIdMap.clear();
    }

    public void update() {
        this.module.getShops().forEach(this::update);
    }

    public void update(@NotNull ChestShop shop) {
        Set<Integer> origin = this.entityIdMap.get(shop.getId());
        Set<Integer> copy = origin == null ? Collections.emptySet() : new HashSet<>(origin);
        if (origin != null) origin.clear();

        this.create(shop);

        copy.forEach(this::destroyEntity);
    }

    public int nextEntityId() {
        return ENTITY_COUNTER.incrementAndGet();
    }

    public void create(@NotNull ChestShop shop) {
        Location location = shop.getLocation();

        World world = location.getWorld();
        if (world == null || !world.isChunkLoaded(shop.getChunkX(), shop.getChunkZ()) || world.getPlayers().isEmpty()) return;

        Set<Player> players = world.getPlayers().stream()
            .filter(player -> player.getLocation().distance(location) <= ChestConfig.DISPLAY_VISIBLE_DISTANCE.get())
            .collect(Collectors.toSet());
        if (players.isEmpty()) return;


        Set<Integer> entityIds = this.entityIdMap.computeIfAbsent(shop.getId(), k -> new HashSet<>());
        ChestProduct product = shop.getRandomProduct();


        if (ChestConfig.DISPLAY_HOLOGRAM_ENABLED.get()) {
            //List<String> text = new ArrayList<>(shop.getDisplayText());
            List<String> text = shop.getDisplayText();
            PlaceholderMap placeholderMap = new PlaceholderMap(shop.getPlaceholders());
            for (TradeType tradeType : TradeType.values()) {
                placeholderMap.add(Placeholders.GENERIC_PRODUCT_PRICE.apply(tradeType), () -> {
                    return product == null ? "-" : product.getCurrency().format(product.getPricer().getPrice(tradeType));
                });
            }
            placeholderMap.add(Placeholders.GENERIC_PRODUCT_NAME, () -> product == null ? "-" : ItemUtil.getItemName(product.getPreview()));

            text.replaceAll(placeholderMap.replacer());

            Collections.reverse(text);
            Location hologramLocation = shop.getDisplayTextLocation().clone();
            for (String line : text) {
                entityIds.add(this.spawnHologram(players, hologramLocation, line));
                hologramLocation = hologramLocation.add(0, ChestConfig.DISPLAY_HOLOGRAM_LINE_GAP.get(), 0);
            }
        }

        ItemStack displayProduct = product == null ? null : product.getPreview();//shop.getDisplayProduct();
        if (displayProduct != null) {
            entityIds.add(this.spawnItem(players, shop.getDisplayItemLocation(), displayProduct));
        }

        ItemStack showcase = shop.getShowcaseItem();
        if (showcase != null) {
            entityIds.add(this.spawnShowcase(players, shop.getDisplayShowcaseLocation(), showcase));
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

        PacketContainer spawnPacket = this.createSpawnPacket(EntityType.DROPPED_ITEM, location, entityID);
        PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item);
        });

        players.forEach(player -> {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, dataPacket);
        });

        return entityID;
    }

    public int spawnShowcase(@NotNull Set<Player> players, @NotNull Location location, @NotNull ItemStack item) {
        int entityID = this.nextEntityId();

        PacketContainer spawnPacket = this.createSpawnPacket(EntityType.ARMOR_STAND, location, entityID);

        PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); //invis
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), false); //custom name visible
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (/*0x01 | */0x08 | 0x10)); //isSmall, noBasePlate, set Marker
        });

        PacketContainer armorPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        List<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList<>();
        list.add(new com.comphenix.protocol.wrappers.Pair<>(EnumWrappers.ItemSlot.HEAD, item));

        armorPacket.getIntegers().write(0, entityID);
        armorPacket.getSlotStackPairLists().writeSafely(0, list);

        players.forEach(player -> {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, dataPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, armorPacket);
        });

        return entityID;
    }

    public int spawnHologram(@NotNull Set<Player> players, @NotNull Location location, @NotNull String name) {
        int entityID = this.nextEntityId();

        PacketContainer spawnPacket = this.createSpawnPacket(EntityType.ARMOR_STAND, location, entityID);

        players.forEach(player -> {

            String text = name;
            if (EngineUtils.hasPlaceholderAPI()) {
                text = PlaceholderAPI.setPlaceholders(player, text);
            }
            text = Colorizer.apply(text);

            Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(text)[0].getHandle());

            PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); //invis
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), opt); //display name
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true); //custom name visible
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x08 | 0x10)); //isSmall, noBasePlate, set Marker
            });

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, dataPacket);
        });

        return entityID;
    }

    public void destroyEntity(int... ids) {
        PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, IntStream.of(ids).boxed().toList());
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
