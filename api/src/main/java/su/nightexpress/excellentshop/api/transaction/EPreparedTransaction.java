package su.nightexpress.excellentshop.api.transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.BalanceHolder;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.nightcore.bridge.currency.Currency;

/**
 * A prepared transaction that has been validated and is ready to be processed by a TransactionEngine.
 */
@NullMarked
public class EPreparedTransaction extends ETransaction {

    private final Map<Product, ETransactionItem> items;
    private final List<ETransactionItem>         looseItems;

    /**
     * Constructs a prepared transaction.
     *
     * @param player  The player involved.
     * @param type    The trade type.
     * @param options The transaction options.
     * @param items   The mapped items ready to be processed.
     */
    public EPreparedTransaction(Player player,
                                TradeType type,
                                ETransactionOptions options,
                                Map<Product, ETransactionItem> items) {
        super(player, type, options);
        this.items = items;
        this.looseItems = new ArrayList<>();
    }

    public static Builder builder(Player player, TradeType type) {
        return new Builder(player, type);
    }

    /**
     * Checks whether the transaction has any items to proceed with.
     *
     * @return True if the item map is not empty, false otherwise.
     */
    public boolean hasItems() {
        return !this.items.isEmpty();
    }

    /**
     * Calculates the total worth (price) of all items in this transaction.
     *
     * @return A BalanceHolder containing the total worth.
     */
    public BalanceHolder calculateWorth() {
        BalanceHolder holder = new BalanceHolder();

        this.items.values().stream().map(ETransactionItem::price).forEach(holder::storeAll);

        return holder;
    }

    /**
     * Gets a newly created list containing all prepared items.
     *
     * @return A list copy of the items.
     */
    public List<ETransactionItem> getItemsList() {
        return new ArrayList<>(this.items.values());
    }

    /**
     * Gets the items that were processed successfully and are ready for the transaction.
     * This map is modifiable.
     *
     * @return A map of products to their transaction items.
     */
    public Map<Product, ETransactionItem> getItems() {
        return this.items;
    }

    /**
     * Gets the items that cannot be processed for whatever reason.
     * This list is modifiable.
     *
     * @return A list of loose transaction items.
     */
    public List<ETransactionItem> getLooseItems() {
        return this.looseItems;
    }

    /**
     * Builder class for prepared transactions.
     */
    @NullMarked
    public static class Builder extends ETransaction.Builder<Builder, EPreparedTransaction> {

        private final Map<Product, ETransactionItem> products;

        public Builder(Player player, TradeType type) {
            super(player, type);
            this.products = new LinkedHashMap<>();
        }

        @Override
        protected EPreparedTransaction build(ETransactionOptions options) {
            return new EPreparedTransaction(this.player, this.type, options, this.products);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Adds a product to the transaction. If the product already exists,
         * the units are added to the existing amount.
         *
         * @param product The product to add.
         * @param units   The amount of units.
         * @return This builder instance.
         */
        public Builder addProduct(Product product, int units) {
            Currency currency = product.getCurrency();
            ETransactionItem previous = this.products.get(product);

            if (previous != null) {
                units += previous.units();
            }

            BalanceHolder balanceHolder = new BalanceHolder();
            balanceHolder.store(currency, product.getFinalPrice(this.type, units, this.player));

            this.products.put(product, new ETransactionItem(product, units, balanceHolder));
            return this;
        }
    }
}
