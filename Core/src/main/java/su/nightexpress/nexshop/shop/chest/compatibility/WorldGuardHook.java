package su.nightexpress.nexshop.shop.chest.compatibility;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class WorldGuardHook implements ClaimHook {

    private final WorldGuard worldGuard;

    public WorldGuardHook() {
        worldGuard = WorldGuard.getInstance();
    }

    @Nullable
    public ProtectedRegion getProtectedRegion(@NotNull Entity entity) {
        return getProtectedRegion(entity.getLocation());
    }

    @Nullable
    public ProtectedRegion getProtectedRegion(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        com.sk89q.worldedit.world.World sworld = BukkitAdapter.adapt(world);
        BlockVector3 vector3 = BukkitAdapter.adapt(location).toVector().toBlockPoint();
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(sworld);
        if (regionManager == null) return null;

        ApplicableRegionSet set = regionManager.getApplicableRegions(vector3);
        return set.getRegions().stream().max(Comparator.comparingInt(ProtectedRegion::getPriority)).orElse(null);
    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        ProtectedRegion region = getProtectedRegion(player);
        return region != null && region.getOwners().contains(player.getUniqueId());
    }
}
