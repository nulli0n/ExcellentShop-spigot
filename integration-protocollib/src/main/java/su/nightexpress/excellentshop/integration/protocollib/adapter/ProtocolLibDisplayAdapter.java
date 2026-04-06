package su.nightexpress.excellentshop.integration.protocollib.adapter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.packet.display.DisplayAdapter;
import su.nightexpress.excellentshop.api.packet.display.DisplaySettings;
import su.nightexpress.excellentshop.api.packet.display.FakeEntity;
import su.nightexpress.nightcore.util.text.night.NightMessage;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

public class ProtocolLibDisplayAdapter implements DisplayAdapter {

    private final ProtocolManager protocolManager;
    private final DisplaySettings settings;

    private final EntityType itemType;
    private final EntityType showcaseType;
    private final EntityType hologramType;
    private final byte       textBitmask;
    private final int        backgroundColor;
    private final float      hologramScale;

    public ProtocolLibDisplayAdapter(@NonNull DisplaySettings settings) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.settings = settings;

        int[] argb = settings.getHologramBackgroundColor();
        this.backgroundColor = toARGB(argb[0], argb[1], argb[2], argb[3]);

        this.textBitmask = (byte) ((settings.isHologramShadow() ? 0x01 : 0) | (settings.isHologramSeeThrough() ? 0x02 : 0));
        this.hologramScale = (float) settings.getHologramScale();

        this.itemType = EntityType.ITEM;
        this.showcaseType = EntityType.ITEM_DISPLAY;
        this.hologramType = EntityType.TEXT_DISPLAY;
    }

    private static int toARGB(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    @Override
    @NonNull
    public DisplaySettings getSettings() {
        return this.settings;
    }

    @Override
    public void broadcastDestroyPacket(@NonNull Set<Integer> idSet) {
        this.protocolManager.broadcastServerPacket(this.createDestroyPacket(idSet));
    }

    @Override
    public void sendDestroyPacket(@NonNull Player player, @NonNull Set<Integer> idSet) {
        this.protocolManager.sendServerPacket(player, this.createDestroyPacket(idSet));
    }

    @Override
    public void sendItemPackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull ItemStack item) {
        PacketContainer dataPacket = this.createMetadataPacket(entity.getId(), metadata -> {
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get((Type) Boolean.class)), true); //no gravity
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item);
        });

        if (needSpawn) {
            this.protocolManager.sendServerPacket(player, this.createSpawnPacket(this.itemType, entity));
        }
        this.protocolManager.sendServerPacket(player, dataPacket);
    }

    @Override
    public void sendShowcasePackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull ItemStack item) {
        PacketContainer dataPacket = this.createMetadataPacket(entity.getId(), metadata -> {
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(12, WrappedDataWatcher.Registry.get((Type) Vector3f.class)), new Vector3f(0.7f, 0.7f, 0.7f)); // scale
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(23, WrappedDataWatcher.Registry.getItemStackSerializer(false)), item); // slot
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(24, WrappedDataWatcher.Registry.get((Type) Byte.class)), (byte) 5); // mode HEAD
        });

        if (needSpawn) {
            this.protocolManager.sendServerPacket(player, this.createSpawnPacket(this.showcaseType, entity));
        }
        this.protocolManager.sendServerPacket(player, dataPacket);

        if (this.showcaseType == EntityType.ARMOR_STAND) {
            PacketContainer armorPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
            List<Pair<EnumWrappers.ItemSlot, ItemStack>> list2 = new ArrayList<>();
            list2.add(new com.comphenix.protocol.wrappers.Pair<>(EnumWrappers.ItemSlot.HEAD, item));

            armorPacket.getIntegers().write(0, entity.getId());
            armorPacket.getSlotStackPairLists().writeSafely(0, list2);

            this.protocolManager.sendServerPacket(player, armorPacket);
        }
    }

    @Override
    public void sendHologramPackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull String textLine) {
        Object component = WrappedChatComponent.fromJson(NightMessage.asJson(textLine)).getHandle();
        PacketContainer dataPacket = this.createMetadataPacket(entity.getId(), metadata -> {
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(12, WrappedDataWatcher.Registry.get((Type) Vector3f.class)), new Vector3f(this.hologramScale, this.hologramScale, this.hologramScale)); // scale
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get((Type) Byte.class)), (byte) 1); // billboard
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(23, WrappedDataWatcher.Registry.getChatComponentSerializer()), component);
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(24, WrappedDataWatcher.Registry.get((Type) Integer.class)), this.settings.getHologramLineWidth());
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(25, WrappedDataWatcher.Registry.get((Type) Integer.class)), this.backgroundColor);
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(26, WrappedDataWatcher.Registry.get((Type) Byte.class)), (byte) this.settings.getHologramTextOpacity());
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(27, WrappedDataWatcher.Registry.get((Type) Byte.class)), this.textBitmask);

        });

        if (needSpawn) {
            this.protocolManager.sendServerPacket(player, this.createSpawnPacket(this.hologramType, entity));
        }

        this.protocolManager.sendServerPacket(player, dataPacket);
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
