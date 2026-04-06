package su.nightexpress.excellentshop.api.transaction;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.nexshop.util.BalanceHolder;

public record ETransactionItem(@NonNull Product product, int units, @NonNull BalanceHolder price) {

}
