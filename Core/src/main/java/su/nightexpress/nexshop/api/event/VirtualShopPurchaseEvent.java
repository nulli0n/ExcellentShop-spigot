package su.nightexpress.nexshop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualPreparedProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;

public class VirtualShopPurchaseEvent extends ShopPurchaseEvent<VirtualProduct> {

    private static final HandlerList handlerList = new HandlerList();

    public VirtualShopPurchaseEvent(@NotNull Player player, @NotNull VirtualPreparedProduct prepared) {
        super(player, prepared);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
