package su.nightexpress.nexshop.shop.virtual.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nightcore.manager.AbstractListener;

public class VirtualShopListener extends AbstractListener<ShopPlugin> {

    private final VirtualShopModule module;

    public VirtualShopListener(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin);
        this.module = module;
    }

    private void updatePlayerBasedPrices() {
        this.module.getShops().forEach(shop -> shop.getProducts().forEach(product -> {
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
