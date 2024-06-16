package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.auction.Placeholders;
import su.nightexpress.nightcore.util.placeholder.Placeholder;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.UUID;

public abstract class AbstractListing implements Placeholder {

    protected final UUID      id;
    protected final UUID      owner;
    protected final String    ownerName;
    protected final ItemStack itemStack;
    protected final Currency  currency;
    protected final double    price;

    protected final long creationDate;
    protected final long deletionDate;

    protected final PlaceholderMap placeholderMap;

    public AbstractListing(
        @NotNull UUID id,
        @NotNull UUID owner,
        @NotNull String ownerName,
        @NotNull ItemStack itemStack,
        Currency currency,
        double price,
        long creationDate,
        long deletionDate
    ) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.itemStack = itemStack;
        this.currency = currency;
        this.price = price;
        this.creationDate = creationDate;
        this.deletionDate = deletionDate;

        this.placeholderMap = Placeholders.forListing(this);
    }

    public boolean isDeletionTime() {
        return this.getDeleteDate() >= 0 && System.currentTimeMillis() >= this.getDeleteDate();
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    public final UUID getId() {
        return id;
    }

    @NotNull
    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(@NotNull UUID uuid) {
        return this.getOwner().equals(uuid);
    }

    @NotNull
    public String getOwnerName() {
        return ownerName;
    }

    @NotNull
    public ItemStack getItemStack() {
        return new ItemStack(this.itemStack);
    }

    @NotNull
    public Currency getCurrency() {
        return currency;
    }

    public double getPrice() {
        return price;
    }

    public boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.getOwner());
    }

    public long getCreationDate() {
        return creationDate;
    }

    public final long getDeleteDate() {
        return this.deletionDate;
    }
}
