package su.nightexpress.excellentshop.api.transaction;

import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.api.BalanceHolder;
import su.nightexpress.excellentshop.api.product.Product;

public record ETransactionItem(@NonNull Product product, int units, @NonNull BalanceHolder price) {

}
