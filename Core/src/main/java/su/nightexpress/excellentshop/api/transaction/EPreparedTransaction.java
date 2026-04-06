package su.nightexpress.excellentshop.api.transaction;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.nexshop.util.BalanceHolder;
import su.nightexpress.nightcore.bridge.currency.Currency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EPreparedTransaction extends ETransaction {

    private final Map<Product, ETransactionItem> items;
    private final List<ETransactionItem>         looseItems;

    public EPreparedTransaction(@NonNull Player player,
                                @NonNull TradeType type,
                                @NonNull ETransactionOptions options,
                                @NonNull Map<Product, ETransactionItem> items) {
        super(player, type, options);
        this.items = items;
        this.looseItems = new ArrayList<>();
    }

    @NonNull
    public static Builder builder(@NonNull Player player, @NonNull TradeType type) {
        return new Builder(player, type);
    }

    @NonNull
    public ECompletedTransaction complete(@NonNull ETransactionResult result) {
        return ECompletedTransaction.create(this, result, this.calculateWorth());
    }

    @NonNull
    public BalanceHolder calculateWorth() {
        BalanceHolder holder = new BalanceHolder();

        this.items.values().stream().map(ETransactionItem::price).forEach(holder::storeAll);

        return holder;
    }

    @NonNull
    public List<ETransactionItem> getItemsList() {
        return new ArrayList<>(this.items.values());
    }

    @NonNull
    public Map<Product, ETransactionItem> getItems() {
        return this.items;
    }

    @NonNull
    public List<ETransactionItem> getLooseItems() {
        return this.looseItems;
    }

    public static class Builder extends ETransaction.Builder<Builder, EPreparedTransaction> {

        private final Map<Product, ETransactionItem> products;

        public Builder(@NonNull Player player, @NonNull TradeType type) {
            super(player, type);
            this.products = new LinkedHashMap<>();
        }

        @Override
        @NonNull
        protected EPreparedTransaction build(@NonNull ETransactionOptions options) {
            return new EPreparedTransaction(this.player, this.type, options, this.products);
        }

        @Override
        @NonNull
        protected Builder getThis() {
            return this;
        }

        @NonNull
        public Builder addProduct(@NonNull Product product, int units) {
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
