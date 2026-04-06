package su.nightexpress.excellentshop.feature.playershop.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ChestShopCreateEvent extends Event implements Cancellable {

    public static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NonNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private final Player player;
    private final Block  block;
    private final ItemStack item;

    private boolean cancelled;

    public ChestShopCreateEvent(@NonNull Player player, @NonNull Block block) {
        this(player, block, null);
    }

    public ChestShopCreateEvent(@NonNull Player player, @NonNull Block block, @Nullable ItemStack item) {
        this.player = player;
        this.block = block;
        this.item = item;
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
    public Block getBlock() {
        return block;
    }

    /**
     * @return Item used to create the shop.
     */
    @Nullable
    public ItemStack getItem() {
        return item;
    }
}
