package su.nightexpress.nexshop.shop.virtual.config;

import com.google.common.collect.Sets;
import org.bukkit.GameMode;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyId;

import java.util.*;

public class VirtualConfig {

    public static final JOption<String>      DEFAULT_CURRENCY       = JOption.create("General.Default_Currency", CurrencyId.VAULT,
        "Sets default currency for the Virtual Shop module.",
        "This currency will be used when you create new products or in case, where other currencies are not available.",
        "Compatible plugins: https://github.com/nulli0n/ExcellentShop-spigot/wiki/Shop-Currency"
    );
    public static final JOption<Boolean>     MAIN_MENU_ENABLED      = JOption.create("General.Main_Menu_Enabled", true,
        "When 'true', enables the Main Menu, where you can list all of your Virtual Shops."
    );
    public static final JOption<Set<String>> GEN_DISABLED_GAMEMODES = JOption.create("General.Disabled_In_Gamemodes",
        Sets.newHashSet(GameMode.CREATIVE.name()),
        "A list of Game Modes, in which Virtual Shop can not be used.",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/GameMode.html"
    );
    public static final JOption<Set<String>> GEN_DISABLED_WORLDS    = JOption.create("General.Disabled_In_Worlds",
        Sets.newHashSet("world_name", "example_world123"),
        "A list of worlds, where Virtual Shop will be disabled"
    );

    public static final JOption<String>       SHOP_FORMAT_NAME = JOption.create("GUI.Shop_Format.Name", Placeholders.SHOP_NAME,
        "Sets display name for the shop item in the Main Menu.",
        "You can use 'Shop' placeholders here:" + Placeholders.URL_WIKI_PLACEHOLDERS
    );
    public static final JOption<List<String>> SHOP_FORMAT_LORE = JOption.create("GUI.Shop_Format.Lore",
        Arrays.asList("&7Need Permission: &f" + Placeholders.SHOP_VIRTUAL_PERMISSION_REQUIRED, "", Placeholders.SHOP_VIRTUAL_DESCRIPTION, "", "&aLeft-Click to &fOpen"),
        "Sets lore for the shop item in the Main Menu.",
        "You can use 'Shop' placeholders here: " + Placeholders.URL_WIKI_PLACEHOLDERS
    );

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_GENERAL_ALL = JOption.create("GUI.Product_Format.Lore.General.All",
        Arrays.asList(Placeholders.GENERIC_LORE, "", Placeholders.GENERIC_DISCOUNT, "",
            "&eBuy: &6" + Placeholders.PRODUCT_PRICE_BUY_FORMATTED + " &8| &eSell: &6" + Placeholders.PRODUCT_PRICE_SELL_FORMATTED,
            "", "%stock_global_buy%", "%stock_global_sell%", "%stock_player_buy%", "%stock_player_sell%", "",
            "&cLeft-Click &8→ &fSelect Quantity &8← &cRight-Click",
            "&cShift-Left &8→ &fBuy &7(Quick) &fSell &8← &cShift-Right",
            "&c[F] Key &8→ &fSell All &7(" + Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED + ")"),
        "Sets lore for the product preview item in Virtual Shop GUI.",
        "This lore will be used when both Buy and Sell prices are available.",
        "Local Placeholders:",
        "- %lore% - Original lore of the product preview item.",
        "- %discount% - Discount info (if present)",
        "- %stock_global_buy% - Global stock info for purchase (if present)",
        "- %stock_global_sell% - Global stock info for sale (if present)",
        "- %stock_player_buy% - Player limit info for purchase (if present)",
        "- %stock_player_sell% - Player limit info for sale (if present).",
        "You can use 'Product' placeholders here: " + Placeholders.URL_WIKI_PLACEHOLDERS
    );

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_GENERAL_BUY_ONLY = JOption.create("GUI.Product_Format.Lore.General.Buy_Only",
        Arrays.asList(Placeholders.GENERIC_LORE, "", Placeholders.GENERIC_DISCOUNT, "",
            "&eBuy: &6" + Placeholders.PRODUCT_PRICE_BUY_FORMATTED,
            "", "%stock_global_buy%", "%stock_player_buy%", "",
            "&cLeft-Click &8→ &fSelect Quantity",
            "&cShift-Left &8→ &fQuick Buy"),
        "Sets lore for the product preview item in Virtual Shop GUI.",
        "This lore will be used when only Buy price is available.",
        "Local Placeholders:",
        "- %lore% - Original lore of the product preview item.",
        "- %discount% - Discount info (if present)",
        "- %stock_global_buy% - Global stock info for purchase (if present)",
        "- %stock_player_buy% - Player limit info for purchase (if present).",
        "You can use 'Product' placeholders here: " + Placeholders.URL_WIKI_PLACEHOLDERS
    );

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_GENERAL_SELL_ONLY = JOption.create("GUI.Product_Format.Lore.General.Sell_Only",
        Arrays.asList(Placeholders.GENERIC_LORE, "", "%discount%", "",
            "&eSell: &6" + Placeholders.PRODUCT_PRICE_SELL_FORMATTED,
            "", "%stock_global_sell%", "%stock_player_sell%", "",
            "&cLeft-Click &8→ &fSelect Quantity",
            "&cShift-Left &8→ &fQuick Sell",
            "&c[F] Key &8→ &fSell All &7(" + Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED + ")"),
        "Sets lore for the product preview item in Virtual Shop GUI.",
        "This lore will be used when only Sell price is available.",
        "Local Placeholders:",
        "- %lore% - Original lore of the product preview item.",
        "- %stock_global_sell% - Global stock info for sale (if present)",
        "- %stock_player_sell% - Player limit info for sale (if present).",
        "You can use 'Product' placeholders here: " + Placeholders.URL_WIKI_PLACEHOLDERS
    );

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_DISCOUNT = JOption.create("GUI.Product_Format.Lore.Discount",
        Collections.singletonList("&c&l[!] #C70039&lSALE &e&l" + Placeholders.PRODUCT_DISCOUNT_AMOUNT + "%#C70039&l OFF &c&l[!]"),
        "Sets the discount display format when there is active discounts in the shop applicable to a product.",
        "You can use 'Product' placeholders here: " + Placeholders.URL_WIKI_PLACEHOLDERS
    );

