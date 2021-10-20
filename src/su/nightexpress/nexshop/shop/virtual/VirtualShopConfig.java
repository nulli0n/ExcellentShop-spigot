package su.nightexpress.nexshop.shop.virtual;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.StringUT;

import java.util.List;
import java.util.Set;

public class VirtualShopConfig {

    public static Set<String> GEN_DISABLED_GAMEMODES;
    public static Set<String> GEN_DISABLED_WORLDS;

    public static List<String> PRODUCT_FORMAT_LORE_PRICE_ALL;
    public static List<String> PRODUCT_FORMAT_LORE_PRICE_BUY;
    public static List<String> PRODUCT_FORMAT_LORE_PRICE_SELL;

    public static void load(@NotNull VirtualShop shop, @NotNull JYML cfg) {
        String path = "General.";
        GEN_DISABLED_GAMEMODES = cfg.getStringSet(path + "Disabled_In_Gamemodes");
        GEN_DISABLED_WORLDS = cfg.getStringSet(path + "Disabled_In_Worlds");

        path = "GUI.Product_Format.Lore.";
        PRODUCT_FORMAT_LORE_PRICE_ALL = StringUT.color(cfg.getStringList(path + "Price_All"));
        PRODUCT_FORMAT_LORE_PRICE_BUY = StringUT.color(cfg.getStringList(path + "Price_Buy_Only"));
        PRODUCT_FORMAT_LORE_PRICE_SELL = StringUT.color(cfg.getStringList(path + "Price_Sell_Only"));
    }
}
