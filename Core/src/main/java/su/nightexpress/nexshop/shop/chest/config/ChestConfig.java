package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.Material;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.hook.HookPlugin;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.impl.Showcase;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nightcore.integration.item.ItemPlugins;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.RankMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nexshop.api.shop.type.TradeType.BUY;
import static su.nightexpress.nexshop.api.shop.type.TradeType.SELL;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ChestConfig {

    public static final ConfigValue<Boolean> RENT_ENABLED = ConfigValue.create("Rent.Enabled",
        true,
        "Controls whether Shop Rent feature is enabled.");

    public static final ConfigValue<Integer> RENT_MAX_DURATION = ConfigValue.create("Rent.MaxDuration",
        60,
        "Sets limit for rent duration value.",
        "[*] Use -1 for unlimited."
    );

    public static final ConfigValue<Map<String, Double>> RENT_MAX_PRICE = ConfigValue.forMapById("Rent.MaxPrice",
        (cfg, path, id) -> cfg.getDouble(path + "." + id),
        map -> {
            map.put(CurrencyId.VAULT, 100_00D);
        },
        "Sets limit for rent price per currency.",
        "[*] Use -1 for unlimited."
    );

    public static final ConfigValue<Integer> SAVE_INTERVAL = ConfigValue.create("Shops.Save_Interval",
        300,
        "Sets how often (in seconds) shop changes will be saved (written) to their configuration files.",
        "[Asynchronous]",
        "[Default is 300]"
    );

    public static final ConfigValue<String> ADMIN_SHOP_NAME = ConfigValue.create("Shops.AdminShop_Name",
        "AdminShop",
        "Sets custom shop's owner name for admin shops.");

    public static final ConfigValue<String> DEFAULT_NAME = ConfigValue.create("Shops.Default_Name",
        "Shop",
        "Default shop name, that will be used on shop creation."
    );

    public static final ConfigValue<Integer> SHOP_MAX_NAME_LENGTH = ConfigValue.create("Shops.Max_Name_Length",
        12,
        "Sets max. possible length for shop name.",
        "Useful to prevent players from setting ridiculously long names for their shops."
    );

    public static final ConfigValue<String> DEFAULT_CART_UI = ConfigValue.create("Shops.Default_Cart_UI",
        DEFAULT,
        "Sets default product purchase menu config.",
        "You can create and edit Cart UIs in " + Config.DIR_CARTS + " directory."
    );

    public static final ConfigValue<Boolean> CHECK_SAFE_LOCATION = ConfigValue.create("Shops.Check_Safe_Location",
        true,
        "Controls whether plugin should check shop's location safety before player teleports to it."
    );

    public static final ConfigValue<Boolean> SHOP_AUTO_BANK = ConfigValue.create("Shops.Auto_Bank",
        true,
        "Sets whether or not player's shop bank will be auto-managed without manual operations.",
        "This means that players will gain/lose their funds instantly for each transaction in their shops.");

    public static final ConfigValue<Boolean> SHOP_OFFLINE_TRANSACTIONS = ConfigValue.create("Shops.OfflineTransactions",
        false,
        "Sets whether or not plugin will withdraw/deposit currency directly",
        "from/to shop owner's account when player is offline instead of shop bank usage.",
        "",
        "Why? Even with 'Auto_Bank' option enabled, players can't sell items to shops with offline owners",
        "unless there are enough funds in the shop bank.",
        "This setting will bypass that.",
        "",
        "[*] This feature may damage performance (depends on the currency plugin and its offline player data handling).",
        "[*] This feature is not available for some currencies.");

    public static final ConfigValue<Boolean> SHOP_INFINITE_STORAGE_ENABLED = ConfigValue.create("Shops.InfiniteStorage.Enabled",
        false,
        "Sets whether or not infinite storage system is enabled.",
        "Infinite storage allows you to store as many items in your shops as you want/can,",
        "and don't uses block inventories.");



    public static final ConfigValue<Double> SHOP_PRODUCT_INITIAL_BUY_PRICE = ConfigValue.create("Shops.Product.InitialPrice.Buy",
        10D,
        "Sets initial buy price for new products added in chest shops.");

    public static final ConfigValue<Double> SHOP_PRODUCT_INITIAL_SELL_PRICE = ConfigValue.create("Shops.Product.InitialPrice.Sell",
        2D,
        "Sets initial sell price for new products added in chest shops.");

    public static final ConfigValue<Boolean> SHOP_ITEM_CREATION_ENABLED = ConfigValue.create("Shops.ItemCreation.Enabled",
        false,
        "Sets whether or not players can create shops by placing specific item.");

    public static final ConfigValue<Double> SHOP_CREATION_COST_CREATE = ConfigValue.create("Shops.Creation.Cost.Create",
        0D,
        "Sets how much player have to pay in order to create a chest shop.");

    public static final ConfigValue<Double> SHOP_CREATION_COST_REMOVE = ConfigValue.create("Shops.Creation.Cost.Remove",
        0D,
        "Sets how much player have to pay in order to remove a chest shop.");

    public static final ConfigValue<Set<String>> SHOP_CREATION_WORLD_BLACKLIST = ConfigValue.create("Shops.Creation.World_Blacklist",
        Set.of("custom_world", "another_world"),
        "List of worlds, where chest shop creation is not allowed.");

    public static final ConfigValue<RankMap<Integer>> SHOP_CREATION_MAX_PER_RANK = ConfigValue.create("Shops.Creation.Max_Shops_Per_Rank",
        (cfg, path, rank) -> RankMap.read(cfg, path, Integer.class, 10),
        (cfg, path, map) -> map.write(cfg, path),
        () -> new RankMap<>(
            RankMap.Mode.RANK,
            ChestPerms.PREFIX_SHOP_LIMIT,
            10,
            Map.of(
                "vip", 20,
                "admin", -1
            )
        ),
        "Sets amount of possible shops available for certain ranks/permissions.",
        "Use '-1' for unlimited amount."
    );

    public static final ConfigValue<Boolean> SHOP_CREATION_CHECK_BUILD = ConfigValue.create("Shops.Creation.Check_Build_Access",
        true,
        "Sets whether or not plugin will simulate block placing to make sure that player can crate shops there.",
        "This setting should be enough as universal compatibility solution with claim/protection plugins.",
        "Disable this only if you're experiencing major issues."
    );

    public static final ConfigValue<Boolean> SHOP_CREATION_CLAIM_ONLY = ConfigValue.create("Shops.Creation.In_Player_Claims_Only.Enabled",
        false,
        "Sets whether or not players can create shops in their own claims only.",
        "Supported Plugins: " + HookPlugin.LANDS + ", " + HookPlugin.GRIEF_PREVENTION + ", " + HookPlugin.WORLD_GUARD + ", " + HookPlugin.KINGDOMS);

    public final static ConfigValue<RankMap<Integer>> SHOP_PRODUCTS_MAX_PER_RANK = ConfigValue.create("Shops.Products.Max_Products_Per_Shop",
        (cfg, path, rank) -> RankMap.read(cfg, path, Integer.class, 3),
        (cfg, path, map) -> map.write(cfg, path),
        () -> new RankMap<>(
            RankMap.Mode.RANK,
            ChestPerms.PREFIX_PRODUCT_LIMIT,
            3,
            Map.of(
                "vip", 5,
                "admin", -1
            )
        ),
        "Sets how many products a player with certain rank/permission can put in a shop at the same time.",
        "Use '-1' for unlimited amount."
    );

    public static final ConfigValue<Boolean> SHOP_PRODUCT_NEW_PRODUCTS_SINGLE_AMOUNT = ConfigValue.create("Shops.Products.New_Products_Single_Amount",
        false,
        "When enabled, all items added as products in chest shops will be added with 1 amount no matter of the stack amount player used in."
    );

    public static final ConfigValue<Set<String>> SHOP_PRODUCT_BANNED_ITEMS = ConfigValue.create("Shops.Products.Material_Blacklist",
        Set.of(
            BukkitThing.toString(Material.BARRIER),
            "custom_item_123"
        ),
        "List of items that can not be added as shop products.",
        "Vanilla Names: https://minecraft.wiki/w/Java_Edition_data_values -> Blocks / Items -> Resource location column.",
        "Supported Plugins: " + String.join(", ", ItemPlugins.values())
    );

    public static final ConfigValue<Set<String>> SHOP_PRODUCT_DENIED_LORES = ConfigValue.create("Shops.Products.Lore_Blacklist",
        Set.of("fuck", "ass hole bitch"),
        "Items containing the following words in their lore will be disallowed from being used as shop products.");

    public static final ConfigValue<Set<String>> SHOP_PRODUCT_DENIED_NAMES = ConfigValue.create("Shops.Products.Name_Blacklist",
        Set.of("shit", "sample text"),
        "Items containing the following words in their name will be disallowed from being used as shop products.");




    public static final ConfigValue<List<String>> PRODUCT_FORMAT_LORE_GENERAL = ConfigValue.create("Shops.Products.Format.Main",
        Lists.newList(
            GENERIC_LORE,
            "",
            GENERIC_BUY,
            "",
            GENERIC_SELL,
            "",
            DARK_GRAY.wrap("Hold " + LIGHT_GRAY.wrap("Shift") + " to buy & sell quickly.")
        ),
        "Product lore format. Use '" + GENERIC_LORE + "' placeholder to insert original lore of the product item.",
        "You can use 'Chest Product' placeholders: " + URL_WIKI_PLACEHOLDERS
    );

    public static final ConfigValue<List<String>> PRODUCT_FORMAT_LORE_BUY = ConfigValue.create("Shops.Products.Format.Buy",
        Lists.newList(
            GREEN.wrap(BOLD.wrap("BUY:")),
            GREEN.wrap("←" + WHITE.wrap(" Left Click to buy for ") + PRODUCT_PRICE_FORMATTED.apply(BUY)),
            GREEN.wrap("✔" + WHITE.wrap(" Items Left: ") + PRODUCT_STOCK_AMOUNT_LEFT.apply(BUY))
        ),
        "Lore that will appear if product is buyable.",
        "Placeholder: " + GENERIC_BUY
    );

    public static final ConfigValue<List<String>> PRODUCT_FORMAT_LORE_SELL = ConfigValue.create("Shops.Products.Format.Sell",
        Lists.newList(
            RED.wrap(BOLD.wrap("SELL:")),
            RED.wrap("→" + WHITE.wrap(" Right Click to sell for ") + PRODUCT_PRICE_FORMATTED.apply(SELL)),
            RED.wrap("→" + WHITE.wrap(" Press [F] to sell all for ") + PRODUCT_PRICE_SELL_ALL_FORMATTED),
            RED.wrap("✔" + WHITE.wrap(" Shop Space: ") + PRODUCT_STOCK_AMOUNT_LEFT.apply(SELL))
        ),
        "Text to appear if product is sellable.",
        "Placeholder: " + GENERIC_SELL
    );

    public static final ConfigValue<Integer> DISPLAY_UPDATE_INTERVAL = ConfigValue.create("Display.Update_Interval",
        1,
        "Sets how often (in seconds) shop displays will render for players."
    );

    public static final ConfigValue<Integer> DISPLAY_ITEM_CHANGE_INTERVAL = ConfigValue.create("Display.Item_Change_Interval",
        5,
        "Sets how often (in seconds) item displayed in the showcase will change.",
        "[*] Must be divisible by the Update_Interval value."
    );

    public static final ConfigValue<Integer> DISPLAY_VISIBLE_DISTANCE = ConfigValue.create("Display.Visible_Distance",
        10,
        "Sets shop display visibility distance."
    );

    public static final ConfigValue<Boolean> DISPLAY_USE_ARMOR_STANDS = ConfigValue.create("Display.UseArmorStands",
        false,
        "Uses ArmorStand entities to display holograms and showcases instead of TextDisplay and ItemDisplay ones.",
        "[*] Enable only if you're experiencing compatibility issues."
    );

    public static final ConfigValue<Boolean> DISPLAY_HOLOGRAM_ENABLED = ConfigValue.create("Display.Hologram.Enabled",
        true,
        "Controls whether shops will have client-side holograms displaying its name and prices.");

    public static final ConfigValue<Double> DISPLAY_HOLOGRAM_LINE_GAP = ConfigValue.create("Display.Hologram.LineGap",
        0.3D,
        "Sets distance between hologram lines.");

    public static final ConfigValue<Integer> DISPLAY_HOLOGRAM_LINE_WIDTH = ConfigValue.create("Display.Hologram.LineWidth",
        200,
        "Maximum line width used to split lines.",
        "[*] Lines split is not supported currently. Adjust the hologram format to avoid splitting.",
        "[Default is 200]"
    );

    public static final ConfigValue<Integer> DISPLAY_HOLOGRAM_TEXT_OPACITY = ConfigValue.create("Display.Hologram.TextOpacity",
        -1,
        "Alpha value of rendered text. Value ranges from 0 to 255. Values up to 3 are treated as fully opaque (255).",
        "The text rendering is discarded for values between 4 and 26. Defaults to -1, which represents 255 and is completely opaque.",
        "[Default is -1]"
    );

    public static final ConfigValue<Boolean> DISPLAY_HOLOGRAM_SEE_THROUGH = ConfigValue.create("Display.Hologram.SeeThrough",
        false,
        "Whether the text be visible through blocks.",
        "[Default is false]"
    );

    public static final ConfigValue<Boolean> DISPLAY_HOLOGRAM_SHADOW = ConfigValue.create("Display.Hologram.Shadow",
        true,
        "Whether the text is displayed with shadow.",
        "[Default is true]"
    );

    public static final ConfigValue<int[]> DISPLAY_HOLOGRAM_BACKGROUND_COLOR = ConfigValue.create("Display.Hologram.BackgroundColor",
        new int[]{64, 0, 0, 0},
        "The background color, arranged by [A,R,G,B]. Where: A = Alpha (opacity), R = Red, G = Green, B = Blue.",
        "[Default is 64,0,0,0]"
    );

    public static final ConfigValue<List<String>> DISPLAY_HOLOGRAM_TEXT_ADMIN_SHOP = ConfigValue.create("Display.Hologram.Text.AdminShop",
        Lists.newList(
            LIGHT_YELLOW.wrap(BOLD.wrap(SHOP_NAME)),
            LIGHT_GRAY.wrap(PRODUCT_PREVIEW_NAME),
            GENERIC_BUY + " " + GENERIC_SELL
        ),
        "Hologram text format for Admin Shops.",
        "Placeholders:",
        "- Chest Shop placeholders: " + URL_WIKI_PLACEHOLDERS,
        "- Chest Product placeholders: " + URL_WIKI_PLACEHOLDERS,
        "- " + Plugins.PLACEHOLDER_API
    );

    public static final ConfigValue<List<String>> DISPLAY_HOLOGRAM_TEXT_PLAYER_SHOP = ConfigValue.create("Display.Hologram.Text.PlayerShop",
        Lists.newList(
            LIGHT_YELLOW.wrap(BOLD.wrap(SHOP_NAME)),
            LIGHT_GRAY.wrap(PRODUCT_PREVIEW_NAME),
            GENERIC_BUY + " " + GENERIC_SELL,
            LIGHT_GRAY.wrap("Stock: " + LIGHT_YELLOW.wrap(PRODUCT_AMOUNT))
        ),
        "Hologram text format for Player Shops.",
        "- Chest Shop placeholders: " + URL_WIKI_PLACEHOLDERS,
        "- Chest Product placeholders: " + URL_WIKI_PLACEHOLDERS,
        "- " + Plugins.PLACEHOLDER_API
    );

    public static final ConfigValue<List<String>> DISPLAY_HOLOGRAM_TEXT_RENT = ConfigValue.create("Display.Hologram.Text.Rent",
        Lists.newList(
            LIGHT_YELLOW.wrap(BOLD.wrap("For Rent")),
            LIGHT_GRAY.wrap(CHEST_SHOP_RENT_DURATION),
            LIGHT_GRAY.wrap(CHEST_SHOP_RENT_PRICE)
        ),
        "Hologram text for shops set for Rent."
    );

    public static final ConfigValue<List<String>> DISPLAY_HOLOGRAM_TEXT_ABSENT = ConfigValue.create("Display.Hologram.Text.Absent",
        Lists.newList(
            GRAY.wrap("< Not Configured >")
        ),
        "Hologram text for shops without products added."
    );

    public static final ConfigValue<String> DISPLAY_HOLOGRAM_TEXT_BUY = ConfigValue.create("Display.Hologram.Text.BuyPrice",
        GREEN.wrap("B: ") + LIGHT_GREEN.wrap(PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY)),
        "Text used in the '" + GENERIC_BUY + "' placeholder if displayed product is buyable."
    );

    public static final ConfigValue<String> DISPLAY_HOLOGRAM_TEXT_SELL = ConfigValue.create("Display.Hologram.Text.SellPrice",
        RED.wrap("S: ") + LIGHT_RED.wrap(PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL)),
        "Text used in the '" + GENERIC_SELL + "' placeholder if displayed product is sellable."
    );

    public static final ConfigValue<Map<String, Showcase>> SHOWCASE_CATALOG = ConfigValue.forMapById("Display.Showcase.Catalog",
        Showcase::read,
        map -> map.putAll(ChestUtils.getDefaultShowcaseCatalog()),
        "The following showcases will be available for players to select for their shops."
    );

    public static boolean isAutoBankEnabled() {
        return SHOP_AUTO_BANK.get();
    }

    public static boolean isRentEnabled() {
        return RENT_ENABLED.get();
    }
}
