package su.nightexpress.nexshop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestPreparedProduct;

public class ChestShopPurchaseEvent extends ShopPurchaseEvent<ChestProduct> {

    private static final HandlerList handlerList = new HandlerList();

    public ChestShopPurchaseEvent(@NotNull Player player, @NotNull ChestPreparedProduct prepared, @NotNull Result result) {
        super(player, prepared, result);
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
