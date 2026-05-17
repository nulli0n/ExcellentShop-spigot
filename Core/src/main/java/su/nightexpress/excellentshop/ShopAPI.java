package su.nightexpress.excellentshop;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.excellentshop.data.DataManager;
import su.nightexpress.excellentshop.playershop.ChestShopModule;
import su.nightexpress.excellentshop.user.UserManager;
import su.nightexpress.excellentshop.virtualshop.VirtualShopModule;

import java.util.function.Consumer;

@Deprecated
public class ShopAPI {

    private static ShopPlugin plugin;

    static void load(@NonNull ShopPlugin instance) {
        plugin = instance;
    }

    static void clear() {
        plugin = null;
    }

    public static boolean isInitialized() {
        return plugin != null;
    }

    @NonNull
    public static ShopPlugin getPlugin() {
        if (plugin == null) throw new IllegalStateException("API is not yet initialized!");

        return plugin;
    }

    @NonNull
    public static DataHandler getDataHandler() {
        return plugin.getDataHandler();
    }

    @NonNull
    public static DataManager getDataManager() {
        return plugin.getDataManager();
    }

    public static void dataAccess(@NonNull Consumer<DataManager> consumer) {
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

    @NonNull
    public static UserManager getUserManager() {
        return plugin.getUserManager();
    }
}
