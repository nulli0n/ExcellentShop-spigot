package su.nightexpress.excellentshop.shop.transaction.validation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import su.nightexpress.excellentshop.api.BalanceHolder;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.shop.transaction.TransactionValidator;
import su.nightexpress.excellentshop.shop.transaction.ValidationResult;

public class ShopBalanceValidator implements TransactionValidator {

    @Override
    public ValidationResult validate(EPreparedTransaction transaction) {
        if (!this.checkShopBalances(transaction)) {
            return new ValidationResult(ETransactionResult.OUT_OF_MONEY, null, Lang.SHOP_TRADE_SHOP_OUT_OF_FUNDS);
        }

        return ValidationResult.success();
    }

    private boolean checkShopBalances(EPreparedTransaction transaction) {
        // Player is buying, shop affordance check is not required
        if (transaction.getType() == TradeType.BUY) return true;

        // Group items by shop to process each shop's inventory together
        Map<Shop, List<ETransactionItem>> itemsByShop = transaction.getItemsList()
            .stream()
            .collect(Collectors.groupingBy(item -> item.product().getShop()));

        for (Map.Entry<Shop, List<ETransactionItem>> entry : itemsByShop.entrySet()) {
            Shop shop = entry.getKey();
            if (shop.isAdminShop()) {
                continue;
            }

            List<ETransactionItem> items = entry.getValue();
            BalanceHolder shopBalance = new BalanceHolder();

            // Load the shop's available balances for the involved currencies
            items.stream()
                .map(item -> item.product().getCurrency())
                .distinct()
                .forEach(currency -> shop.queryBalance(currency)
                    .ifPresent(amount -> shopBalance.store(currency, amount)));

            // If the shop has absolutely no balance data, it can't afford anything
            if (shopBalance.isEmpty()) {
                return false;
            }

            // Process each item against the shop's running balance
            for (ETransactionItem item : items) {
                if (!this.checkItemAffordance(transaction, item, shopBalance)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkItemAffordance(EPreparedTransaction transaction,
                                        ETransactionItem item,
                                        BalanceHolder shopBalance) {
        BalanceHolder itemPrice = item.price();

        if (shopBalance.beatsAll(itemPrice)) {
            // Shop can afford it. Deduct the price from the running balance 
            // so subsequent items are checked against the remaining funds.
            itemPrice.getBalanceMap().forEach(shopBalance::remove);
            return true;
        }

        // Shop cannot afford it.
        if (transaction.isStrict()) {
            return false; // Fast fail the whole transaction
        }

        // Non-strict: Strip the unaffordable item from the transaction
        transaction.getItems().remove(item.product());
        transaction.getLooseItems().add(item);

        return true;
    }
}
