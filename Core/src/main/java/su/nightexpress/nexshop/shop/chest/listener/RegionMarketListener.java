package su.nightexpress.nexshop.shop.chest.listener;

import net.alex9849.arm.adapters.WGRegion;
import net.alex9849.arm.events.RestoreRegionEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;

public class RegionMarketListener extends AbstractListener<ExcellentShop> {

    private final ChestShopModule module;

    public RegionMarketListener(@NotNull ExcellentShop plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegionReset(RestoreRegionEvent event) {
        World world = event.getRegion().getRegionworld();
        WGRegion region = event.getRegion().getRegion();

        this.module.getShops().forEach(shop -> {
            Location location = shop.getLocation();
            if (location.getWorld() != world) return;

            if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                this.module.removeShop(shop);
            }
        });
    }
}
