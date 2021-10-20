package su.nightexpress.nexshop.shop.auction.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public abstract class AbstractAuctionItem implements IPlaceholder {

    public static final String PLACEHOLDER_ITEM_NAME = "%listing_item_name%";
    public static final String PLACEHOLDER_ITEM_LORE   = "%listing_item_lore%";
    public static final String PLACEHOLDER_ITEM_AMOUNT = "%listing_item_amount%";
    public static final String PLACEHOLDER_SELLER      = "%listing_seller%";
    public static final String PLACEHOLDER_PRICE  = "%listing_price%";

    protected final UUID      id;
    protected       UUID      owner;
    protected       String    ownerName;
    protected       ItemStack itemStack;
    protected       double    price;

    public AbstractAuctionItem(
            @NotNull UUID id,
            @NotNull UUID owner,
            @NotNull String ownerName,
            @NotNull ItemStack itemStack,
            double price
    ) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.itemStack = new ItemStack(itemStack);
        this.price = price;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        ItemStack item = this.getItemStack();
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = meta == null ? new ArrayList<>() : meta.getLore();

        return str -> str
                .replace(PLACEHOLDER_SELLER, this.getOwnerName())
                .replace(PLACEHOLDER_PRICE, NumberUT.formatGroup(this.getPrice()))
                .replace(PLACEHOLDER_ITEM_AMOUNT, String.valueOf(item.getAmount()))
                .replace(PLACEHOLDER_ITEM_NAME, ItemUT.getItemName(item))
                .replace(PLACEHOLDER_ITEM_LORE, String.join("\n", itemLore == null ? Collections.emptyList() : itemLore))
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

    @NotNull
    public String getOwnerName() {
        return ownerName;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getPrice() {
        return price;
    }

    public boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.getOwner());
    }
}
