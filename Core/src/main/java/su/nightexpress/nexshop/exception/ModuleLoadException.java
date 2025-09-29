package su.nightexpress.nexshop.exception;

import org.jetbrains.annotations.NotNull;

public class ModuleLoadException extends LoadException {

    public ModuleLoadException(@NotNull String message) {
        this(false, message);
    }

    public ModuleLoadException(boolean fatal, @NotNull String message) {
        super(fatal, message);
    }
}
