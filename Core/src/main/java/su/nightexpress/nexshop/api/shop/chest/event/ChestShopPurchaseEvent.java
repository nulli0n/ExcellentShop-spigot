package su.nightexpress.nexshop.api.shop.chest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.shop.chest.IProductChestPrepared;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent;

public class ChestShopPurchaseEvent extends AbstractShopPurchaseEvent {

    private static final HandlerList handlerList = new HandlerList();

    public ChestShopPurchaseEvent(@NotNull Player player, @NotNull IProductChestPrepared prepared) {
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

    @Override
    @NotNull
    public IProductChestPrepared getPrepared() {
        return (IProductChestPrepared) this.prepared;
    }
}
