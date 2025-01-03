package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.Placeholders;

import java.util.UUID;

public class ActiveListing extends AbstractListing {

    private final long expireDate;

    public static ActiveListing create(@NotNull Player player,
                                       //@NotNull ItemStack itemStack,
                                       @NotNull ItemHandler handler,
                                       @NotNull ItemPacker packer,
                                       @NotNull Currency currency,
                                       double price) {
        UUID id = UUID.randomUUID();
        UUID holder = player.getUniqueId();
        String ownerName = player.getDisplayName();
        //ItemStack copyStack = new ItemStack(itemStack);

        long creationDate = System.currentTimeMillis();
        long expirationDate = AuctionUtils.generateExpireDate(creationDate);
        long deletionDate = AuctionUtils.generatePurgeDate(expirationDate);

        return new ActiveListing(id, holder, ownerName, handler, packer, currency, price, creationDate, expirationDate, deletionDate);
    }

    public ActiveListing(
            @NotNull UUID id,
            @NotNull UUID owner,
            @NotNull String ownerName,
            //@NotNull ItemStack itemStack,
            @NotNull ItemHandler handler,
            @NotNull ItemPacker packer,
            @NotNull Currency currency,
            double price,
            long creationDate,
            long expireDate,
            long deletionDate
    ) {
        super(id, owner, ownerName, handler, packer, currency, price, creationDate, deletionDate);
        this.expireDate = expireDate;
        this.placeholderMap.add(Placeholders.forActiveListing(this));
    }

    public long getExpireDate() {
        return expireDate;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.getExpireDate();
    }
}
