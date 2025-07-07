package su.nightexpress.nexshop.shop.chest.display.handler;

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
import su.nightexpress.nexshop.shop.chest.display.impl.FakeEntity;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.function.Consumer;

public class ProtocolLibHandler extends DisplayHandler<PacketContainer> {

    private final ProtocolManager protocolManager;

    public ProtocolLibHandler() {
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
    protected List<PacketContainer> getItemPackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull ItemStack item) {
        List<PacketContainer> list = new ArrayList<>();

        PacketContainer spawnPacket = this.createSpawnPacket(this.itemType, entity);
        PacketContainer dataPacket = this.createMetadataPacket(entity.getId(), metadata -> {
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item);
        });

        if (needSpawn) list.add(spawnPacket);
        list.add(dataPacket);

        return list;
    }

    @Override
    @NotNull
    protected List<PacketContainer> getShowcasePackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull ItemStack item) {
        List<PacketContainer> list = new ArrayList<>();

        PacketContainer spawnPacket = this.createSpawnPacket(this.showcaseType, entity);

        PacketContainer dataPacket = this.createMetadataPacket(entity.getId(), metadata -> {
            if (this.showcaseType == EntityType.ARMOR_STAND) {
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

        if (needSpawn) list.add(spawnPacket);
        list.add(dataPacket);

        if (this.showcaseType == EntityType.ARMOR_STAND) {
            PacketContainer armorPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
            List<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> list2 = new ArrayList<>();
            list2.add(new com.comphenix.protocol.wrappers.Pair<>(EnumWrappers.ItemSlot.HEAD, item));

            armorPacket.getIntegers().write(0, entity.getId());
            armorPacket.getSlotStackPairLists().writeSafely(0, list2);

            list.add(armorPacket);
        }

        return list;
    }

    @Override
    @NotNull
    protected List<PacketContainer> getHologramPackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull String textLine) {
        List<PacketContainer> list = new ArrayList<>();

        PacketContainer spawnPacket = createSpawnPacket(this.hologramType, entity);

        Object component = WrappedChatComponent.fromJson(NightMessage.asJson(textLine)).getHandle();
        PacketContainer dataPacket = this.createMetadataPacket(entity.getId(), metadata -> {
            // Armor Stands (legacy)
            if (this.hologramType == EntityType.ARMOR_STAND) {
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
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(24, WrappedDataWatcher.Registry.get(Integer.class)), this.lineWidth);
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(25, WrappedDataWatcher.Registry.get(Integer.class)), this.backgroundColor);
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(26, WrappedDataWatcher.Registry.get(Byte.class)), (byte) this.textOpacity);
                metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(27, WrappedDataWatcher.Registry.get(Byte.class)), this.textBitmask);
            }
        });

        if (needSpawn) list.add(spawnPacket);
        list.add(dataPacket);

        return list;
    }

    @NotNull
    protected PacketContainer createSpawnPacket(@NotNull EntityType entityType, @NotNull FakeEntity entity) {
        Location location = entity.getLocation();

        PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        spawnPacket.getIntegers().write(0, entity.getId());
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
