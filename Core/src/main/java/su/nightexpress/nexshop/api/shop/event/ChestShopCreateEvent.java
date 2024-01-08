package su.nightexpress.nexshop.api.shop.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.util.ShopType;

public class ChestShopCreateEvent extends Event implements Cancellable {

    public static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private final Player player;
    private final Block  block;
    private final ItemStack item;
    private final ShopType shopType;

    private boolean cancelled;

    public ChestShopCreateEvent(@NotNull Player player, @NotNull Block block, @NotNull ItemStack item, @NotNull ShopType shopType) {
        this.player = player;
        this.block = block;
        this.item = item;
        this.shopType = shopType;
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
    public Block getBlock() {
        return block;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public ShopType getShopType() {
        return shopType;
    }
}
