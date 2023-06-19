package su.nightexpress.nexshop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.shop.util.TransactionResult;

public abstract class ShopTransactionEvent<P extends Product<P, ?, ?>> extends Event {

    protected Player            player;
    protected TransactionResult result;

    public ShopTransactionEvent(@NotNull Player player, @NotNull TransactionResult result) {
        this.player = player;
        this.result = result;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public TransactionResult getResult() {
        return result;
    }
}