    public static final JOption<Map<StockType, Map<TradeType, List<String>>>> PRODUCT_FORMAT_LORE_STOCK = new JOption<Map<StockType, Map<TradeType, List<String>>>>("GUI.Product_Format.Lore.Stock",
        (cfg, path, def) -> {
            Map<StockType, Map<TradeType, List<String>>> map = new HashMap<>();
            for (StockType stockType : StockType.values()) {
                for (TradeType tradeType : TradeType.values()) {
                    List<String> lore = StringUtil.color(cfg.getStringList(path + "." + stockType.name() + "." + tradeType.name()));
                    map.computeIfAbsent(stockType, k -> new HashMap<>()).put(tradeType, lore);
                }
            }
            return map;
        },
        () -> {
        Map<StockType, Map<TradeType, List<String>>> map = new HashMap<>();
        map.computeIfAbsent(StockType.GLOBAL, k -> new HashMap<>()).put(TradeType.BUY, Collections.singletonList("#95fafaBuy Stock &8→ #84dbdb" + Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_LEFT + "&7/#84dbdb" + Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_INITIAL + " &7(⟳ &f" + Placeholders.PRODUCT_STOCK_GLOBAL_BUY_RESTOCK_DATE + "&7)"));
        map.computeIfAbsent(StockType.GLOBAL, k -> new HashMap<>()).put(TradeType.SELL, Collections.singletonList("#95fafaSell Stock &8→ #84dbdb" + Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_LEFT + "&7/#84dbdb" + Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_INITIAL + " &7(⟳ &f" + Placeholders.PRODUCT_STOCK_GLOBAL_SELL_RESTOCK_DATE + "&7)"));
        map.computeIfAbsent(StockType.PLAYER, k -> new HashMap<>()).put(TradeType.BUY, Collections.singletonList("#FF7777Buy Limit &8→ #e16060" + Placeholders.PRODUCT_STOCK_PLAYER_BUY_AMOUNT_LEFT + "&7/#e16060" + Placeholders.PRODUCT_STOCK_PLAYER_BUY_AMOUNT_INITIAL + " &7(⟳ &f" + Placeholders.PRODUCT_STOCK_PLAYER_BUY_RESTOCK_DATE + "&7)"));
        map.computeIfAbsent(StockType.PLAYER, k -> new HashMap<>()).put(TradeType.SELL, Collections.singletonList("#FF7777Sell Limit &8→ #e16060" + Placeholders.PRODUCT_STOCK_PLAYER_SELL_AMOUNT_LEFT + "&7/#e16060" + Placeholders.PRODUCT_STOCK_PLAYER_SELL_AMOUNT_INITIAL + " &7(⟳ &f" + Placeholders.PRODUCT_STOCK_PLAYER_SELL_RESTOCK_DATE + "&7)"));
        return map;
    },
        "Sets the stock display format for each Stock and Trade types.",
        "If product stock settings is undefined, format will be skipped.",
        "You can use 'Product' placeholders here: " + Placeholders.URL_WIKI_PLACEHOLDERS
    );

    static {
        PRODUCT_FORMAT_LORE_STOCK.setWriter((cfg, path) -> {
            PRODUCT_FORMAT_LORE_STOCK.get().forEach((stockType, map1) -> {
                map1.forEach(((tradeType, lore) -> {
                    cfg.set(path + "." + stockType.name() + "." + tradeType.name(), lore);
                }));
            });
        });
    }
}
