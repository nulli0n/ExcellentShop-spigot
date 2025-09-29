package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.data.DataManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.user.UserManager;

import java.util.function.Consumer;

public class ShopAPI {

    private static ShopPlugin plugin;

    static void load(@NotNull ShopPlugin instance) {
        plugin = instance;
    }

    static void clear() {
        plugin = null;
    }

    public static boolean isInitialized() {
        return plugin != null;
    }

    @NotNull
    public static ShopPlugin getPlugin() {
        if (plugin == null) throw new IllegalStateException("API is not yet initialized!");

        return plugin;
    }

    @NotNull
    public static DataHandler getDataHandler() {
        return plugin.getDataHandler();
    }

    @NotNull
    public static DataManager getDataManager() {
        return plugin.getDataManager();
    }

    public static void dataAccess(@NotNull Consumer<DataManager> consumer) {
        plugin.dataAccess(consumer);
    }

    public static AuctionManager getAuctionManager() {
        return plugin.getAuction();
    }

    public static VirtualShopModule getVirtualShop() {
        return plugin.getVirtualShop();
    }

    public static ChestShopModule getChestShop() {
        return plugin.getChestShop();
    }

    @NotNull
    public static UserManager getUserManager() {
        return plugin.getUserManager();
    }
}
