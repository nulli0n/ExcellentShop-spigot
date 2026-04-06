package su.nightexpress.nexshop.module;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.api.Module;
import su.nightexpress.nightcore.util.LowerCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModuleRegistry {

    private final Map<String, Module>   byId;
    private final Map<Class<?>, Module> byType;

    public ModuleRegistry() {
        this.byId = new HashMap<>();
        this.byType = new HashMap<>();
    }

    public void reload() {
        this.getModules().forEach(Module::setup);
    }

    public boolean register(@NonNull Module module) throws IllegalStateException {
        if (this.isPresent(module.getId())) throw new IllegalStateException("Module with such ID is already registered!");
        if (this.isPresent(module.getClass())) throw new IllegalStateException("Module of such type is already registered!");

        this.byId.put(module.getId(), module);
        this.byType.put(module.getClass(), module);

        //module.init();
        module.setup();
        return true;
    }

    public void clear() {
        this.getModules().forEach(Module::shutdown);
        this.byId.clear();
        this.byType.clear();
    }

    public boolean isPresent(@NonNull String id) {
        return this.byId(id).isPresent();
    }

    public <T extends Module> boolean isPresent(@NonNull Class<T> type) {
        return this.byType(type).isPresent();
    }

    @NonNull
    public Optional<Module> byId(@NonNull String id) {
        return Optional.ofNullable(this.getById(id));
    }

    @NonNull
    public <T extends Module> Optional<T> byType(@NonNull Class<T> type) {
        return Optional.ofNullable(this.byType.get(type)).map(type::cast);
    }

    @Nullable
    public Module getById(@NonNull String id) {
        return this.byId.get(LowerCase.INTERNAL.apply(id));
    }

    @NonNull
    public Set<Module> getModules() {
        return Set.copyOf(this.byType.values());
    }
}
