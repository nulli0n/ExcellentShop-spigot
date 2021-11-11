package su.nightexpress.nexshop.shop.auction.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.shop.auction.AuctionConfig;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class AuctionHistoryItem extends AbstractAuctionItem {

    public static final String PLACEHOLDER_BUYER    = "%listing_buyer%";
    public static final String PLACEHOLDER_BUY_DATE = "%listing_buy_date%";

    private final String  buyerName;
    private boolean isNotified;
    private final long buyDate;
    private final long deleteDate;

    public AuctionHistoryItem(@NotNull AuctionListing listing, @NotNull Player buyer) {
        this(
                UUID.randomUUID(),
                listing.getOwner(),
                listing.getOwnerName(),
                buyer.getDisplayName(),
                listing.getItemStack(),
                listing.getPrice(),
                false,
                System.currentTimeMillis(),
                System.currentTimeMillis() + AuctionConfig.STORAGE_SALES_HISTORY
        );
    }

    public AuctionHistoryItem(
            @NotNull UUID id,
            @NotNull UUID owner,
            @NotNull String ownerName,
            @NotNull String buyerName,
            @NotNull ItemStack itemStack,
            double price,
            boolean isNotified,
            long buyDate,
            long deleteDate
    ) {
        super(id, owner, ownerName, itemStack, price);
        this.setNotified(isNotified);
        this.buyerName = buyerName;
        this.buyDate = buyDate;
        this.deleteDate = deleteDate;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_BUYER, this.getBuyerName())
                .replace(PLACEHOLDER_BUY_DATE, AuctionConfig.DATE_FORMAT.format(TimeUT.getLocalDateTimeOf(this.getBuyDate())))
        );
    }

    @NotNull
    public String getBuyerName() {
        return this.buyerName;
    }

    public long getBuyDate() {
        return buyDate;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    public long getDeleteDate() {
        return this.deleteDate;
    }

    public boolean isValid() {
        return System.currentTimeMillis() <= this.getDeleteDate();
    }
}
