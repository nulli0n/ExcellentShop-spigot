package su.nightexpress.nexshop.api.shop.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Transaction;

public class ShopTransactionEvent extends Event {

    public static final HandlerList handlerList = new HandlerList();

    private final Player      player;
    private final Shop shop;
    private final Transaction transaction;

    public ShopTransactionEvent(@NotNull Player player, @NotNull Shop shop, @NotNull Transaction transaction) {
        this.player = player;
        this.shop = shop;
        this.transaction = transaction;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public Shop getShop() {
        return shop;
    }

    @NotNull
    public Transaction getTransaction() {
        return transaction;
    }

    @NotNull
    public Transaction.Result getTransactionResult() {
        return this.transaction.getResult();
    }
}
