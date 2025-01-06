package su.nightexpress.nexshop.auction.listing;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;

import java.util.UUID;
import java.util.function.UnaryOperator;

public abstract class AbstractListing {

    protected final UUID           id;
    protected final UUID           owner;
    protected final String         ownerName;
    protected final PhysicalTyping typing;
    protected final Currency       currency;
    protected final double         price;

    protected final long creationDate;
    protected final long deletionDate;

    public AbstractListing(@NotNull UUID id,
                           @NotNull UUID owner,
                           @NotNull String ownerName,
                           @NotNull PhysicalTyping typing,
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

    @NotNull
    public abstract UnaryOperator<String> replacePlaceholders();

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
        return this.typing.getItem();
    }

    @NotNull
    public PhysicalTyping getTyping() {
        return this.typing;
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
