package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.data.UserManager;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

public class ShopAPI {

    public static final ExcellentShop PLUGIN = ExcellentShop.getPlugin(ExcellentShop.class);

    @NotNull
    public static DataHandler getDataHandler() {
        return PLUGIN.getData();
    }

    @NotNull
    public static CurrencyManager getCurrencyManager() {
        return PLUGIN.getCurrencyManager();
    }

    public static AuctionManager getAuctionManager() {
        return PLUGIN.getAuction();
    }

    public static VirtualShopModule getVirtualShop() {
        return PLUGIN.getVirtualShop();
    }

    public static ChestShopModule getChestShop() {
        return PLUGIN.getChestShop();
    }

    @NotNull
    public static UserManager getUserManager() {
        return PLUGIN.getUserManager();
    }
}
