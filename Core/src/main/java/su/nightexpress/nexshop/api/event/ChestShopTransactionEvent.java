package su.nightexpress.nexshop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.util.TransactionResult;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;

public class ChestShopTransactionEvent extends ShopTransactionEvent<ChestProduct> {

    private static final HandlerList handlerList = new HandlerList();

    public ChestShopTransactionEvent(@NotNull Player player, @NotNull TransactionResult transactionResult) {
        super(player, transactionResult);
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
