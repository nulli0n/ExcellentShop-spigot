package su.nightexpress.nexshop.shop.chest.display;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.function.Consumer;

public class ProtocolLibHandler extends DisplayHandler<PacketContainer> {

    private final ProtocolManager protocolManager;

    public ProtocolLibHandler(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, module);
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    protected void sendPacket(@NotNull Player player, @NotNull PacketContainer packet) {
        this.protocolManager.sendServerPacket(player, packet);
    }

    @Override
    protected void broadcastPacket(@NotNull PacketContainer packet) {
        this.protocolManager.broadcastServerPacket(packet);
    }

    @Override
    @NotNull
    protected List<PacketContainer> getItemPackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull ItemStack item) {
        List<PacketContainer> list = new ArrayList<>();

        PacketContainer spawnPacket = this.createSpawnPacket(type, location, entityID);
        PacketContainer dataPacket = this.createMetadataPacket(entityID, metadata -> {
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item);
        });

        if (create) list.add(spawnPacket);
        list.add(dataPacket);

        return list;
    }

    @Override
    @NotNull
    protected List<PacketContainer> getShowcasePackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull ItemStack item) {
        List<PacketContainer> list = new ArrayList<>();

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

        if (create) list.add(spawnPacket);
        list.add(dataPacket);

        if (type == EntityType.ARMOR_STAND) {
            PacketContainer armorPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
            List<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> list2 = new ArrayList<>();
            list2.add(new com.comphenix.protocol.wrappers.Pair<>(EnumWrappers.ItemSlot.HEAD, item));

            armorPacket.getIntegers().write(0, entityID);
            armorPacket.getSlotStackPairLists().writeSafely(0, list2);

            list.add(armorPacket);
        }

        return list;
    }

    @Override
    @NotNull
    protected List<PacketContainer> getHologramPackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull String textLine) {
        List<PacketContainer> list = new ArrayList<>();

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

        if (create) list.add(spawnPacket);
        list.add(dataPacket);

        return list;
    }

    @NotNull
    protected PacketContainer createSpawnPacket(@NotNull EntityType entityType, @NotNull Location location, int entityID) {
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

    @NotNull
    protected PacketContainer createDestroyPacket(@NotNull Set<Integer> list) {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        container.getIntLists().write(0, new ArrayList<>(list));

        return container;
    }
}
