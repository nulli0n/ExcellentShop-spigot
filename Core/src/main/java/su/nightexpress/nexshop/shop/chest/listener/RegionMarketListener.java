package su.nightexpress.nexshop.shop.chest.listener;

import net.alex9849.arm.adapters.WGRegion;
import net.alex9849.arm.events.RestoreRegionEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.util.BlockPos;
import su.nightexpress.nightcore.manager.AbstractListener;

public class RegionMarketListener extends AbstractListener<ShopPlugin> {

    private final ChestShopModule module;

    public RegionMarketListener(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegionReset(RestoreRegionEvent event) {
        World world = event.getRegion().getRegionworld();
        WGRegion region = event.getRegion().getRegion();

        this.module.getShops(world).forEach(shop -> {
            BlockPos blockPos = shop.getBlockPos();
            if (region.contains(blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
                this.module.removeShop(shop);
            }
        });
    }
}
