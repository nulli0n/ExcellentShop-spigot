package su.nightexpress.excellentshop.exception;

import org.jspecify.annotations.NonNull;

public class ModuleLoadException extends LoadException {

    public ModuleLoadException(@NonNull String message) {
        this(false, message);
    }

    public ModuleLoadException(boolean fatal, @NonNull String message) {
        super(fatal, message);
    }
}
