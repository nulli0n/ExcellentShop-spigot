package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.nexshop.data.UserManager;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class ShopAPI {

    private static final ExcellentShop PLUGIN;

    static {
        PLUGIN = ExcellentShop.getInstance();
    }

    @Nullable
    public static VirtualShop getVirtualShop() {
        return PLUGIN.getVirtualShop();
    }

    @Nullable
    public static ChestShop getChestShop() {
        return PLUGIN.getChestShop();
    }

    @NotNull
    public static UserManager getUserManager() {
        return PLUGIN.getUserManager();
    }
}
