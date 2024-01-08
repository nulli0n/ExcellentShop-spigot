package su.nightexpress.nexshop.api.shop.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

public class ChestShopRemoveEvent extends Event implements Cancellable {

    public static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private final Player    player;
    private final ChestShop shop;

    private boolean cancelled;

    public ChestShopRemoveEvent(@NotNull Player player, @NotNull ChestShop shop) {
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

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public ChestShop getShop() {
        return shop;
    }
}
