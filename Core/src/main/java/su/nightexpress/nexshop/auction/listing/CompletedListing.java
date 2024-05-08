package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.Placeholders;

import java.util.UUID;

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
        ItemStack itemStack = listing.getItemStack();
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

        return new CompletedListing(id, holder, ownerName, buyerName, itemStack, currency, price, creationDate, buyDate, deletionDate, isPaid);
    }

    public CompletedListing(
        @NotNull UUID id,
        @NotNull UUID owner,
        @NotNull String ownerName,
        @NotNull String buyerName,
        @NotNull ItemStack itemStack,
        @NotNull Currency currency,
        double price,
        long creationDate,
        long buyDate,
        long deletionDate,
        boolean claimed
    ) {
        super(id, owner, ownerName, itemStack, currency, price, creationDate, deletionDate);
        this.setClaimed(claimed);
        this.buyerName = buyerName;
        this.buyDate = buyDate;

        this.placeholderMap.add(Placeholders.forComletedListing(this));
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
