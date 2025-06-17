package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.module.Module;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

import java.util.HashMap;
import java.util.Map;

public class ModuleLoaders {

    private static final Map<String, Loader<?>> LOADER_MAP = new HashMap<>();

    public static void load() {
        addLoader(ModuleId.AUCTION, AuctionManager::new);
        addLoader(ModuleId.CHEST_SHOP, ChestShopModule::new);
        addLoader(ModuleId.VIRTUAL_SHOP, VirtualShopModule::new);
    }

    public static <T extends Module> void addLoader(@NotNull String id, @NotNull Loader<T> loader) {
        LOADER_MAP.put(id.toLowerCase(), loader);
    }

    @Nullable
    public static Module loadModule(@NotNull ShopPlugin plugin, @NotNull String id, @NotNull ModuleConfig config) {
        Loader<?> loader = LOADER_MAP.get(id.toLowerCase());
        if (loader == null) {
            plugin.error("Could not load unknown module '" + id + "'.");
            return null;
        }

        if (!config.isEnabled()) return null;

        return loader.create(plugin, id, config);
    }

    public interface Loader<T extends Module> {

        @NotNull T create(@NotNull ShopPlugin plugin, @NotNull String id, @NotNull ModuleConfig config);
    }
}
