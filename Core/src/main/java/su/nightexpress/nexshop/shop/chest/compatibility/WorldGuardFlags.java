package su.nightexpress.nexshop.shop.chest.compatibility;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class WorldGuardFlags implements ClaimHook {

    private static StateFlag FLAG_CHEST_SHOP_CREATE;

    private static final String NAME_CHEST_SHOP_CREATE = "chest-shop-create";

    static WorldGuard worldGuard = WorldGuard.getInstance();

    public WorldGuardFlags() {

    }

    public static boolean canFights(@NotNull Entity damager, @NotNull Entity victim) {
        return WorldGuardPlugin.inst().createProtectionQuery().testEntityDamage(damager, victim);
    }

    public static boolean isInRegion(@NotNull Entity entity, @NotNull String region) {
        return getRegion(entity).equalsIgnoreCase(region);
    }

    @NotNull
    public static String getRegion(@NotNull Entity entity) {
        return getRegion(entity.getLocation());
    }

    @NotNull
    public static String getRegion(@NotNull Location loc) {
        ProtectedRegion region = getProtectedRegion(loc);
        return region == null ? "" : region.getId();
    }

    @Nullable
    public static ProtectedRegion getProtectedRegion(@NotNull Entity entity) {
        return getProtectedRegion(entity.getLocation());
    }

    @Nullable
    public static ProtectedRegion getProtectedRegion(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        com.sk89q.worldedit.world.World sworld = BukkitAdapter.adapt(world);
        BlockVector3 vector3 = BukkitAdapter.adapt(location).toVector().toBlockPoint();
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(sworld);
        if (regionManager == null) return null;

        ApplicableRegionSet set = regionManager.getApplicableRegions(vector3);
        return set.getRegions().stream().max(Comparator.comparingInt(ProtectedRegion::getPriority)).orElse(null);
    }

    @NotNull
    public static Collection<ProtectedRegion> getProtectedRegions(@NotNull World w) {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(w);
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(world);

        return regionManager == null ? Collections.emptySet() : regionManager.getRegions().values();
    }

    public static void setupFlag() {
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag(NAME_CHEST_SHOP_CREATE, true);
            flagRegistry.register(flag);
            FLAG_CHEST_SHOP_CREATE = flag;
        }
        catch (FlagConflictException e) {
            Flag<?> existing = flagRegistry.get(NAME_CHEST_SHOP_CREATE);
            if (existing instanceof StateFlag existing2) {
                FLAG_CHEST_SHOP_CREATE = existing2;
            }
        }
    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        //if (!Hooks.hasPlugin(HookId.WORLD_GUARD)) return true;

        ProtectedRegion region = getProtectedRegion(player);
        return region != null && region.getOwners().contains(player.getUniqueId());
    }

    public static boolean checkFlag(@NotNull Player player, @NotNull Location location) {
        if (FLAG_CHEST_SHOP_CREATE == null) return true;

        World world1 = location.getWorld();
        if (world1 == null) return false;

        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(world1);
        BlockVector3 vector3 = BukkitAdapter.adapt(location).toVector().toBlockPoint();

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (regionManager == null) return true;

        ApplicableRegionSet set = regionManager.getApplicableRegions(vector3);
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        return set.testState(localPlayer, FLAG_CHEST_SHOP_CREATE);
    }
}
