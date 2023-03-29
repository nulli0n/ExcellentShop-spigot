package su.nightexpress.nexshop.shop.chest.nms;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Colorizer;

import java.util.ArrayList;
import java.util.List;

public class V1_19_R1 implements ChestNMS {

    @Override
    public int createHologram(@NotNull Location location, @NotNull ItemStack showcase, @NotNull String name) {
        org.bukkit.World world = location.getWorld();
        if (world == null) return -1;

        ServerLevel level = ((CraftWorld) world).getHandle();
        net.minecraft.world.entity.decoration.ArmorStand entity = new net.minecraft.world.entity.decoration.ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
        ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();

        entity.moveTo(location.getX(), location.getY(), location.getZ(), 0, 0);
        entity.setYHeadRot(0);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        if (!name.isEmpty()) {
            armorStand.setCustomName(Colorizer.apply(name));
            armorStand.setCustomNameVisible(true);
        }
        armorStand.setSmall(false);
        armorStand.setGravity(false);
        armorStand.setSilent(true);
        armorStand.setRemoveWhenFarAway(false);

        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equip = new ArrayList<>();
        equip.add(Pair.of(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(showcase)));

        ClientboundAddEntityPacket spawnEntityLiving = new ClientboundAddEntityPacket(entity);
        ClientboundSetEntityDataPacket entityMetadata = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), false);
        ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(entity.getId(), equip);

        location.getWorld().getPlayers().forEach(player -> {
            ((CraftPlayer) player).getHandle().connection.send(spawnEntityLiving);
            ((CraftPlayer) player).getHandle().connection.send(entityMetadata);
            ((CraftPlayer) player).getHandle().connection.send(equipmentPacket);
        });

        return entity.getId();
    }

    @Override
    public int createItem(@NotNull Location location, @NotNull ItemStack product) {
        org.bukkit.World world = location.getWorld();
        if (world == null) return -1;

        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ItemEntity entity = new ItemEntity(EntityType.ITEM, nmsWorld);
        Item item = (Item) entity.getBukkitEntity();

        entity.setPos(location.getX(), location.getY(), location.getZ());
        item.setItemStack(product);
        item.setPickupDelay(Short.MAX_VALUE);
        item.setInvulnerable(true);
        item.setCustomName("");

        ClientboundAddEntityPacket spawnEntityLiving = new ClientboundAddEntityPacket(entity);
        ClientboundSetEntityDataPacket entityMetadata = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), false);

        location.getWorld().getPlayers().forEach(player -> {
            ((CraftPlayer) player).getHandle().connection.send(spawnEntityLiving);
            ((CraftPlayer) player).getHandle().connection.send(entityMetadata);
        });

        return entity.getId();
    }

    @Override
    public void deleteEntity(int... ids) {
        ClientboundRemoveEntitiesPacket packetPlayOutEntityDestroy = new ClientboundRemoveEntitiesPacket(ids);
        Bukkit.getServer().getOnlinePlayers().forEach(player ->  {
            ((CraftPlayer) player).getHandle().connection.send(packetPlayOutEntityDestroy);
        });
    }
}
