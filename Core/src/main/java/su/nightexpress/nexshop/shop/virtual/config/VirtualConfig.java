package su.nightexpress.nexshop.shop.virtual.config;

import org.bukkit.GameMode;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.currency.handler.VaultEconomyHandler;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.RankMap;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.*;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.shop.virtual.Placeholders.*;
import static su.nightexpress.nexshop.api.shop.type.TradeType.BUY;
import static su.nightexpress.nexshop.api.shop.type.TradeType.SELL;

public class VirtualConfig {

    public static final ConfigValue<String> DEFAULT_CURRENCY = ConfigValue.create("General.Default_Currency",
        VaultEconomyHandler.ID,
        "Sets default currency for the Virtual Shop module.",
        "This currency will be used when you create new products or if no other currency is available.",
        "Compatible plugins: " + URL_WIKI_CURRENCY);

    public static final ConfigValue<String> DEFAULT_CART_UI = ConfigValue.create("General.Default_Cart_UI",
        DEFAULT,
        "Sets default product purchase menu config.",
        "You can create and edit Cart UIs in " + Config.DIR_CARTS + " directory."
    );

    public static final ConfigValue<Boolean> SPLIT_BUY_SELL_CART_UI = ConfigValue.create("General.Split_BuySell_Cart_UI",
        false,
        "Sets whether or not Virtual Shop will use different purchase menus for buying and selling."
    );

    public static final ConfigValue<String> DEFAULT_BUY_CART_UI = ConfigValue.create("General.Default_Buy_Cart_UI",
        DEFAULT,
        "Sets default product purchase menu config for buying if Split option is enabled.",
        "You can create and edit Cart UIs in " + Config.DIR_CARTS + " directory."
    );

    public static final ConfigValue<String> DEFAULT_SELL_CART_UI = ConfigValue.create("General.Default_Sell_Cart_UI",
        DEFAULT,
        "Sets default product purchase menu config for selling if Split option is enabled.",
        "You can create and edit Cart UIs in " + Config.DIR_CARTS + " directory."
    );

    public static final ConfigValue<String> DEFAULT_LAYOUT = ConfigValue.create("General.Default_Layout",
        DEFAULT,
        "Sets default shop layout configuration in case if shop's one is not existing anymore."
    );

    public static final ConfigValue<Boolean> MAIN_MENU_ENABLED = ConfigValue.create("General.Main_Menu.Enabled",
        true,
        "Enables the Main Menu feature, where you can list all your Virtual Shops.");

    public static final ConfigValue<Boolean> MAIN_MENU_HIDE_NO_PERM_SHOPS = ConfigValue.create("General.Main_Menu.Hide_No_Permission_Shops",
        true,
        "When enabled, hides shops from the main menu a player don't have access to.");

    public static final ConfigValue<Boolean> SHOP_SHORTCUTS_ENABLED = ConfigValue.create("General.Shop_Shortcut.Enabled",
        true,
        "Enables the Shop Shortcut commands feature. Allows to quickly open shops.");

    public static final ConfigValue<String[]> SHOP_SHORTCUTS_COMMANDS = ConfigValue.create("General.Shop_Shortcut.Commands",
        new String[]{"shop"},
        "Command aliases for quick main menu and shop access.", "Split by commas.",
        "[*] Reboot is required when changed!"
    );

    public static final ConfigValue<Boolean> SELL_MENU_ENABLED = ConfigValue.create("General.Sell_Menu.Enabled",
        true,
        "When 'true' enables the Sell Menu feature, where you can quickly sell all your items.");

    public static final ConfigValue<Boolean> SELL_MENU_SIMPLIFIED = ConfigValue.create("General.Sell_Menu.Simplified",
        false,
        "Sets whether or not Sell Menu should be simplified.",
        "When simplified, no item and click validation is performed, so menu acts like a regular chest,",
        "and items will be sold on close instead of button click.");

