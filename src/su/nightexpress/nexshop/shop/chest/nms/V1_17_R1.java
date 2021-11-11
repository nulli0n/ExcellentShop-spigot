package su.nightexpress.nexshop.shop.chest.nms;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestDisplayHandler;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;

public class V1_17_R1 implements ChestNMS {

    @Override
    public ArmorStand createHologram(@NotNull IShopChest shop) {
        Location loc = shop.getDisplayLocation();
        if (!this.isSafeCreation(loc)) return null;

        org.bukkit.World world = shop.getChest().getWorld();

        World nmsWorld = ((CraftWorld) world).getHandle();
        CustomStand entity = new CustomStand(nmsWorld);
        entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        entity.setHeadRotation(0);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.setSlot(EnumItemSlot.f, CraftItemStack.asNMSCopy(ChestShopConfig.DISPLAY_SHOWCASE));
        entity.setSmall(false);
        entity.setNoGravity(true);
        entity.setCustomNameVisible(true);
        entity.setSilent(true);
        entity.getBukkitEntity().setCustomName(shop.getDisplayText().get(0));

        entity.getEntityType().createCreature(nmsWorld.getMinecraftWorld(),
                null,
                null,
                null,
                new BlockPosition(loc.getX(), loc.getY(), loc.getZ()),
                EnumMobSpawn.n, false, false);

        nmsWorld.addEntity(entity);
        entity.getBukkitEntity().teleport(loc);

        return (ArmorStand) entity.getBukkitEntity();
    }

    @Override
    public Item createItem(@NotNull IShopChest shop) {
        Location loc = shop.getDisplayItemLocation();
        if (!this.isSafeCreation(loc)) return null;

        org.bukkit.World world = shop.getChest().getWorld();
        World nmsWorld = ((CraftWorld) world).getHandle();
        EntityItem customItem = new CustomItem(nmsWorld);
        customItem.setPosition(loc.getX(), loc.getY(), loc.getZ());
        customItem.setItemStack(CraftItemStack.asNMSCopy(UNKNOWN));
        customItem.setPickupDelay(Short.MAX_VALUE);
        customItem.setInvulnerable(true);
        customItem.setCustomName(new ChatComponentText(""));

        customItem.getEntityType().createCreature(nmsWorld.getMinecraftWorld(),
                null,
                null,
                null,
                new BlockPosition(loc.getX(), loc.getY(), loc.getZ()),
                EnumMobSpawn.n, false, false);

        nmsWorld.addEntity(customItem);
        customItem.getBukkitEntity().teleport(loc);

        return (Item) customItem.getBukkitEntity();
    }

    static class CustomItem extends EntityItem {

        public CustomItem(World world) {
            super(EntityTypes.Q, world);
        }

        @Override
        public void tick() {
        }

        @Override
        public void entityBaseTick() {

        }

        @Override
        public boolean isFireProof() {
            return true;
        }

        @Override
        public void burnFromLava() {

        }

        @Override
        public void setOnFire(int i) {

        }

        @Override
        public void move(EnumMoveType enummovetype, Vec3D vec3d) {

        }

        @Override
        public void pickup(EntityHuman entityhuman) {
        }

        @Override
        public void inactiveTick() {
        }

        @Override
        public boolean isAlive() {
            return false;
        }

        @Override
        public boolean damageEntity(DamageSource damagesource, float f2) {
            return false;
        }

        @Override
        public void a(Entity.RemovalReason entity_removalreason) {
            if (!ChestDisplayHandler.ALLOW_REMOVE) return;
            super.a(entity_removalreason);
        }
    }

    static class CustomStand extends EntityArmorStand {

        public CustomStand(World world) {
            super(EntityTypes.c, world);
            this.cf = EnumItemSlot.f.getSlotFlag(); // HEAD
        }

        @Override
        public boolean hasArms() {
            return false;
        }

        // .doPush
        @Override
        protected void A(Entity entity) {
        }

        // .interact
        @Override
        public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
            return EnumInteractionResult.e;
        }

        @Override
        public EnumInteractionResult a(EntityHuman entityhuman, EnumHand enumhand) {
            return EnumInteractionResult.e;
        }
        
        /*@Override
        public boolean isAlive() {
            return false;
        }*/

        @Override
        public void entityBaseTick() {
            super.entityBaseTick();
        }

        @Override
        public boolean isFireProof() {
            return true;
        }

        @Override
        public void burnFromLava() {

        }

        @Override
        public void setOnFire(int i) {

        }

        @Override
        public void move(EnumMoveType enummovetype, Vec3D vec3d) {

        }

        @Override
        protected void collideNearby() {
        }

        @Override
        public boolean damageEntity(DamageSource damagesource, float f2) {
            return false;
        }

        @Override
        public void a(Entity.RemovalReason entity_removalreason) {
            if (!ChestDisplayHandler.ALLOW_REMOVE) return;
            super.a(entity_removalreason);
        }
    }
}
