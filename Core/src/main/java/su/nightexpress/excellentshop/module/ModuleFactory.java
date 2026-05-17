package su.nightexpress.excellentshop.module;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.Module;

@FunctionalInterface
public interface ModuleFactory<T extends Module> {

    @NonNull
    T load(@NonNull ModuleContext context);
}