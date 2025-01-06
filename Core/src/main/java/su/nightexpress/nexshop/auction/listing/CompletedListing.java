package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.auction.AuctionUtils;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class CompletedListing extends AbstractListing {

    private final String buyerName;
    private final long   buyDate;

    private boolean claimed;

    @NotNull
    public static CompletedListing create(@NotNull ActiveListing listing, @NotNull Player buyer) {
        UUID id = UUID.randomUUID();
        UUID holder = listing.getOwner();
        String ownerName = listing.getOwnerName();
        String buyerName = buyer.getDisplayName();
        PhysicalTyping typing = listing.getTyping();
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
        @NotNull UUID id,
        @NotNull UUID owner,
        @NotNull String ownerName,
        @NotNull String buyerName,
        @NotNull PhysicalTyping typing,
        @NotNull Currency currency,
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
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.forCompletedListing(this);
    }

    @NotNull
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
