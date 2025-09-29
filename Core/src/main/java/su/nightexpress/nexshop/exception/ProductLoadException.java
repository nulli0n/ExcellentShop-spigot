package su.nightexpress.nexshop.exception;

import org.jetbrains.annotations.NotNull;

public class ProductLoadException extends LoadException {

    public ProductLoadException(@NotNull String message) {
        this(false, message);
    }

    public ProductLoadException(boolean fatal, @NotNull String message) {
        super(fatal, message);
    }
}
