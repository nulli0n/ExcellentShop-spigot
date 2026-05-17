package su.nightexpress.nexshop.api.shop.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nexshop.auction.listing.ActiveListing;

import org.jspecify.annotations.NonNull;

public class AuctionListingCreateEvent extends Event implements Cancellable {

    public static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NonNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private final Player        player;
    private final Currency      currency;
    private final ActiveListing listing;

    private boolean cancelled;

    public AuctionListingCreateEvent(@NonNull Player player, @NonNull Currency currency,
                                     @NonNull ActiveListing listing) {
        this.player = player;
        this.currency = currency;
        this.listing = listing;
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

    /**
     * @return Currency that was used for this listing.
     */
    @NonNull
    public Currency getCurrency() {
        return currency;
    }

    /**
     * @return Listing that is about to be created.
     */
    @NonNull
    public ActiveListing getListing() {
        return listing;
    }
}
