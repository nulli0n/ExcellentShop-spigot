package su.nightexpress.nexshop.exception;

import org.jetbrains.annotations.NotNull;

public class ShopLoadException extends LoadException {

    public ShopLoadException(@NotNull String message) {
        this(false, message);
    }

    public ShopLoadException(boolean fatal, @NotNull String message) {
        super(fatal, message);
    }
}
