package su.nightexpress.excellentshop.integration.packetevents.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.packet.display.DisplayAdapter;
import su.nightexpress.excellentshop.api.packet.display.DisplaySettings;
import su.nightexpress.excellentshop.api.packet.display.FakeEntity;
import su.nightexpress.nightcore.bridge.paper.PaperBridge;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bridge.Software;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.NightMessage;

import java.util.*;
import java.util.function.Consumer;

public class PacketEventsDisplayAdapter implements DisplayAdapter {

    private final PlayerManager   playerManager;
    private final DisplaySettings settings;

    private final EntityType itemType;
    private final EntityType showcaseType;
    private final EntityType hologramType;
    private final byte       textBitmask;
    private final int        backgroundColor;
    private final float hologramScale;

    public PacketEventsDisplayAdapter(@NonNull DisplaySettings settings) {
        this.playerManager = PacketEvents.getAPI().getPlayerManager();
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
        PacketWrapper<?> packet = this.createDestroyPacket(idSet);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> this.playerManager.sendPacket(player, packet));
    }

    @Override
    public void sendDestroyPacket(@NonNull Player player, @NonNull Set<Integer> idSet) {
        this.playerManager.sendPacket(player, this.createDestroyPacket(idSet));
    }

    @Override
    public void sendItemPackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull ItemStack item) {
        PacketWrapper<?> dataPacket = this.createMetadataPacket(entity.getId(), dataList -> {
            dataList.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true)); // no gravity
            dataList.add(new EntityData<>(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(item))); // item
        });

        if (needSpawn) {
            this.playerManager.sendPacket(player, this.createSpawnPacket(this.itemType, entity));
        }

        this.playerManager.sendPacket(player, dataPacket);
    }

    @Override
    public void sendShowcasePackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull ItemStack item) {
        PacketWrapper<?> dataPacket = this.createMetadataPacket(entity.getId(), dataList -> {
            if (this.showcaseType == EntityType.ARMOR_STAND) {
                dataList.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20)); // invisible
                dataList.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, false)); // custom name visible
                dataList.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true)); // no gravity
                dataList.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte) (0x08 | 0x10))); // isSmall noBasePlate

            }
            else {
                dataList.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(0.7f, 0.7f, 0.7f))); // scale
                dataList.add(new EntityData<>(23, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(item))); // slot
                dataList.add(new EntityData<>(24, EntityDataTypes.BYTE, (byte) 5)); // mode HEAD
            }
        });

        if (needSpawn) {
            this.playerManager.sendPacket(player, this.createSpawnPacket(this.showcaseType, entity));
        }

        this.playerManager.sendPacket(player, dataPacket);

        if (this.showcaseType == EntityType.ARMOR_STAND) {
            List<Equipment> equipment = Lists.newList(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(item)));
            PacketWrapper<?> armorPacket = new WrapperPlayServerEntityEquipment(entity.getId(), equipment);
            this.playerManager.sendPacket(player, armorPacket);
        }
    }

    @Override
    public void sendHologramPackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull String textLine) {
        PaperBridge bridge = (PaperBridge) Software.get();
        NightComponent component = NightMessage.parse(textLine);
        Component textComponent = bridge.getTextComponentAdapter().adaptComponent(component);

        PacketWrapper<?> dataPacket = this.createMetadataPacket(entity.getId(), dataList -> {
            dataList.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(this.hologramScale, this.hologramScale, this.hologramScale))); // scale
            dataList.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte) 1)); // billboard
            dataList.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, textComponent)); // text
            dataList.add(new EntityData<>(24, EntityDataTypes.INT, this.settings.getHologramLineWidth()));
            dataList.add(new EntityData<>(25, EntityDataTypes.INT, this.backgroundColor));
            dataList.add(new EntityData<>(26, EntityDataTypes.BYTE, (byte) this.settings.getHologramTextOpacity()));
            dataList.add(new EntityData<>(27, EntityDataTypes.BYTE, this.textBitmask));
        });

        if (needSpawn) {
            this.playerManager.sendPacket(player, this.createSpawnPacket(this.hologramType, entity));
        }

        this.playerManager.sendPacket(player, dataPacket);
    }

    @NonNull
    protected PacketWrapper<?> createSpawnPacket(@NonNull EntityType entityType, @NonNull FakeEntity entity) {
        Location location = entity.getLocation();
        int entityId = entity.getId();

        com.github.retrooper.packetevents.protocol.entity.type.EntityType type = SpigotConversionUtil.fromBukkitEntityType(entityType);
        com.github.retrooper.packetevents.protocol.world.Location loc = SpigotConversionUtil.fromBukkitLocation(location);

        return new WrapperPlayServerSpawnEntity(entityId, UUID.randomUUID(), type, loc, 0F, 0, new Vector3d());
    }

    @NonNull
    private PacketWrapper<?> createDestroyPacket(@NonNull Set<Integer> list) {
        return new WrapperPlayServerDestroyEntities(list.stream().mapToInt(i -> i).toArray());
    }

    @NonNull
    private PacketWrapper<?> createMetadataPacket(int entityID, @NonNull Consumer<List<EntityData<?>>> consumer) {
        List<EntityData<?>> dataList = new ArrayList<>();

        consumer.accept(dataList);

        return new WrapperPlayServerEntityMetadata(entityID, dataList);
    }
}
