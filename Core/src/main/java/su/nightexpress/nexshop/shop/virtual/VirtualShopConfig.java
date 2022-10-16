package su.nightexpress.nexshop.shop.virtual;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.currency.CurrencyId;

import java.util.List;
import java.util.Set;

public class VirtualShopConfig {

    public static ICurrency   DEFAULT_CURRENCY;
    public static Set<String> GEN_DISABLED_GAMEMODES;
    public static Set<String> GEN_DISABLED_WORLDS;

    public static List<String> PRODUCT_FORMAT_LORE_PRICE_ALL;
    public static List<String> PRODUCT_FORMAT_LORE_PRICE_BUY;
    public static List<String> PRODUCT_FORMAT_LORE_PRICE_SELL;

    public static JYML SHOP_LIST_YML;
    public static JYML SHOP_MAIN_YML;
    public static JYML SHOP_DISCOUNTS_YML;
    public static JYML SHOP_PRODUCT_LIST_YML;
    public static JYML SHOP_PRODUCT_MAIN_YML;

    public static void load(@NotNull VirtualShop shop, @NotNull JYML cfg) {
        SHOP_LIST_YML = JYML.loadOrExtract(shop.plugin(), shop.getPath() + "editor/shop_list.yml");
        SHOP_MAIN_YML = JYML.loadOrExtract(shop.plugin(), shop.getPath() + "editor/shop_main.yml");
        SHOP_PRODUCT_LIST_YML = JYML.loadOrExtract(shop.plugin(), shop.getPath() + "editor/shop_product_list.yml");
        SHOP_PRODUCT_MAIN_YML = JYML.loadOrExtract(shop.plugin(), shop.getPath() + "editor/shop_product.yml");
        SHOP_DISCOUNTS_YML = JYML.loadOrExtract(shop.plugin(), shop.getPath() + "editor/shop_discounts.yml");


        String path = "General.";
        cfg.addMissing(path + "Default_Currency", CurrencyId.VAULT);

        DEFAULT_CURRENCY = shop.plugin().getCurrencyManager().getCurrency(cfg.getString(path + "Default_Currency", CurrencyId.VAULT));
        GEN_DISABLED_GAMEMODES = cfg.getStringSet(path + "Disabled_In_Gamemodes");
        GEN_DISABLED_WORLDS = cfg.getStringSet(path + "Disabled_In_Worlds");

        path = "GUI.Product_Format.Lore.";
        PRODUCT_FORMAT_LORE_PRICE_ALL = StringUtil.color(cfg.getStringList(path + "Price_All"));
        PRODUCT_FORMAT_LORE_PRICE_BUY = StringUtil.color(cfg.getStringList(path + "Price_Buy_Only"));
        PRODUCT_FORMAT_LORE_PRICE_SELL = StringUtil.color(cfg.getStringList(path + "Price_Sell_Only"));

        cfg.saveChanges();
    }
}
