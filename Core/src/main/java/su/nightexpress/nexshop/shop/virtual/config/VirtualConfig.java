package su.nightexpress.nexshop.shop.virtual.config;

import org.bukkit.GameMode;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.PlayerRankMap;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.shop.virtual.Placeholders;

import java.util.*;

import static su.nexmedia.engine.utils.Colors.*;
import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nexshop.api.shop.type.TradeType.BUY;
import static su.nightexpress.nexshop.api.shop.type.TradeType.SELL;

public class VirtualConfig {

    public static final JOption<String> DEFAULT_CURRENCY = JOption.create("General.Default_Currency", CurrencyManager.VAULT,
        "Sets default currency for the Virtual Shop module.",
        "This currency will be used when you create new products or in case, where other currencies are not available.",
        "Compatible plugins: https://github.com/nulli0n/ExcellentShop-spigot/wiki/Shop-Currency");

    public static final JOption<Boolean> MAIN_MENU_ENABLED = JOption.create("General.Main_Menu_Enabled", true,
        "When 'true', enables the Main Menu, where you can list all of your Virtual Shops.");

    public static final JOption<String> SHOP_SHORTCUTS = JOption.create("General.Shop_Shortcuts", "shop",
        "A list of command aliases for quick access to main menu and shops.", "Split them with a comma.");

    public static final JOption<PlayerRankMap<Double>> SELL_RANK_MULTIPLIERS = new JOption<PlayerRankMap<Double>>("General.Sell_Multipliers",
        (cfg, path, def) -> PlayerRankMap.read(cfg, path, Double.class),
        () -> new PlayerRankMap<>(Map.of(
            "vip", 1.25,
            "premium", 1.50,
            "gold", 2.0
        )),
        "Here you can define Sell Multipliers for certain ranks.",
        "If you want to use permission based system instead of rank based, you can use '" + VirtualPerms.PREFIX_SELL_MULTIPLIER + "[name]' permission pattern.",
        "(make sure to use names different from your permission ranks then)",
        "Formula: 'sellPrice * sellMultiplier'. So, 1.0 = 100% (no changes), 1.5 = +50%, 0.75 = -25%, etc."
    ).mapReader(rm -> rm.setCheckAsPermission(VirtualPerms.PREFIX_SELL_MULTIPLIER)).setWriter((cfg, path, rankMap) -> rankMap.write(cfg, path));

    public static final JOption<Boolean> SELL_MENU_ENABLED = JOption.create("General.Sell_Menu.Enabled",
        true,
        "When 'true' enables the Sell Menu, where you can quickly sell all your items.");

    public static final JOption<Boolean> SELL_MENU_SIMPLIFIED = JOption.create("General.Sell_Menu.Simplified", false,
        "Sets whether or not Sell Menu should be simplified.",
        "When simplified, no item and click validation is performed, so menu acts like a regular chest,",
        "and items will be sold on close instead of button click.",
        "Also, you should remove all GUI buttons and items to avoid players stealing them.");

    public static final JOption<String> SELL_MENU_COMMANDS = JOption.create("General.Sell_Menu.Commands",
        "sellgui",
        "Custom command aliases to open the Sell Menu. Split them with a comma.");

    public static final JOption<String> SELL_ALL_COMMANDS = JOption.create("General.Sell_All.Commands",
        "sellall",
        "Custom Sell All command aliases. Split them with a comma.");

