package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nightcore.bridge.currency.Currency;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class ActiveListing extends AbstractListing {

    private final long expireDate;

    public static ActiveListing create(@NotNull Player player,
                                       @NotNull ItemContent typing,
                                       @NotNull Currency currency,
                                       double price) {
        UUID id = UUID.randomUUID();
        UUID holder = player.getUniqueId();
        String ownerName = player.getDisplayName();

        long creationDate = System.currentTimeMillis();
        long expirationDate = AuctionUtils.generateExpireDate(creationDate);
        long deletionDate = AuctionUtils.generatePurgeDate(expirationDate);

        return new ActiveListing(id, holder, ownerName, typing, currency, price, creationDate, expirationDate, deletionDate);
    }

    public ActiveListing(@NotNull UUID id,
                         @NotNull UUID owner,
                         @NotNull String ownerName,
                         @NotNull ItemContent typing,
                         @NotNull Currency currency,
                         double price,
                         long creationDate,
                         long expireDate,
                         long deletionDate) {
        super(id, owner, ownerName, typing, currency, price, creationDate, deletionDate);
        this.expireDate = expireDate;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.forActiveListing(this);
    }

    public long getExpireDate() {
        return expireDate;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.getExpireDate();
    }
}