    public static final ConfigValue<String[]> SELL_MENU_COMMANDS = ConfigValue.create("General.Sell_Menu.Commands",
        new String[]{"sellgui", "sellmenu"},
        "Custom command aliases to open the Sell Menu. Split them with a comma.",
        "[*] Reboot is required when changed!"
    );

    public static final ConfigValue<Boolean> SELL_ALL_ENABLED = ConfigValue.create("General.Sell_All.Enabeled",
        true,
        "Enables the Sell All command feature.");

    public static final ConfigValue<String[]> SELL_ALL_COMMANDS = ConfigValue.create("General.Sell_All.Commands",
        new String[]{"sellall"},
        "Sell All command aliases. Split by commas.",
        "[*] Reboot is required when changed!"
    );

    public static final ConfigValue<Boolean> SELL_HAND_ENABLED = ConfigValue.create("General.Sell_Hand.Enabled",
        true,
        "Enables the Sell Hand feature.");

    public static final ConfigValue<String[]> SELL_HAND_COMMANDS = ConfigValue.create("General.Sell_Hand.Commands",
        new String[]{"sellhand"},
        "Sell Hand command aliases. Split by commas.",
        "[*] Reboot is required when changed!"
    );

    public static final ConfigValue<RankMap<Double>> SELL_RANK_MULTIPLIERS = ConfigValue.create("General.Sell_Multipliers",
        (cfg, path, def) -> RankMap.readDouble(cfg, path, 1D),
        (cfg, path, rankMap) -> rankMap.write(cfg, path),
        () -> new RankMap<>(RankMap.Mode.RANK, VirtualPerms.PREFIX_SELL_MULTIPLIER, 1D, Map.of(
            "vip", 1.5D,
            "gold", 2D
        )),
        "Here you can define Sell Multipliers for certain ranks.",
        "If you want to use permission based system, you can use '" + VirtualPerms.PREFIX_SELL_MULTIPLIER + "[name]' permission pattern.",
        "(make sure to use names different from your permission ranks then)",
        "Formula: '<sellPrice> * <sellMultiplier>'. So, 1.0 = 100% (no changes), 1.5 = +50%, 0.75 = -25%, etc."
    );