    public static final JOption<Set<String>> DISABLED_GAMEMODES = JOption.create("General.Disabled_In_Gamemodes",
        Set.of(GameMode.CREATIVE.name()),
        "A list of Game Modes, where players can not access shops.",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/GameMode.html");

    public static final JOption<Set<String>> DISABLED_WORLDS = JOption.create("General.Disabled_In_Worlds",
        Set.of("world_name", "example_world123"),
        "A list of worlds, where players can not access shops.");

    public static final JOption<String> SHOP_FORMAT_NAME = JOption.create("GUI.Shop_Format.Name", Placeholders.SHOP_NAME,
        "Sets display name for the shop item in the Main Menu.",
        "You can use 'Shop' placeholders:" + Placeholders.URL_WIKI_PLACEHOLDERS
    ).mapReader(Colorizer::apply);

    public static final JOption<List<String>> SHOP_FORMAT_LORE = JOption.create("GUI.Shop_Format.Lore",
        Arrays.asList(
            Placeholders.SHOP_DESCRIPTION,
            "",
            RED + "[!] " + GRAY + "Need Permission: " + RED + Placeholders.SHOP_PERMISSION_REQUIRED
        ),
        "Sets lore for the shop item in the Main Menu.",
        "You can use 'Shop' placeholders: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).mapReader(Colorizer::apply);

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_GENERAL_ALL = JOption.create("GUI.Product_Format.Lore.General.All",
        Arrays.asList(
            Placeholders.GENERIC_LORE,
            "",
            Placeholders.GENERIC_DISCOUNT,
            "",
            LIGHT_YELLOW + "▪ " + GRAY + "Buy: " + LIGHT_YELLOW + Placeholders.PRODUCT_PRICE_BUY_FORMATTED + DARK_GRAY + "(Left-Click)",
            LIGHT_YELLOW + "▪ " + GRAY + "Sell: " + LIGHT_YELLOW + Placeholders.PRODUCT_PRICE_SELL_FORMATTED + DARK_GRAY + "(Right-Click)",
            LIGHT_YELLOW + "▪ " + GRAY + "Sell All: " + LIGHT_YELLOW + Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED + DARK_GRAY + " (F/Swap Key)",
            "",
            DARK_GRAY + "(Hold " + LIGHT_YELLOW + "Shift" + DARK_GRAY + " for quick buy/sell)",
            "",
            "%stock_global_buy%",
            "%stock_global_sell%",
            "%stock_player_buy%",
            "%stock_player_sell%",
            "",
            "%permission%"
        ),
        "Sets lore for the product preview item in Virtual Shop GUI.",
        "This lore will be used when both Buy and Sell prices are available.",
        "Local Placeholders:",
        "- %lore% - Original lore of the product preview item.",
        "- %discount% - Discount info (if present)",
        "- %permission% - Permission requirement info (if present)",
        "- %stock_global_buy% - Global stock info for purchase (if present)",
        "- %stock_global_sell% - Global stock info for sale (if present)",
        "- %stock_player_buy% - Player limit info for purchase (if present)",
        "- %stock_player_sell% - Player limit info for sale (if present).",
        "You can use 'Product' placeholders: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).mapReader(Colorizer::apply);

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_GENERAL_BUY_ONLY = JOption.create("GUI.Product_Format.Lore.General.Buy_Only",
        Arrays.asList(
            Placeholders.GENERIC_LORE,
            "",
            Placeholders.GENERIC_DISCOUNT,
            "",
            LIGHT_YELLOW + "▪ " + GRAY + "Buy: " + LIGHT_YELLOW + Placeholders.PRODUCT_PRICE_BUY_FORMATTED + DARK_GRAY + "(Left-Click)",
            "",
            DARK_GRAY + "(Hold " + LIGHT_YELLOW + "Shift" + DARK_GRAY + " for quick buy)",
            "",
            "%stock_global_buy%",
            "%stock_player_buy%",
            "",
            "%permission%"
        ),
        "Sets lore for the product preview item in Virtual Shop GUI.",
        "This lore will be used when only Buy price is available.",
        "Local Placeholders:",
        "- %lore% - Original lore of the product preview item.",
        "- %discount% - Discount info (if present)",
        "- %permission% - Permission requirement info (if present)",
        "- %stock_global_buy% - Global stock info for purchase (if present)",
        "- %stock_player_buy% - Player limit info for purchase (if present).",
        "You can use 'Product' placeholders: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).mapReader(Colorizer::apply);

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_GENERAL_SELL_ONLY = JOption.create("GUI.Product_Format.Lore.General.Sell_Only",
        Arrays.asList(
            Placeholders.GENERIC_LORE,
            "",
            Placeholders.GENERIC_DISCOUNT,
            "",
            LIGHT_YELLOW + "▪ " + GRAY + "Sell: " + LIGHT_YELLOW + Placeholders.PRODUCT_PRICE_SELL_FORMATTED + DARK_GRAY + "(Right-Click)",
            LIGHT_YELLOW + "▪ " + GRAY + "Sell All: " + LIGHT_YELLOW + Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED + DARK_GRAY + " (F/Swap Key)",
            "",
            DARK_GRAY + "(Hold " + LIGHT_YELLOW + "Shift" + DARK_GRAY + " for quick sell)",
            "",
            "%stock_global_sell%",
            "%stock_player_sell%",
            "",
            "%permission%"
        ),
        "Sets lore for the product preview item in Virtual Shop GUI.",
        "This lore will be used when only Sell price is available.",
        "Local Placeholders:",
        "- %lore% - Original lore of the product preview item.",
        "- %permission% - Permission requirement info (if present)",
        "- %stock_global_sell% - Global stock info for sale (if present)",
        "- %stock_player_sell% - Player limit info for sale (if present).",
        "You can use 'Product' placeholders: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).mapReader(Colorizer::apply);

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_DISCOUNT = JOption.create("GUI.Product_Format.Lore.Discount",
        Collections.singletonList(
            LIME + "[!] " + GRAY + "There is " + LIME + Placeholders.PRODUCT_DISCOUNT_AMOUNT + "%" + GRAY + " discount on this item!"
        ),
        "Sets the discount display format when there is active discounts in the shop applicable to a product.",
        "You can use 'Product' placeholders: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).mapReader(Colorizer::apply);

    public static final JOption<List<String>> PRODUCT_FORMAT_LORE_NO_PERMISSION = JOption.create("GUI.Product_Format.Lore.NoPermission",
        Collections.singletonList(
            RED + "[!] " + GRAY + "You don't have " + RED + "permission" + GRAY + " to this item!"
        ),
        "Text to display in item lore when player has no permission to product.",
        "You can use 'Product' placeholders: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).mapReader(Colorizer::apply);

    public static final JOption<Map<TradeType, List<String>>> PRODUCT_FORMAT_LORE_STOCK = JOption.forMap("GUI.Product_Format.Lore.Stock.GLOBAL",
        (type) -> StringUtil.getEnum(type, TradeType.class).orElse(null),
        (cfg, path, type) -> Colorizer.apply(cfg.getStringList(path + "." + type)),
        () -> {
            Map<TradeType, List<String>> map = new HashMap<>();
            map.put(TradeType.BUY,
                Collections.singletonList(
                    CYAN + "Buy Stock: " + CYAN + PRODUCT_STOCK_AMOUNT_LEFT.apply(BUY) + GRAY + "/" + CYAN + PRODUCT_STOCK_AMOUNT_INITIAL.apply(BUY) + GRAY + " (⟳ &f" + PRODUCT_STOCK_RESTOCK_DATE.apply(BUY) + GRAY + ")"
                )
            );

            map.put(TradeType.SELL,
                Collections.singletonList(
                    CYAN + "Sell Stock: " + CYAN + PRODUCT_STOCK_AMOUNT_LEFT.apply(SELL) + GRAY + "/" + CYAN + PRODUCT_STOCK_AMOUNT_INITIAL.apply(SELL) + GRAY + " (⟳ &f" + PRODUCT_STOCK_RESTOCK_DATE.apply(SELL) + GRAY + ")"
                )
            );
            return map;
        },
        "Sets display format for product Stock.",
        "If product stock settings is undefined, it will be skipped.",
        "You can use 'Product' placeholders: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).setWriter((cfg, path, map) -> map.forEach((tradeType, lore) -> {
        cfg.set(path + "." + tradeType.name(), lore);
    }));

    public static final JOption<Map<TradeType, List<String>>> PRODUCT_FORMAT_LORE_LIMIT = JOption.forMap("GUI.Product_Format.Lore.Stock.PLAYER",
        (type) -> StringUtil.getEnum(type, TradeType.class).orElse(null),
        (cfg, path, type) -> Colorizer.apply(cfg.getStringList(path + "." + type)),
        () -> {
            Map<TradeType, List<String>> map = new HashMap<>();
            map.put(TradeType.BUY,
                Collections.singletonList(
                    RED + "Buy Limit: " + RED + PRODUCT_LIMIT_AMOUNT_LEFT.apply(BUY) + GRAY + "/" + RED + PRODUCT_LIMIT_AMOUNT_INITIAL.apply(BUY) + GRAY + " (⟳ &f" + PRODUCT_LIMIT_RESTOCK_DATE.apply(BUY) + GRAY + ")"
                )
            );

            map.put(TradeType.SELL,
                Collections.singletonList(
                    RED + "Sell Limit: " + RED + PRODUCT_LIMIT_AMOUNT_LEFT.apply(SELL) + GRAY + "/" + RED + PRODUCT_LIMIT_AMOUNT_INITIAL.apply(SELL) + GRAY + " (⟳ &f" + PRODUCT_LIMIT_RESTOCK_DATE.apply(SELL) + GRAY + ")"
                )
            );
            return map;
        },
        "Sets display format for product Player Limits.",
        "If product limit settings is undefined, it will be skipped.",
        "You can use 'Product' placeholders: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).setWriter((cfg, path, map) -> map.forEach((tradeType, lore) -> {
        cfg.set(path + "." + tradeType.name(), lore);
    }));
}
