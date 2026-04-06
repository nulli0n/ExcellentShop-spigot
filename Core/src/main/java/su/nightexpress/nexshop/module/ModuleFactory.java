package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.api.Module;

@FunctionalInterface
public interface ModuleFactory<T extends Module> {

    @NotNull T load(@NotNull ModuleContext context);
}