package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.data.ShopDataHandler;
import su.nightexpress.nexshop.data.ShopUserManager;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

public class ShopAPI {

    public static final ExcellentShop PLUGIN = ExcellentShop.getPlugin(ExcellentShop.class);

    @NotNull
    public static ShopDataHandler getDataHandler() {
        return PLUGIN.getData();
    }

    @NotNull
    public static CurrencyManager getCurrencyManager() {
        return PLUGIN.getCurrencyManager();
    }

    public static AuctionManager getAuctionManager() {
        return PLUGIN.getAuctionManager();
    }

    public static VirtualShopModule getVirtualShop() {
        return PLUGIN.getVirtualShop();
    }

    public static ChestShopModule getChestShop() {
        return PLUGIN.getChestShop();
    }

    @NotNull
    public static ShopUserManager getUserManager() {
        return PLUGIN.getUserManager();
    }
}
