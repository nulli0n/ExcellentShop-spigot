package su.nightexpress.nexshop.api.shop.virtual.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent;
import su.nightexpress.nexshop.shop.virtual.object.ProductVirtualPrepared;

public class VirtualShopPurchaseEvent extends AbstractShopPurchaseEvent {

    private static final HandlerList handlerList = new HandlerList();

    public VirtualShopPurchaseEvent(@NotNull Player player, @NotNull ProductVirtualPrepared prepared) {
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
