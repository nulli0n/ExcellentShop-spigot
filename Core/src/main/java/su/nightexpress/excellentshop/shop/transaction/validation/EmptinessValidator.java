package su.nightexpress.excellentshop.shop.transaction.validation;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.BalanceHolder;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.shop.transaction.TransactionValidator;
import su.nightexpress.excellentshop.shop.transaction.ValidationResult;
import su.nightexpress.nightcore.bridge.currency.Currency;

@NullMarked
public class EmptinessValidator implements TransactionValidator {

    @Override
    public ValidationResult validate(EPreparedTransaction transaction) {
        if (!transaction.hasItems()) {
            return new ValidationResult(ETransactionResult.FAILURE, null, Lang.SHOP_TRADE_FEEDBACK_EMPTY);
        }

        if (!transaction.isStrict()) {
            this.removeUnaffordableItems(transaction);

            if (!transaction.hasItems()) {
                return new ValidationResult(ETransactionResult.FAILURE, null, Lang.SHOP_TRADE_FEEDBACK_LOOSE_ITEMS);
            }
        }

        return ValidationResult.success();
    }

    private void removeUnaffordableItems(EPreparedTransaction transaction) {
        if (transaction.isStrict()) return;

        Player player = transaction.getPlayer();
        TradeType tradeType = transaction.getType();
        Inventory inventory = transaction.getUserInventory();

        transaction.getItemsList().forEach(transactionItem -> {
            Product product = transactionItem.product();
            int units = transactionItem.units();

            int maxUnits = switch (tradeType) {
                case BUY -> product.getMaxBuyableUnitAmount(player, inventory);
                case SELL -> product.getMaxSellableUnitAmount(player, inventory);
            };

            if (maxUnits == 0) {
                transaction.getLooseItems().add(transactionItem);
                transaction.getItems().remove(product);
                return;
            }

            if (maxUnits < 0 || maxUnits >= units) return;

            int loseUnits = units - maxUnits;
            Currency currency = product.getCurrency();

            BalanceHolder loseWorth = new BalanceHolder();
            BalanceHolder maxWorth = new BalanceHolder();

            loseWorth.store(currency, product.getFinalSellPrice(player, loseUnits));
            maxWorth.store(currency, product.getFinalSellPrice(player, maxUnits));

            transaction.getLooseItems().add(new ETransactionItem(product, loseUnits, loseWorth));
            transaction.getItems().put(product, new ETransactionItem(product, maxUnits, maxWorth));
        });
    }
}
