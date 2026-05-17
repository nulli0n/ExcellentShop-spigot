package su.nightexpress.excellentshop.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;

@NullMarked
public class TransactionPreValidateEvent extends Event implements Cancellable {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final EPreparedTransaction transaction;

    private boolean cancelled;

    public TransactionPreValidateEvent(EPreparedTransaction transaction) {
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

    public EPreparedTransaction getTransaction() {
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
