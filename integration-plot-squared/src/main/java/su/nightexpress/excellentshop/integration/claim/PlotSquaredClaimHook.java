package su.nightexpress.excellentshop.integration.claim;

import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.events.PlotClearEvent;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.api.claim.ClaimHook;
import su.nightexpress.excellentshop.api.playershop.PlayerShopManager;
import su.nightexpress.nightcore.util.geodata.Cuboid;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;

import java.util.Optional;

public class PlotSquaredClaimHook implements ClaimHook {

    private final PlayerShopManager manager;
    private final PlotAPI api;

    public PlotSquaredClaimHook(@NotNull PlayerShopManager manager) {
        this.manager = manager;
        this.api = new PlotAPI();
    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Location location = Location.at(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        PlotArea area = this.api.getPlotSquared().getPlotAreaManager().getPlotArea(location);
        if (area == null) return false;

        Plot plot = area.getPlot(location);
        return plot != null && plot.isOwner(player.getUniqueId());
    }

    @Subscribe
    public void onPlotClear(PlotClearEvent event) {
        Plot plot = event.getPlot();

        World world = Optional.ofNullable(event.getWorld()).map(Bukkit::getWorld).orElse(null);
        if (world == null) return;

        plot.getRegions().forEach(cuboidRegion -> {
            var minPoint = cuboidRegion.getMinimumPoint();
            var maxPoint = cuboidRegion.getMaximumPoint();

            BlockPos minPos = new BlockPos(minPoint.getX(), minPoint.getY(), minPoint.getZ());
            BlockPos maxPos = new BlockPos(maxPoint.getX(), maxPoint.getY(), maxPoint.getZ());
            Cuboid cuboid = new Cuboid(minPos, maxPos);

            this.manager.getShopsInArea(world, cuboid).forEach(this.manager::removeShop);
        });
    }
}
