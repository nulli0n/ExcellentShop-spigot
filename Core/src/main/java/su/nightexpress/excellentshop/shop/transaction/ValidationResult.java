package su.nightexpress.excellentshop.shop.transaction;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.nightcore.locale.entry.MessageLocale;

@NullMarked
public record ValidationResult(ETransactionResult result,
                               @Nullable ETransactionItem cause,
                               @Nullable MessageLocale errorMessage) {

    public static ValidationResult success() {
        return new ValidationResult(ETransactionResult.SUCCESS, null, null);
    }
}
