package su.nightexpress.excellentshop.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;

public class TransactionCompletedEvent extends Event implements Cancellable {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final ECompletedTransaction transaction;

    private boolean cancelled;

    public TransactionCompletedEvent(ECompletedTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public ECompletedTransaction getTransaction() {
        return this.transaction;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
