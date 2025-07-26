package su.nightexpress.nexshop.shop.chest.display.handler;

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
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.display.impl.FakeEntity;
import su.nightexpress.nightcore.bridge.paper.PaperBridge;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bridge.Software;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.NightMessage;

import java.util.*;
import java.util.function.Consumer;

public class PacketEventsHandler extends DisplayHandler<PacketWrapper<?>> {

    private final PlayerManager playerManager;

    public PacketEventsHandler() {
        this.playerManager = PacketEvents.getAPI().getPlayerManager();
    }

    @Override
    protected void sendPacket(@NotNull Player player, @NotNull PacketWrapper<?> packet) {
        this.playerManager.sendPacket(player, packet);
    }

    @Override
    protected void broadcastPacket(@NotNull PacketWrapper<?> packet) {
        Bukkit.getServer().getOnlinePlayers().forEach(player -> this.playerManager.sendPacket(player, packet));
    }


    @Override
    @NotNull
    protected List<PacketWrapper<?>> getItemPackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull ItemStack item) {
        List<PacketWrapper<?>> list = new ArrayList<>();

        PacketWrapper<?> dataPacket = this.createMetadataPacket(entity.getId(), dataList -> {
            dataList.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true)); // no gravity
            dataList.add(new EntityData<>(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(item))); // item
        });

        if (needSpawn) list.add(this.createSpawnPacket(this.itemType, entity));
        list.add(dataPacket);

        return list;
    }

    @Override
    @NotNull
    protected List<PacketWrapper<?>> getShowcasePackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull ItemStack item) {
        List<PacketWrapper<?>> list = new ArrayList<>();

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

        if (needSpawn) list.add(this.createSpawnPacket(this.showcaseType, entity));
        list.add(dataPacket);

        if (this.showcaseType == EntityType.ARMOR_STAND) {
            List<Equipment> equipment = Lists.newList(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(item)));
            PacketWrapper<?> armorPacket = new WrapperPlayServerEntityEquipment(entity.getId(), equipment);
            list.add(armorPacket);
        }

        return list;
    }

    @Override
    @NotNull
    protected List<PacketWrapper<?>> getHologramPackets(@NotNull FakeEntity entity, boolean needSpawn, @NotNull String textLine) {
        List<PacketWrapper<?>> list = new ArrayList<>();

        PaperBridge bridge = (PaperBridge) Software.instance();
        NightComponent component = NightMessage.parse(textLine);
        Component textComponent = bridge.getTextComponentAdapter().adaptComponent(component);

        PacketWrapper<?> dataPacket = this.createMetadataPacket(entity.getId(), dataList -> {
            // Armor Stands (legacy)
            if (this.hologramType == EntityType.ARMOR_STAND) {
                dataList.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20)); // invisible
                dataList.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(textComponent))); // display name
                dataList.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, true)); // custom name visible
                dataList.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true)); // no gravity
                dataList.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte) (0x01 | 0x08 | 0x10))); // isSmall noBasePlate setMarker
            }
            // Displays (modern)
            else {
                dataList.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte) 1)); // billboard
                dataList.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, textComponent)); // text
                dataList.add(new EntityData<>(24, EntityDataTypes.INT, this.lineWidth));
                dataList.add(new EntityData<>(25, EntityDataTypes.INT, this.backgroundColor));
                dataList.add(new EntityData<>(26, EntityDataTypes.BYTE, (byte) this.textOpacity));
                dataList.add(new EntityData<>(27, EntityDataTypes.BYTE, this.textBitmask));
            }
        });

        if (needSpawn) list.add(this.createSpawnPacket(this.hologramType, entity));
        list.add(dataPacket);

        return list;
    }

    @Override
    @NotNull
    protected PacketWrapper<?> createSpawnPacket(@NotNull EntityType entityType, @NotNull FakeEntity entity) {
        Location location = entity.getLocation();
        int entityId = entity.getId();

        com.github.retrooper.packetevents.protocol.entity.type.EntityType type = SpigotConversionUtil.fromBukkitEntityType(entityType);
        com.github.retrooper.packetevents.protocol.world.Location loc = SpigotConversionUtil.fromBukkitLocation(location);

        return new WrapperPlayServerSpawnEntity(entityId, UUID.randomUUID(), type, loc, 0F, 0, new Vector3d());
    }

    @Override
    @NotNull
    protected PacketWrapper<?> createDestroyPacket(@NotNull Set<Integer> list) {
        return new WrapperPlayServerDestroyEntities(list.stream().mapToInt(i -> i).toArray());
    }

    @NotNull
    private PacketWrapper<?> createMetadataPacket(int entityID, @NotNull Consumer<List<EntityData<?>>> consumer) {
        List<EntityData<?>> dataList = new ArrayList<>();

        consumer.accept(dataList);

        return new WrapperPlayServerEntityMetadata(entityID, dataList);
    }
}
