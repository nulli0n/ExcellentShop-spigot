package su.nightexpress.excellentshop.shop.transaction;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;

@NullMarked
@FunctionalInterface
public interface TransactionCallback {

    void accept(ECompletedTransaction completedTransaction);
}
