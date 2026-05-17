package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.nightcore.bridge.currency.Currency;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class ActiveListing extends AbstractListing {

    private final long expireDate;

    public static ActiveListing create(@NonNull Player player,
                                       @NonNull ItemContent typing,
                                       @NonNull Currency currency,
                                       double price) {
        UUID id = UUID.randomUUID();
        UUID holder = player.getUniqueId();
        String ownerName = player.getDisplayName();

        long creationDate = System.currentTimeMillis();
        long expirationDate = AuctionUtils.generateExpireDate(creationDate);
        long deletionDate = AuctionUtils.generatePurgeDate(expirationDate);

        return new ActiveListing(id, holder, ownerName, typing, currency, price, creationDate, expirationDate, deletionDate);
    }

    public ActiveListing(@NonNull UUID id,
                         @NonNull UUID owner,
                         @NonNull String ownerName,
                         @NonNull ItemContent typing,
                         @NonNull Currency currency,
                         double price,
                         long creationDate,
                         long expireDate,
                         long deletionDate) {
        super(id, owner, ownerName, typing, currency, price, creationDate, deletionDate);
        this.expireDate = expireDate;
    }

    @Override
    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return ShopPlaceholders.forActiveListing(this);
    }

    public long getExpireDate() {
        return expireDate;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.getExpireDate();
    }
}
