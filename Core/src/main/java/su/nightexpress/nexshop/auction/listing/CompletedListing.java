package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.Placeholders;
import su.nightexpress.nexshop.auction.config.AuctionConfig;

import java.util.UUID;

public class CompletedListing extends AbstractListing {

    private final String buyerName;
    private boolean      isRewarded;
    private final long   buyDate;

    public CompletedListing(@NotNull ActiveListing listing, @NotNull Player buyer) {
        this(
                UUID.randomUUID(),
                listing.getOwner(),
                listing.getOwnerName(),
                buyer.getDisplayName(),
                listing.getItemStack(),
                listing.getCurrency(),
                listing.getPrice(),
                listing.getDateCreation(),
                false,
                System.currentTimeMillis()
        );

        double tax = buyer.hasPermission(Perms.AUCTION_BYPASS_LISTING_TAX) ? 0D : AuctionConfig.LISTINGS_TAX_ON_LISTING_PURCHASE;
        if (tax > 0D) {
            this.price -= Math.max(0D, AuctionUtils.calculateTax(this.getPrice(), tax));
        }
    }

    public CompletedListing(
        @NotNull UUID id,
        @NotNull UUID owner,
        @NotNull String ownerName,
        @NotNull String buyerName,
        @NotNull ItemStack itemStack,
        @NotNull Currency currency,
            double price,
        long dateCreation,
        boolean isRewarded,
        long buyDate
    ) {
        super(id, owner, ownerName, itemStack, currency, price, dateCreation);
        this.setRewarded(isRewarded);
        this.buyerName = buyerName;
        this.buyDate = buyDate;
        this.placeholderMap
            .add(Placeholders.LISTING_BUYER, this.getBuyerName())
            .add(Placeholders.LISTING_BUY_DATE, AuctionConfig.DATE_FORMAT.format(TimeUtil.getLocalDateTimeOf(this.getBuyDate())))
            ;
    }

    @NotNull
    public String getBuyerName() {
        return this.buyerName;
    }

    public long getBuyDate() {
        return buyDate;
    }

    public boolean isRewarded() {
        return isRewarded;
    }

    public void setRewarded(boolean rewarded) {
        isRewarded = rewarded;
    }

    @Override
    public long getDeleteDate() {
        return this.getBuyDate() + AuctionConfig.LISTINGS_PURGE_IN;
    }
}
