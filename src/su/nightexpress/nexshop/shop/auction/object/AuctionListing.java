package su.nightexpress.nexshop.shop.auction.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.shop.auction.AuctionConfig;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class AuctionListing extends AbstractAuctionItem {

    public static final String PLACEHOLDER_EXPIRES_IN = "%listing_expires_in%";
    public static final String PLACEHOLDER_DELETES_IN = "%listing_deletes_in%";

    private final long expireDate;
    private final long deleteDate;

    public AuctionListing(
            @NotNull Player player,
            @NotNull ItemStack itemStack,
            double price
    ) {
        this(
                UUID.randomUUID(),
                player.getUniqueId(),
                player.getDisplayName(),
                itemStack,
                price,
                System.currentTimeMillis() + AuctionConfig.STORAGE_EXPIRE_IN,
                System.currentTimeMillis() + AuctionConfig.STORAGE_EXPIRE_IN + AuctionConfig.STORAGE_DELETE_EXPIRED
        );
    }

    public AuctionListing(
            @NotNull UUID id,
            @NotNull UUID owner,
            @NotNull String ownerName,
            @NotNull ItemStack itemStack,
            double price,
            long expireDate,
            long deleteDate
    ) {
        super(id, owner, ownerName, itemStack, price);
        this.expireDate = expireDate;
        this.deleteDate = deleteDate;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_EXPIRES_IN, TimeUT.formatTimeLeft(this.getExpireDate()))
                .replace(PLACEHOLDER_DELETES_IN, TimeUT.formatTimeLeft(this.getDeleteDate()))
        );
    }

    public long getExpireDate() {
        return expireDate;
    }

    public long getDeleteDate() {
        return deleteDate;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.getExpireDate();
    }

    public boolean isValid() {
        return System.currentTimeMillis() <= this.getDeleteDate();
    }
}
