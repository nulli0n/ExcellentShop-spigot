package su.nightexpress.excellentshop.module;

import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.api.ModuleDefinition;

import java.nio.file.Path;

@FunctionalInterface
public interface ModuleContextProvider {

    @NonNull
    ModuleContext createModuleContext(@NonNull String id, @NonNull Path path, @NonNull ModuleDefinition definition);
}
