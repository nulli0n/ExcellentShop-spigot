package su.nightexpress.excellentshop.shop.transaction;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;

@NullMarked
public interface TransactionValidator {

    ValidationResult validate(EPreparedTransaction transaction);
}
