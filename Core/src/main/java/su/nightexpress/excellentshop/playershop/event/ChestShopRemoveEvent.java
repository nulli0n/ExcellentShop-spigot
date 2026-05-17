package su.nightexpress.excellentshop.playershop.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.playershop.impl.ChestShop;

public class ChestShopRemoveEvent extends Event implements Cancellable {

    public static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NonNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private final Player    player;
    private final ChestShop shop;

    private boolean cancelled;

    public ChestShopRemoveEvent(@NonNull Player player, @NonNull ChestShop shop) {
        this.player = player;
        this.shop = shop;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NonNull
    public Player getPlayer() {
        return player;
    }

    @NonNull
    public ChestShop getShop() {
        return shop;
    }
}