    public static final ConfigValue<Set<GameMode>> DISABLED_GAMEMODES = ConfigValue.forSet("General.Disabled_In_Gamemodes",
        id -> StringUtil.getEnum(id, GameMode.class).orElse(null),
        (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
        () -> Lists.newSet(GameMode.CREATIVE),
        "Players can not use shops in specified gamemodes.",
        "Available values: " + StringUtil.inlineEnum(GameMode.class, ", ")
    );

    public static final ConfigValue<Set<String>> DISABLED_WORLDS = ConfigValue.create("General.Disabled_In_Worlds",
        Lists.newSet("world_name", "example_world123"),
        "Players can not use shops in specified worlds. Case sensetive."
    );

    public static final ConfigValue<String> SHOP_FORMAT_NAME = ConfigValue.create("GUI.Shop_Format.Name",
        Placeholders.SHOP_NAME,
        "Sets display name for the shop item in the Main Menu.",
        "You can use 'Vritual Shop' placeholders:" + URL_WIKI_PLACEHOLDERS
    );

    public static final ConfigValue<List<String>> SHOP_FORMAT_LORE = ConfigValue.create("GUI.Shop_Format.Lore",
        Lists.newList(
            Placeholders.SHOP_DESCRIPTION
        ),
        "Sets lore for the shop item in the Main Menu.",
        "You can use 'Virtual Shop' placeholders: " + URL_WIKI_PLACEHOLDERS
    );

    public static final ConfigValue<List<String>> PRODUCT_FORMAT_LORE_GENERAL = ConfigValue.create("GUI.Product_Format.Lore.Main",
        Lists.newList(
            GENERIC_PERMISSION,
            GENERIC_DISCOUNT,
            "",
            GENERIC_LORE,
            "",
            GENERIC_BUY,
            "",
            GENERIC_SELL,
            "",
            DARK_GRAY.enclose("Hold " + LIGHT_GRAY.enclose("Shift") + " to buy & sell quickly.")
        ),
        "Product lore format. Use '" + GENERIC_LORE + "' placeholder to insert original lore of the product item.",
        "You can use 'Virtual Product' placeholders: " + URL_WIKI_PLACEHOLDERS
    );

    public static final ConfigValue<List<String>> PRODUCT_FORMAT_LORE_BUY = ConfigValue.create("GUI.Product_Format.Lore.Buy",
        Lists.newList(
            GREEN.enclose(BOLD.enclose("BUY:")),
            GREEN.enclose("←" + WHITE.enclose(" Left Click to buy for ") + PRODUCT_PRICE_FORMATTED.apply(BUY)),
            "",
            PRICE_DYNAMIC.apply(BUY),
            "",
            STOCK_TYPE.apply(BUY),
            LIMIT_TYPE.apply(BUY)
        ),
        "Lore that will appear if product is buyable.",
        "Placeholder: " + GENERIC_BUY
    );

    public static final ConfigValue<List<String>> PRODUCT_FORMAT_LORE_SELL = ConfigValue.create("GUI.Product_Format.Lore.Sell",
        Lists.newList(
            RED.enclose(BOLD.enclose("SELL:")),
            RED.enclose("→" + WHITE.enclose(" Right Click to sell for ") + PRODUCT_PRICE_FORMATTED.apply(SELL)),
            RED.enclose("→" + WHITE.enclose(" Press [F] to sell all for ") + PRODUCT_PRICE_SELL_ALL_FORMATTED),
            "",
            PRICE_DYNAMIC.apply(SELL),
            "",
            STOCK_TYPE.apply(SELL),
            LIMIT_TYPE.apply(SELL)
        ),
        "Text to appear if product is sellable.",
        "Placeholder: " + GENERIC_SELL
    );

    public static final ConfigValue<List<String>> PRODUCT_FORMAT_LORE_DISCOUNT = ConfigValue.create("GUI.Product_Format.Lore.Discount",
        Lists.newList(
            GRAY.enclose(GREEN.enclose("✔") + " Discount " + GREEN.enclose(BOLD.enclose(PRODUCT_DISCOUNT_AMOUNT + "%")) + "!")
        ),
        "Text to appear if product has active discount.",
        "Placeholder to insert: " + GENERIC_DISCOUNT
    );

    public static final ConfigValue<List<String>> PRODUCT_FORMAT_LORE_NO_PERMISSION = ConfigValue.create("GUI.Product_Format.Lore.NoPermission",
        Lists.newList(
            GRAY.enclose(RED.enclose("✘") + " You don't have access to this item!")
        ),
        "Text to appear if player don't have access to a product.",
        "Placeholder to insert: " + GENERIC_PERMISSION
    );

    public static final ConfigValue<Map<TradeType, List<String>>> PRODUCT_FORMAT_LORE_PRICE_DYNAMIC = ConfigValue.forMap("GUI.Product_Format.Lore.PriceDynamics",
        (type) -> StringUtil.getEnum(type, TradeType.class).orElse(null),
        (cfg, path, type) -> cfg.getStringList(path + "." + type),
        (cfg, path, map) -> map.forEach((tradeType, lore) -> cfg.set(path + "." + tradeType.name(), lore)),
        () -> {
            return Map.of(
                TradeType.BUY, Lists.newList(
                    GREEN.enclose("[?]" + WHITE.enclose(" Average: ") + PRODUCT_PRICE_AVERAGE.apply(BUY) + WHITE.enclose(" | Dynamics: ") + PRODUCT_PRICE_AVERAGE_DIFFERENCE.apply(BUY))
                ),
                TradeType.SELL, Lists.newList(
                    RED.enclose("[?]" + WHITE.enclose(" Average: ") + PRODUCT_PRICE_AVERAGE.apply(SELL) + WHITE.enclose(" | Dynamics: ") + PRODUCT_PRICE_AVERAGE_DIFFERENCE.apply(SELL))
                ));
        },
        "Text to appear when product has dynamic/float price.",
        "Placeholders to insert:",
        PRICE_DYNAMIC.apply(BUY),
        PRICE_DYNAMIC.apply(SELL)
    );

    public static final ConfigValue<Map<TradeType, List<String>>> PRODUCT_FORMAT_LORE_STOCK = ConfigValue.forMap("GUI.Product_Format.Lore.Stock.GLOBAL",
        (type) -> StringUtil.getEnum(type, TradeType.class).orElse(null),
        (cfg, path, type) -> cfg.getStringList(path + "." + type),
        (cfg, path, map) -> map.forEach((tradeType, lore) -> cfg.set(path + "." + tradeType.name(), lore)),
        () -> {
            return Map.of(TradeType.BUY, Lists.newList(
                GREEN.enclose("● " + WHITE.enclose("Stock: ") + PRODUCT_STOCK_AMOUNT_LEFT.apply(BUY) + WHITE.enclose("/") + PRODUCT_STOCK_AMOUNT_INITIAL.apply(BUY)) + GRAY.enclose(" (" + WHITE.enclose(PRODUCT_STOCK_RESTOCK_DATE.apply(BUY)) + ")")
                ),
                TradeType.SELL, Lists.newList(
                RED.enclose("● " + WHITE.enclose("Stock: ") + PRODUCT_STOCK_AMOUNT_LEFT.apply(SELL) + WHITE.enclose("/") + PRODUCT_STOCK_AMOUNT_INITIAL.apply(SELL)) + GRAY.enclose(" (" + WHITE.enclose(PRODUCT_STOCK_RESTOCK_DATE.apply(SELL)) + ")")
                )
            );
        },
        "Text to appear when product has buy/sell stock configured.",
        "Placeholders to insert:",
        STOCK_TYPE.apply(BUY),
        STOCK_TYPE.apply(SELL)
    );

    public static final ConfigValue<Map<TradeType, List<String>>> PRODUCT_FORMAT_LORE_LIMIT = ConfigValue.forMap("GUI.Product_Format.Lore.Stock.PLAYER",
        (type) -> StringUtil.getEnum(type, TradeType.class).orElse(null),
        (cfg, path, type) -> cfg.getStringList(path + "." + type),
        (cfg, path, map) -> map.forEach((tradeType, lore) -> cfg.set(path + "." + tradeType.name(), lore)),
        () -> {
            return Map.of(TradeType.BUY, Lists.newList(
                GREEN.enclose("● " + WHITE.enclose("Your Limit: ") + PRODUCT_LIMIT_AMOUNT_LEFT.apply(BUY) + WHITE.enclose("/") + PRODUCT_LIMIT_AMOUNT_INITIAL.apply(BUY)) + GRAY.enclose(" (" + WHITE.enclose(PRODUCT_LIMIT_RESTOCK_DATE.apply(BUY)) + ")")
                ),
                TradeType.SELL, Lists.newList(
                RED.enclose("● " + WHITE.enclose("Your Limit: ") + PRODUCT_LIMIT_AMOUNT_LEFT.apply(SELL) + WHITE.enclose("/") + PRODUCT_LIMIT_AMOUNT_INITIAL.apply(SELL)) + GRAY.enclose(" (" + WHITE.enclose(PRODUCT_LIMIT_RESTOCK_DATE.apply(SELL)) + ")")
                )
            );
        },
        "Text to appear when product has buy/sell limits configured.",
        "Placeholders to insert:",
        LIMIT_TYPE.apply(BUY),
        LIMIT_TYPE.apply(SELL)
    );
}
