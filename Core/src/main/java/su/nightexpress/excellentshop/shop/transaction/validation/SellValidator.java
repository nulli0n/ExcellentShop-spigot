package su.nightexpress.excellentshop.shop.transaction.validation;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.shop.transaction.TransactionValidator;
import su.nightexpress.excellentshop.shop.transaction.ValidationResult;

public class SellValidator implements TransactionValidator {

    @Override
    public ValidationResult validate(EPreparedTransaction transaction) {
        Player player = transaction.getPlayer();
        Inventory inventory = transaction.getUserInventory();
        List<ETransactionItem> products = transaction.getItemsList();

        for (ETransactionItem transactionItem : products) {
            ValidationResult result = this.validateItem(player, inventory, transactionItem);
            if (result.result() != ETransactionResult.SUCCESS) return result;
        }

        return ValidationResult.success();
    }

    private ValidationResult validateItem(Player player, Inventory inventory, ETransactionItem item) {
        Product product = item.product();
        int units = item.units();

        if (/* !product.canTrade(player) ||  */!product.isSellable()) {
            return new ValidationResult(ETransactionResult.NOT_AVAILABLE, item, Lang.SHOP_TRADE_PRODUCT_UNSELLABLE);
        }

        if (product.countUnits(inventory) < units) {
            return new ValidationResult(ETransactionResult.NOT_ENOUGH_ITEMS, item, Lang.SHOP_TRADE_PLAYER_NOT_ENOUGH_ITEMS);
        }

        int spaceUnits = product.getSpace();
        if (spaceUnits >= 0 && spaceUnits < units) {
            return new ValidationResult(ETransactionResult.OUT_OF_SHOP_SPACE, item, Lang.SHOP_TRADE_PRODUCT_OUT_OF_SPACE);
        }

        int tradeLimit = product.getTradeLimit(TradeType.SELL);
        if (tradeLimit >= 0) {
            int trades = product.getLimitData(player).getTrades(TradeType.SELL);
            int left = tradeLimit - trades;
            if (left < units) {
                return new ValidationResult(ETransactionResult.LIMIT_REACHED, item, Lang.SHOP_TRADE_PLAYER_OUT_OF_LIMIT);
            }
        }

        return ValidationResult.success();
    }
}
