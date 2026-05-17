package su.nightexpress.excellentshop.shop.transaction.validation;

import static su.nightexpress.excellentshop.api.product.TradeType.BUY;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.UnitUtils;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.shop.transaction.TransactionValidator;
import su.nightexpress.excellentshop.shop.transaction.ValidationResult;

@NullMarked
public class BuyValidator implements TransactionValidator {

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

        if (/* !product.canTrade(player) ||  */!product.isBuyable()) {
            return new ValidationResult(ETransactionResult.NOT_AVAILABLE, item, Lang.SHOP_TRADE_PRODUCT_UNBUYABLE);
        }

        int stockUnits = product.getStock();
        if (stockUnits >= 0 && stockUnits < units) {
            return new ValidationResult(ETransactionResult.OUT_OF_STOCK, item, Lang.SHOP_TRADE_PRODUCT_OUT_OF_STOCK);
        }

        int inventorySpace = product.countSpace(inventory);
        if (inventorySpace >= 0 && inventorySpace < UnitUtils.unitsToAmount(product, units)) {
            return new ValidationResult(ETransactionResult.OUT_OF_INVENTORY_SPACE, item, Lang.SHOP_TRADE_PLAYER_FULL_INVENTORY);
        }

        int maxAffordable = product.getMaxAffordableUnitAmount(player);
        if (maxAffordable >= 0 && maxAffordable < units) {
            return new ValidationResult(ETransactionResult.TOO_EXPENSIVE, item, Lang.SHOP_PURCHASE_PLAYER_OUT_OF_MONEY);
        }

        int tradeLimit = product.getTradeLimit(BUY);
        if (tradeLimit >= 0) {
            int trades = product.getLimitData(player).getTrades(BUY);
            int left = tradeLimit - trades;
            if (left < units) {
                return new ValidationResult(ETransactionResult.LIMIT_REACHED, item, Lang.SHOP_TRADE_PLAYER_OUT_OF_LIMIT);
            }
        }

        return ValidationResult.success();
    }
}
