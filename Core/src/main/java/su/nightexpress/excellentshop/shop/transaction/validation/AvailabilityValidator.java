package su.nightexpress.excellentshop.shop.transaction.validation;

import java.util.List;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.shop.transaction.TransactionValidator;
import su.nightexpress.excellentshop.shop.transaction.ValidationResult;

@NullMarked
public class AvailabilityValidator implements TransactionValidator {

    @Override
    public ValidationResult validate(EPreparedTransaction transaction) {
        Player player = transaction.getPlayer();
        List<ETransactionItem> products = transaction.getItemsList();

        for (ETransactionItem transactionItem : products) {
            ValidationResult result = this.validateItem(player, transactionItem);
            if (result.result() != ETransactionResult.SUCCESS) return result;
        }

        return ValidationResult.success();
    }

    private ValidationResult validateItem(Player player, ETransactionItem item) {
        Product product = item.product();

        if (!product.canTrade(player)) {
            return new ValidationResult(ETransactionResult.NOT_AVAILABLE, item, Lang.SHOP_TRADE_PLAYER_FORBIDDEN);
        }

        return ValidationResult.success();
    }
}
