package su.nightexpress.nexshop.shop.chest.listener;

import me.angeschossen.upgradeablehoppers.api.events.hopper.link.LinkCreationEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nightcore.manager.AbstractListener;

public class UpgradeHopperListener extends AbstractListener<ShopPlugin> {

    private final ChestShopModule module;

    public UpgradeHopperListener(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLinkCreate(LinkCreationEvent event) {
        Block block = event.getBlockInventoryHolder().getBlock();
        if (this.module.isShop(block)) {
            event.setCancelled(true);
        }
    }
}
