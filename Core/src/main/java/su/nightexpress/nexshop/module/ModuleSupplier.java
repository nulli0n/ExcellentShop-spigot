package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.module.Module;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nightcore.util.Lists;

import java.util.List;

public class ModuleSupplier<T extends Module> {

    private final String         id;
    private final Initializer<T> initializer;
    private final ModuleSettings settings;

    private ModuleSupplier(@NotNull String id, @NotNull Initializer<T> initializer, @NotNull ModuleSettings settings) {
        this.id = id;
        this.initializer = initializer;
        this.settings = settings;
    }

    @NotNull
    public static List<ModuleSupplier<?>> create() {
        return Lists.newList(
            new ModuleSupplier<>(ModuleId.AUCTION, AuctionManager::new, ModuleSettings.createNoItemHandlers("Auction", "auction", "auc", "ah")),
            new ModuleSupplier<>(ModuleId.CHEST_SHOP, ChestShopModule::new, ModuleSettings.createNoItemHandlers("ChestShop", "chestshop", "cshop", "playershop", "pshop")),
            new ModuleSupplier<>(ModuleId.VIRTUAL_SHOP, VirtualShopModule::new, ModuleSettings.createDefault("Shop", "virtualshop", "vshop"))
        );
    }

    @NotNull
    public T init(@NotNull ShopPlugin plugin, @NotNull ModuleSettings settings) {
        return this.initializer.init(plugin, this.id, settings);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public ModuleSettings getSettings() {
        return this.settings;
    }

    private interface Initializer<T extends Module> {

        @NotNull T init(@NotNull ShopPlugin plugin, @NotNull String id, @NotNull ModuleSettings settings);
    }
}
