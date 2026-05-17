package su.nightexpress.excellentshop.api.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.shop.Shop;

/**
 * A raw transaction containing unvalidated items and candidate shops.
 */
@NullMarked
public class ERawTransaction extends ETransaction {

    private final List<ItemStack> items;
    private final Set<Shop>       targetShops;

    /**
     * Constructs a new raw transaction.
     *
     * @param player      The player initiating the transaction.
     * @param type        The trade type.
     * @param options     The transaction options.
     * @param items       The raw list of item stacks to process.
     * @param targetShops The set of candidate shops.
     */
    public ERawTransaction(Player player,
                           TradeType type,
                           ETransactionOptions options,
                           List<ItemStack> items,
                           Set<Shop> targetShops) {
        super(player, type, options);
        this.items = items;
        this.targetShops = targetShops;
    }


    public static Builder builder(Player player, TradeType type) {
        return new Builder(player, type);
    }

    /**
     * Checks whether there are any candidate shops targeted for this transaction.
     *
     * @return True if target shops are present, false otherwise.
     */
    public boolean hasTargetShops() {
        return !this.targetShops.isEmpty();
    }

    /**
     * Gets the raw list of item stacks in this transaction.
     *
     * @return The list of items.
     */
    public List<ItemStack> getItems() {
        return this.items;
    }

    /**
     * Gets the set of targeted candidate shops.
     *
     * @return The set of target shops.
     */
    public Set<Shop> getTargetShops() {
        return this.targetShops;
    }

    /**
     * Builder class for raw transactions.
     */
    @NullMarked
    public static class Builder extends ETransaction.Builder<Builder, ERawTransaction> {

        private final List<ItemStack> items;
        private final Set<Shop>       targetShops;

        Builder(Player player, TradeType type) {
            super(player, type);
            this.items = new ArrayList<>();
            this.targetShops = new HashSet<>();
        }

        @Override
        protected ERawTransaction build(ETransactionOptions options) {
            return new ERawTransaction(this.player, this.type, options, this.items, this.targetShops);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder addItem(ItemStack itemStack) {
            this.items.add(new ItemStack(itemStack));
            return this;
        }

        public Builder addItems(Collection<ItemStack> items) {
            items.forEach(this::addItem);
            return this;
        }

        public Builder addItems(@Nullable ItemStack[] items) {
            for (ItemStack itemStack : items) {
                if (itemStack != null) {
                    this.addItem(itemStack);
                }
            }
            return this;
        }

        public Builder addItems(Inventory inventory) {
            return this.addItems(inventory.getContents());
        }

        public Builder targetShop(Shop shop) {
            this.targetShops.add(shop);
            return this;
        }

        public Builder targetShops(Collection<? extends Shop> shops) {
            this.targetShops.addAll(shops);
            return this;
        }
    }
}
