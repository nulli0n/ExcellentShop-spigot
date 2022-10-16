package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.data.UserManager;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class ShopAPI {

    private static final ExcellentShop PLUGIN = ExcellentShop.getPlugin(ExcellentShop.class);

    public static AuctionManager getAuctionManager() {
        return PLUGIN.getAuctionManager();
    }

    public static VirtualShop getVirtualShop() {
        return PLUGIN.getVirtualShop();
    }

    public static ChestShop getChestShop() {
        return PLUGIN.getChestShop();
    }

    @NotNull
    public static UserManager getUserManager() {
        return PLUGIN.getUserManager();
    }
}
