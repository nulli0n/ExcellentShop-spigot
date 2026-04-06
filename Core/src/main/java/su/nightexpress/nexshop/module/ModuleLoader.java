package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.ShopFiles;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.Module;
import su.nightexpress.nexshop.exception.ModuleLoadException;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModuleLoader {

    private final ShopPlugin     plugin;
    private final ModuleRegistry moduleRegistry;

    private final Map<String, ModuleFactory<?>> registrationMap;
    private final Map<String, ModuleDefinition>   definitionMap;

    public ModuleLoader(@NotNull ShopPlugin plugin, @NotNull ModuleRegistry moduleRegistry) {
        this.plugin = plugin;
        this.moduleRegistry = moduleRegistry;

        this.registrationMap = new HashMap<>();
        this.definitionMap = new LinkedHashMap<>();
    }

    public <T extends Module> void register(@NotNull String id, @NotNull ModuleDefinition definition, @NotNull ModuleFactory<T> factory) {
        this.definitionMap.put(id, definition);
        this.registrationMap.put(id, factory);
    }

    public void loadAll() {
        FileConfig config = FileConfig.load(this.plugin.getDataFolder().getAbsolutePath(), ShopFiles.FILE_MODULES);
        FileConfig pluginConfig = this.plugin.getConfig();

        this.definitionMap.forEach((id, defaultDefinition) -> {
            // ========== MIGRATION FROM THE CONFIG.YML - START ==========
            if (pluginConfig.contains("Modules." + id)) {
                ModuleDefinition oldValue = pluginConfig.get(ModuleDefinition.CONFIG_TYPE, "Modules." + id, defaultDefinition);

                config.set("Modules." + id, oldValue);
                pluginConfig.remove("Modules." + id);
            }
            // ========== MIGRATION FROM THE CONFIG.YML - END ==========

            ModuleDefinition definition = config.get(ModuleDefinition.CONFIG_TYPE, "Modules." + id, defaultDefinition);

            try {
                this.loadModule(id, definition);
            }
            catch (ModuleLoadException exception) {
                this.plugin.error("Fatal error when trying to load module '%s': %s".formatted(id, exception.getMessage()));
            }
        });

        config.saveChanges();

        pluginConfig.remove("Modules");
        pluginConfig.saveChanges();
    }

    private boolean loadModule(@NotNull String id, @NotNull ModuleDefinition definition) throws ModuleLoadException {
        if (!definition.isEnabled()) return false;

        ModuleFactory<?> registration = this.registrationMap.remove(id);
        if (registration == null) {
            throw new ModuleLoadException("No loader present");
        }

        if (this.moduleRegistry.isPresent(id)) {
            throw new ModuleLoadException("Module with ID '%s' is already registered");
        }

        /*LoadCondition condition = registration.getCondition().get();
        if (!condition.isSuccess()) {
            this.plugin.error("Module '%s' can not be loaded: '%s'".formatted(id, condition.reason().orElse(null)));
            return false;
        }*/

        Path path = this.plugin.dataPath().resolve(id);
        ModuleContext context = this.plugin.createModuleContext(id, path, definition);
        Module module = registration.load(context);

        return this.moduleRegistry.register(module);
    }
}
