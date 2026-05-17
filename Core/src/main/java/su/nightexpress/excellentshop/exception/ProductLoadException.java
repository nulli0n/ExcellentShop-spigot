package su.nightexpress.excellentshop.exception;

import org.jspecify.annotations.NonNull;

public class ProductLoadException extends LoadException {

    public ProductLoadException(@NonNull String message) {
        this(false, message);
    }

    public ProductLoadException(boolean fatal, @NonNull String message) {
        super(fatal, message);
    }
}
