package su.nightexpress.excellentshop.integration.shop;

import me.angeschossen.upgradeablehoppers.api.events.hopper.link.LinkCreationEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.api.playershop.PlayerShopManager;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.manager.AbstractListener;

public class UpgradeHopperListener extends AbstractListener<NightPlugin> {

    private final PlayerShopManager manager;

    public UpgradeHopperListener(@NotNull NightPlugin plugin, @NotNull PlayerShopManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLinkCreate(LinkCreationEvent event) {
        Block block = event.getBlockInventoryHolder().getBlock();
        if (this.manager.isShop(block)) {
            event.setCancelled(true);
        }
    }
}
