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
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class V1_18_R2 implements ChestNMS {

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
            armorStand.setCustomName(StringUtil.color(name));
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

    /*@Override
    @NotNull
    public ArmorStand createHologram(@NotNull Location location, @NotNull ItemStack showcase) {
        org.bukkit.World world = location.getWorld();
        if (world == null) throw new IllegalStateException("Location world is null!");

        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        CustomStand entity = new CustomStand(nmsWorld);
        ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();
        entity.moveTo(location.getX(), location.getY(), location.getZ(), 0, 0);
        entity.setYHeadRot(0);
        entity.setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(showcase));
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setSmall(false);
        armorStand.setGravity(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setSilent(true);
        armorStand.setRemoveWhenFarAway(false);

        nmsWorld.addFreshEntity(entity);

        armorStand.teleport(location);
        return armorStand;
    }

    @Override
    @NotNull
    public Item createItem(@NotNull Location location) {
        org.bukkit.World world = location.getWorld();
        if (world == null) throw new IllegalStateException("Location world is null!");

        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        CustomItem customItem = new CustomItem(nmsWorld);
        Item item = (Item) customItem.getBukkitEntity();

        customItem.setPos(location.getX(), location.getY(), location.getZ());
        item.setItemStack(UNKNOWN);
        item.setPickupDelay(Short.MAX_VALUE);
        item.setInvulnerable(true);
        item.setCustomName("");

        nmsWorld.addFreshEntity(customItem);

        item.teleport(location);
        return item;
    }

    static class CustomItem extends ItemEntity {

        public CustomItem(ServerLevel world) {
            super(EntityType.ITEM, world);
        }

        @Override
        public void tick() { }


        @Override
        public void baseTick() { }

        @Override
        public boolean fireImmune() {
            return true;
        }

        @Override
        public void lavaHurt() { }

        @Override
        public void setSecondsOnFire(int i, boolean callEvent) { }

        @Override
        public void move(MoverType enummovetype, Vec3 vec3d) { }

        @Override
        public void playerTouch(Player entityhuman) { }

        @Override
        public void inactiveTick() { }

        @Override
        public boolean isAlive() {
            return false;
        }

        @Override
        public boolean hurt(DamageSource damagesource, float f2) {
            return false;
        }

        @Override
        public void remove(Entity.RemovalReason entity_removalreason) {
            if (!ChestDisplayRemovalState.ALLOW_REMOVE) return;
            super.remove(entity_removalreason);
        }
    }

    static class CustomStand extends net.minecraft.world.entity.decoration.ArmorStand {

        public CustomStand(ServerLevel world) {
            super(EntityType.ARMOR_STAND, world);
            this.disabledSlots = EquipmentSlot.HEAD.getFilterFlag();
        }

        @Override
        public boolean isShowArms() {
            return false;
        }

        @Override
        protected void doPush(Entity entity) { }

        @Override
        public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
            return InteractionResult.FAIL;
        }

        @Override
        public InteractionResult interact(Player entityhuman, InteractionHand enumhand) {
            return InteractionResult.FAIL;
        }

        @Override
        public void baseTick() {
            super.baseTick();
        }

        @Override
        public boolean fireImmune() {
            return true;
        }

        @Override
        public void lavaHurt() { }

        @Override
        public void setSecondsOnFire(int i, boolean callEvent) { }

        @Override
        public void move(MoverType enummovetype, Vec3 vec3d) { }

        @Override
        protected void pushEntities() { }

        @Override
        public boolean hurt(DamageSource damagesource, float f2) {
            return false;
        }

        @Override
        public void remove(Entity.RemovalReason entity_removalreason) {
            if (!ChestDisplayRemovalState.ALLOW_REMOVE) return;
            super.remove(entity_removalreason);
        }
    }*/
}
