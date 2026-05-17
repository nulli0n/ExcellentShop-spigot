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

@NullMarked
public class EPreparedTransaction extends ETransaction {

    private final Map<Product, ETransactionItem> items;
    private final List<ETransactionItem>         looseItems;

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

    public ECompletedTransaction complete(ETransactionResult result) {
        return ECompletedTransaction.create(this, result, this.calculateWorth());
    }

    public BalanceHolder calculateWorth() {
        BalanceHolder holder = new BalanceHolder();

        this.items.values().stream().map(ETransactionItem::price).forEach(holder::storeAll);

        return holder;
    }

    public List<ETransactionItem> getItemsList() {
        return new ArrayList<>(this.items.values());
    }

    public Map<Product, ETransactionItem> getItems() {
        return this.items;
    }

    public List<ETransactionItem> getLooseItems() {
        return this.looseItems;
    }

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
