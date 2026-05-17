package su.nightexpress.excellentshop.exception;

import org.jspecify.annotations.NonNull;

public abstract class LoadException extends RuntimeException {

    private final boolean fatal;

    public LoadException(boolean fatal, @NonNull String message) {
        super(message);
        this.fatal = false;
    }

    public boolean isFatal() {
        return this.fatal;
    }
}
