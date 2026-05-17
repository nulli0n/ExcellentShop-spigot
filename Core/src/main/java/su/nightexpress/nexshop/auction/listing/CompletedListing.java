package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.nightcore.bridge.currency.Currency;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class CompletedListing extends AbstractListing {

    private final String buyerName;
    private final long   buyDate;

    private boolean claimed;

    @NonNull
    public static CompletedListing create(@NonNull ActiveListing listing, @NonNull Player buyer) {
        UUID id = UUID.randomUUID();
        UUID holder = listing.getOwner();
        String ownerName = listing.getOwnerName();
        String buyerName = buyer.getDisplayName();
        ItemContent typing = listing.getTyping();
        Currency currency = listing.getCurrency();
        double price = listing.getPrice();
        long creationDate = listing.getCreationDate();

        long buyDate = System.currentTimeMillis();
        long deletionDate = AuctionUtils.generatePurgeDate(buyDate);
        boolean isPaid = false;

        double tax = AuctionUtils.getClaimTax(buyer);
        if (tax > 0D) {
            price -= Math.max(0D, AuctionUtils.getTax(currency, price, tax));
        }

        return new CompletedListing(id, holder, ownerName, buyerName, typing, currency, price, creationDate, buyDate, deletionDate, isPaid);
    }

    public CompletedListing(
                            @NonNull UUID id,
                            @NonNull UUID owner,
                            @NonNull String ownerName,
                            @NonNull String buyerName,
                            @NonNull ItemContent typing,
                            @NonNull Currency currency,
                            double price,
                            long creationDate,
                            long buyDate,
                            long deletionDate,
                            boolean claimed
    ) {
        super(id, owner, ownerName, typing, currency, price, creationDate, deletionDate);
        this.setClaimed(claimed);
        this.buyerName = buyerName;
        this.buyDate = buyDate;
    }

    @Override
    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return ShopPlaceholders.forCompletedListing(this);
    }

    @NonNull
    public String getBuyerName() {
        return this.buyerName;
    }

    public long getBuyDate() {
        return buyDate;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }
}
