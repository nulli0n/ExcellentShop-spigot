package su.nightexpress.nexshop.exception;

import org.jetbrains.annotations.NotNull;

public abstract class LoadException extends RuntimeException {

    private final boolean fatal;

    public LoadException(boolean fatal, @NotNull String message) {
        super(message);
        this.fatal = false;
    }

    public boolean isFatal() {
        return this.fatal;
    }
}
