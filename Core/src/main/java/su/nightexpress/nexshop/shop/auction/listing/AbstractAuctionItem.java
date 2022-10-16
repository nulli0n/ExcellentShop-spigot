package su.nightexpress.nexshop.shop.auction.listing;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.Placeholders;

import java.util.UUID;
import java.util.function.UnaryOperator;

public abstract class AbstractAuctionItem implements IPlaceholder {

    protected final UUID      id;
    protected       UUID      owner;
    protected       String    ownerName;
    protected       ItemStack itemStack;
    protected       double    price;
    protected final ICurrency currency;
    protected final long dateCreation;

    public AbstractAuctionItem(
            @NotNull UUID id,
            @NotNull UUID owner,
            @NotNull String ownerName,
            @NotNull ItemStack itemStack,
            ICurrency currency,
            double price,
            long dateCreation
    ) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.itemStack = new ItemStack(itemStack);
        this.currency = currency;
        this.price = price;
        this.dateCreation = dateCreation;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.LISTING_SELLER, this.getOwnerName())
            .replace(Placeholders.LISTING_PRICE, this.getCurrency().format(this.getPrice()))
            .replace(Placeholders.LISTING_DATE_CREATION, AuctionConfig.DATE_FORMAT.format(TimeUtil.getLocalDateTimeOf(this.getDateCreation())))
            .replace(Placeholders.LISTING_ITEM_AMOUNT, String.valueOf(this.getItemStack().getAmount()))
            .replace(Placeholders.LISTING_ITEM_NAME, ItemUtil.getItemName(this.getItemStack()))
            .replace(Placeholders.LISTING_ITEM_LORE, String.join("\n", ItemUtil.getLore(this.getItemStack())))
            .replace(Placeholders.LISTING_ITEM_VALUE, String.valueOf(ItemUtil.toBase64(this.getItemStack())))
            .replace(Placeholders.LISTING_DELETES_IN, TimeUtil.formatTimeLeft(this.getDeleteDate()))
            ;
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
        return itemStack;
    }

    @NotNull
    public ICurrency getCurrency() {
        return currency;
    }

    public double getPrice() {
        return price;
    }

    public boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.getOwner());
    }

    public long getDateCreation() {
        return dateCreation;
    }

    public abstract long getDeleteDate();
}
