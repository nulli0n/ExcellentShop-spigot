package su.nightexpress.nexshop.api.shop.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.Transaction;

public class ShopTransactionEvent extends Event {

    public static final HandlerList handlerList = new HandlerList();

    protected Player      player;
    protected Transaction transaction;

    public ShopTransactionEvent(@NotNull Player player, @NotNull Transaction transaction) {
        this.player = player;
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
    public Transaction getTransaction() {
        return transaction;
    }
}
