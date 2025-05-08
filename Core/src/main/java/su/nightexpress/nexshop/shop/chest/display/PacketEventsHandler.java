package su.nightexpress.nexshop.shop.chest.display;

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
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.function.Consumer;

public class PacketEventsHandler extends DisplayHandler<PacketWrapper<?>> {

    private final PlayerManager playerManager;

    public PacketEventsHandler(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, module);
        this.playerManager = PacketEvents.getAPI().getPlayerManager();
    }

    @Override
    protected void sendPacket(@NotNull Player player, @NotNull PacketWrapper<?> packet) {
        this.playerManager.sendPacket(player, packet);
    }

    @Override
    protected void broadcastPacket(@NotNull PacketWrapper<?> packet) {
        this.plugin.getServer().getOnlinePlayers().forEach(player -> this.playerManager.sendPacket(player, packet));
    }


    @Override
    @NotNull
    protected List<PacketWrapper<?>> getItemPackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull ItemStack item) {
        List<PacketWrapper<?>> list = new ArrayList<>();

        PacketWrapper<?> spawnPacket = this.createSpawnPacket(type, location, entityID);
        PacketWrapper<?> dataPacket = this.createMetadataPacket(entityID, dataList -> {
            dataList.add(new EntityData(5, EntityDataTypes.BOOLEAN, true)); // no gravity
            dataList.add(new EntityData(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(item))); // item
        });


        if (create) list.add(spawnPacket);
        list.add(dataPacket);

        return list;
    }

    @Override
    @NotNull
    protected List<PacketWrapper<?>> getShowcasePackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull ItemStack item) {
        List<PacketWrapper<?>> list = new ArrayList<>();

        PacketWrapper<?> spawnPacket = this.createSpawnPacket(type, location, entityID);

        PacketWrapper<?> dataPacket = this.createMetadataPacket(entityID, dataList -> {
            if (type == EntityType.ARMOR_STAND) {
                dataList.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20)); // invisible
                dataList.add(new EntityData(3, EntityDataTypes.BOOLEAN, false)); // custom name visible
                dataList.add(new EntityData(5, EntityDataTypes.BOOLEAN, true)); // no gravity
                dataList.add(new EntityData(15, EntityDataTypes.BYTE, (byte) (0x08 | 0x10))); // isSmall noBasePlate

            }
            else {
                dataList.add(new EntityData(12, EntityDataTypes.VECTOR3F, new Vector3f(0.7f, 0.7f, 0.7f))); // scale
                dataList.add(new EntityData(23, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(item))); // slot
                dataList.add(new EntityData(24, EntityDataTypes.BYTE, (byte) 5)); // mode HEAD
            }
        });

        if (create) list.add(spawnPacket);
        list.add(dataPacket);

        if (type == EntityType.ARMOR_STAND) {
            List<Equipment> equipment = Lists.newList(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(item)));
            PacketWrapper<?> armorPacket = new WrapperPlayServerEntityEquipment(entityID, equipment);
            list.add(armorPacket);
        }

        return list;
    }

    @SuppressWarnings("deprecation")
    @Override
    @NotNull
    protected List<PacketWrapper<?>> getHologramPackets(int entityID, boolean create, @NotNull EntityType type, @NotNull Location location, @NotNull String textLine) {
        List<PacketWrapper<?>> list = new ArrayList<>();

        PacketWrapper<?> spawnPacket = this.createSpawnPacket(type, location, entityID);
        PacketWrapper<?> dataPacket = this.createMetadataPacket(entityID, dataList -> {
            // Armor Stands (legacy)
            if (type == EntityType.ARMOR_STAND) {
                dataList.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20)); // invisible
                dataList.add(new EntityData(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.of(NightMessage.asJson(textLine)))); // display name
                dataList.add(new EntityData(3, EntityDataTypes.BOOLEAN, true)); // custom name visible
                dataList.add(new EntityData(5, EntityDataTypes.BOOLEAN, true)); // no gravity
                dataList.add(new EntityData(15, EntityDataTypes.BYTE, (byte) (0x01 | 0x08 | 0x10))); // isSmall noBasePlate setMarker
            }
            // Displays (modern)
            else {
                dataList.add(new EntityData(15, EntityDataTypes.BYTE, (byte) 1)); // billboard
                dataList.add(new EntityData(23, EntityDataTypes.COMPONENT, NightMessage.asJson(textLine))); // text
                dataList.add(new EntityData(27, EntityDataTypes.BYTE, (byte) 0x1)); // shadow
            }
        });

        if (create) list.add(spawnPacket);
        list.add(dataPacket);

        return list;
    }

    @Override
    @NotNull
    protected PacketWrapper<?> createSpawnPacket(@NotNull EntityType entityType, @NotNull Location location, int entityID) {
        com.github.retrooper.packetevents.protocol.entity.type.EntityType type = SpigotConversionUtil.fromBukkitEntityType(entityType);
        com.github.retrooper.packetevents.protocol.world.Location loc = SpigotConversionUtil.fromBukkitLocation(location);

        return new WrapperPlayServerSpawnEntity(entityID, UUID.randomUUID(), type, loc, 0F, 0, new Vector3d());
    }

    @Override
    @NotNull
    protected PacketWrapper<?> createDestroyPacket(@NotNull Set<Integer> list) {
        return new WrapperPlayServerDestroyEntities(list.stream().mapToInt(i -> i).toArray());
    }

    @NotNull
    private PacketWrapper<?> createMetadataPacket(int entityID, @NotNull Consumer<List<EntityData>> consumer) {
        List<EntityData> dataList = new ArrayList<>();

        consumer.accept(dataList);

        return new WrapperPlayServerEntityMetadata(entityID, dataList);
    }
}
