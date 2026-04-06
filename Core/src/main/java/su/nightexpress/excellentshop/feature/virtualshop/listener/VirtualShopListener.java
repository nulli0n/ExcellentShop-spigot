package su.nightexpress.excellentshop.feature.virtualshop.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.nightcore.manager.AbstractListener;

public class VirtualShopListener extends AbstractListener<ShopPlugin> {

    private final VirtualShopModule module;

    public VirtualShopListener(@NonNull ShopPlugin plugin, @NonNull VirtualShopModule module) {
        super(plugin);
        this.module = module;
    }

    private void updatePlayerBasedPrices() {
        this.module.getShops().forEach(shop -> shop.getValidProducts().forEach(product -> {
            if (product.getPricingType() == PriceType.PLAYER_AMOUNT) {
                product.updatePrice(false);
            }
        }));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.updatePlayerBasedPrices();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.updatePlayerBasedPrices();
    }
}
