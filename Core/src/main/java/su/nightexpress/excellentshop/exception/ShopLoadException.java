package su.nightexpress.excellentshop.exception;

import org.jspecify.annotations.NonNull;

public class ShopLoadException extends LoadException {

    public ShopLoadException(@NonNull String message) {
        this(false, message);
    }

    public ShopLoadException(boolean fatal, @NonNull String message) {
        super(fatal, message);
    }
}
