package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@FunctionalInterface
public interface ModuleContextProvider {

    @NotNull ModuleContext createModuleContext(@NotNull String id, @NotNull Path path, @NotNull ModuleDefinition definition);
}
