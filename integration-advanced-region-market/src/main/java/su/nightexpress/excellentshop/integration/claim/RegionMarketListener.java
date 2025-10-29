package su.nightexpress.excellentshop.integration.claim;

import net.alex9849.arm.adapters.WGRegion;
import net.alex9849.arm.events.RestoreRegionEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.api.playershop.PlayerShopManager;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.util.geodata.Cuboid;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;

public class RegionMarketListener extends AbstractListener<NightPlugin> {

    private final PlayerShopManager manager;

    public RegionMarketListener(@NotNull NightPlugin plugin, @NotNull PlayerShopManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegionReset(RestoreRegionEvent event) {
        World world = event.getRegion().getRegionworld();
        WGRegion region = event.getRegion().getRegion();

        Vector minPoint = region.getMinPoint();
        Vector maxPoint = region.getMaxPoint();

        BlockPos min = new BlockPos(minPoint.getBlockX(), minPoint.getBlockY(), minPoint.getBlockZ());
        BlockPos max = new BlockPos(maxPoint.getBlockX(), maxPoint.getBlockY(), maxPoint.getBlockZ());
        Cuboid cuboid = new Cuboid(min, max);

        this.manager.getShopsInArea(world, cuboid).forEach(this.manager::removeShop);
    }
}
