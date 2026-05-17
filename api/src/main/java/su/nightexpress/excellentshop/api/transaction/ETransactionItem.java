package su.nightexpress.excellentshop.api.transaction;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.BalanceHolder;
import su.nightexpress.excellentshop.api.product.Product;

/**
 * A transaction item that holds the Product instance, units amount, and price.
 *
 * @param product The product involved in the transaction.
 * @param units   The amount of units of the product.
 * @param price   The total calculated price for these units.
 */
@NullMarked
public record ETransactionItem(Product product, int units, BalanceHolder price) {

}
