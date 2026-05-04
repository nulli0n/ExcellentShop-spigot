package su.nightexpress.excellentshop.feature.playershop.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;

public class ChestShopCanManageEvent extends Event {

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
    private boolean         canManage;

    public ChestShopCanManageEvent(@NotNull Player player, @NotNull ChestShop shop, boolean canManage) {
        this.player = player;
        this.shop = shop;
        this.canManage = canManage;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public ChestShop getShop() {
        return shop;
    }

    public boolean canManage() {
        return canManage;
    }

    public void setCanManage(boolean canManage) {
        this.canManage = canManage;
    }
}
