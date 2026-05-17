package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.nightcore.bridge.currency.Currency;

import java.util.UUID;
import java.util.function.UnaryOperator;

public abstract class AbstractListing {

    protected final UUID        id;
    protected final UUID        owner;
    protected final String      ownerName;
    protected final ItemContent typing;
    protected final Currency    currency;
    protected final double      price;

    protected final long creationDate;
    protected final long deletionDate;

    public AbstractListing(@NonNull UUID id,
                           @NonNull UUID owner,
                           @NonNull String ownerName,
                           @NonNull ItemContent typing,
                           Currency currency,
                           double price,
                           long creationDate,
                           long deletionDate) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.typing = typing;
        this.currency = currency;
        this.price = price;
        this.creationDate = creationDate;
        this.deletionDate = deletionDate;
    }

    public boolean isDeletionTime() {
        return this.getDeleteDate() >= 0 && System.currentTimeMillis() >= this.getDeleteDate();
    }

    @NonNull
    public abstract UnaryOperator<String> replacePlaceholders();

    @NonNull
    public final UUID getId() {
        return id;
    }

    @NonNull
    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(@NonNull UUID uuid) {
        return this.getOwner().equals(uuid);
    }

    @NonNull
    public String getOwnerName() {
        return ownerName;
    }

    public boolean isValid() {
        return this.typing.isValid();
    }

    @NonNull
    public ItemStack getItemStack() {
        return this.typing.getItem();
    }

    @NonNull
    public ItemContent getTyping() {
        return this.typing;
    }

    @NonNull
    public Currency getCurrency() {
        return currency;
    }

    public double getPrice() {
        return price;
    }

    public boolean isOwner(@NonNull Player player) {
        return player.getUniqueId().equals(this.getOwner());
    }

    public long getCreationDate() {
        return creationDate;
    }

    public final long getDeleteDate() {
        return this.deletionDate;
    }
}
