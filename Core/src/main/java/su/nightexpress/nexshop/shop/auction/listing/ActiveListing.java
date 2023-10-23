package su.nightexpress.nexshop.shop.auction.listing;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.auction.Placeholders;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;

import java.util.UUID;

public class ActiveListing extends AbstractListing {

    private final long expireDate;

    public ActiveListing(
        @NotNull Player player,
        @NotNull ItemStack itemStack,
        @NotNull Currency currency,
        double price
    ) {
        this(
            UUID.randomUUID(),
            player.getUniqueId(),
            player.getDisplayName(),
            itemStack,
            currency,
            price,
            System.currentTimeMillis(),
            System.currentTimeMillis() + AuctionConfig.LISTINGS_EXPIRE_IN
        );
    }

    public ActiveListing(
            @NotNull UUID id,
            @NotNull UUID owner,
            @NotNull String ownerName,
            @NotNull ItemStack itemStack,
            @NotNull Currency currency,
            double price,
            long dateCreation,
            long expireDate
    ) {
        super(id, owner, ownerName, itemStack, currency, price, dateCreation);
        this.expireDate = expireDate;
        this.placeholderMap
            .add(Placeholders.LISTING_EXPIRES_IN, () -> TimeUtil.formatTimeLeft(this.getExpireDate()))
            .add(Placeholders.LISTING_EXPIRE_DATE, AuctionConfig.DATE_FORMAT.format(TimeUtil.getLocalDateTimeOf(this.getExpireDate())))
            ;
    }

    public long getExpireDate() {
        return expireDate;
    }

    public long getDeleteDate() {
        return this.getExpireDate() + AuctionConfig.LISTINGS_PURGE_IN;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.getExpireDate();
    }

    /*public boolean isValid() {
        return System.currentTimeMillis() <= this.getDeleteDate();
    }*/
}
