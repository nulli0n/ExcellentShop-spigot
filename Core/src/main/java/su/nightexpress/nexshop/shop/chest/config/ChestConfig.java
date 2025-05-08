package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.economybridge.item.ItemPlugins;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.*;

import java.util.*;

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

    public static final ConfigValue<Boolean> DELETE_INVALID_SHOP_CONFIGS = ConfigValue.create("Shops.Delete_Invalid_Shop_Configs",
        false,
        "Sets whether or not invalid shops (that can not be loaded properly) will be auto deleted.");

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

    public static final ConfigValue<String> DEFAULT_CURRENCY = ConfigValue.create("Shops.Default_Currency",
        CurrencyId.VAULT,
        "Sets the default ChestShop currency. It will be used for new products and when no other currencies are available.",
        "As well as for shop creation/deletion price."
    ).onRead(CurrencyId::reroute);

    public static final ConfigValue<String> DEFAULT_CART_UI = ConfigValue.create("Shops.Default_Cart_UI",
        DEFAULT,
        "Sets default product purchase menu config.",
        "You can create and edit Cart UIs in " + Config.DIR_CARTS + " directory."
    );

    public static final ConfigValue<Boolean> CHECK_SAFE_LOCATION = ConfigValue.create("Shops.Check_Safe_Location",
        true,
        "Controls whether plugin should check shop's location safety before player teleports to it."
    );

    public static final ConfigValue<Boolean> CHECK_CURRENCY_PERMISSIONS = ConfigValue.create("Shops.Check_Currency_Permission",
        false,
        "Sets whether or not players must have '" + ChestPerms.PREFIX_CURRENCY + "[name]' permissions to use specific currencies from 'Allowed_Currencies' list."
    );

    public static final ConfigValue<Set<String>> ALLOWED_CURRENCIES = ConfigValue.create("Shops.Allowed_Currencies",
        Set.of(CurrencyId.VAULT),
        "A list of currencies that can be used for Chest Shop products."
    ).onRead(set -> Lists.modify(set, CurrencyId::reroute));

    public static final ConfigValue<Set<Material>> ALLOWED_CONTAINERS = ConfigValue.forSet("Shops.Allowed_Containers",
        BukkitThing::getMaterial,
        (cfg, path, set) -> cfg.set(path, set.stream().map(BukkitThing::toString).toList()),
        () -> {
            Set<Material> set = new HashSet<>(/*Tag.SHULKER_BOXES.getValues()*/);
            set.add(Material.CHEST);
            set.add(Material.TRAPPED_CHEST);
            set.add(Material.BARREL);
            set.add(Material.SHULKER_BOX);
            return set;
        },
        "A list of Materials, that can be used for shop creation.",
        "Only 'Container' block materials can be used!",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Container.html"
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

    public static final ConfigValue<Boolean> SHOP_PRODUCT_BYPASS_DETECTION_ENABLED = ConfigValue.create("Shop.Product.Bypass_Plugin_Detection.Enabled",
        false,
        "When enabled, allows players to bypass plugin detection of the item they want to put as a shop product."
    );

    public static final ConfigValue<Boolean> SHOP_PRODUCT_BYPASS_DETECTION_HOLD_SHIFT = ConfigValue.create("Shop.Product.Bypass_Plugin_Detection.Hold_Shift",
        false,
        "When enabled, bypass plugin detection for a shop products will work only when player holds the Shift key (sneaking)."
    );

    public static final ConfigValue<Double> SHOP_PRODUCT_INITIAL_BUY_PRICE = ConfigValue.create("Shops.Product.InitialPrice.Buy",
        10D,
        "Sets initial buy price for new products added in chest shops.");

    public static final ConfigValue<Double> SHOP_PRODUCT_INITIAL_SELL_PRICE = ConfigValue.create("Shops.Product.InitialPrice.Sell",
        2D,
        "Sets initial sell price for new products added in chest shops.");

    public static final ConfigValue<Boolean> SHOP_ITEM_CREATION_ENABLED = ConfigValue.create("Shops.ItemCreation.Enabled",
        false,
        "Sets whether or not players can create shops by placing specific item.");

    public static final ConfigValue<Map<Material, ItemStack>> SHOP_ITEM_CREATION_ITEMS = ConfigValue.forMap("Shops.ItemCreation.Items",
        BukkitThing::getMaterial,
        (cfg, path, material) -> cfg.getItem(path + "." + material),
        (cfg, path, map) -> map.forEach((type, item) -> cfg.setItem(path + "." + BukkitThing.toString(type), item)),
        () -> {
            Map<Material, ItemStack> map = new HashMap<>();
            map.put(Material.CHEST, ChestUtils.getDefaultShopItem(Material.CHEST, "edc36c9cb50a527aa55607a0df7185ad20aabaa903e8d9abfc78260705540def"));
            map.put(Material.BARREL, ChestUtils.getDefaultShopItem(Material.BARREL, "5193c89d1df679854f33c2215247b676a159d5395392d0c61b8476f813d9edb0"));
            map.put(Material.WHITE_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.WHITE_SHULKER_BOX, "7e066c569d4b94e49b23770e46c9a0e1d736711becb702809375e2d5a32f2a99"));
            map.put(Material.LIGHT_GRAY_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.LIGHT_GRAY_SHULKER_BOX, "56a48c4037343731bd5fd1510ca15c573788389f258677a17f014e08aeaa9560"));
            map.put(Material.GRAY_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.GRAY_SHULKER_BOX, "a95bde13c45754468cfc8c3a00133d997362fa7e302b0b0fbc4bd0fca6890059"));
            map.put(Material.BLACK_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.BLACK_SHULKER_BOX, "bf6174c01a67e1eada9db16bb551ddee32dfcbf37611382f88cdb1e62895bab2"));
            map.put(Material.BROWN_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.BROWN_SHULKER_BOX, "b41956931d1f6d1d1b6f82f077b8a265b259f5d29f567869e9955cc6f9a82f12"));
            map.put(Material.RED_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.RED_SHULKER_BOX, "324aa7bf056ccd3d4e63197b04d89f0e9ba79fa6049c503029521724e234054a"));
            map.put(Material.ORANGE_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.ORANGE_SHULKER_BOX, "a83cbb7b98e1954dd2c007ea45975fc7fe1f6ebea7c12d75a578d0960f34996"));
            map.put(Material.YELLOW_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.YELLOW_SHULKER_BOX, "e780feff71541bf6eb4368e726f868871cc549f2599d45cc0e1c729825fee9df"));
            map.put(Material.LIME_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.LIME_SHULKER_BOX, "f45cf042172c9e4e7c8eea570ac8bbd76dc7d8d561ae0f7b60937b3bd4d92e19"));
            map.put(Material.GREEN_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.GREEN_SHULKER_BOX, "efd8881d02fe9cee859ac597731b2df46bee6d52c446205f9da9dfa7bd111694"));
            map.put(Material.CYAN_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.CYAN_SHULKER_BOX, "101facfbdea4980bb0182dcf259341093e22b87375e70b849b88cfad17fc5b27"));
            map.put(Material.LIGHT_BLUE_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.LIGHT_BLUE_SHULKER_BOX, "dbcf2e5a0ee72bbd63b08336918f11730120a49df3557cfa84ff6450fbc4c65c"));
            map.put(Material.BLUE_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.BLUE_SHULKER_BOX, "47d0d86e1f1108468ac150dafa28e7d0619a6e3066cb42994bc6c1866bd267e3"));
            map.put(Material.PURPLE_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.PURPLE_SHULKER_BOX, "30eed20b95873611c2b60e54f8df6f4e6654fb9575fc47e63f63327f9f6c56cf"));
            map.put(Material.MAGENTA_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.MAGENTA_SHULKER_BOX, "83524237183c2ba221ba61923a7130099e37166227cacb1f65cec93b6c33a6a8"));
            map.put(Material.PINK_SHULKER_BOX, ChestUtils.getDefaultShopItem(Material.PINK_SHULKER_BOX, "a374cf299874af600608acfb55169237fda244ccca0e23aa08c2e335bd73406b"));
            return map;
        },
        "Assigns custom shop creation items with specific container types.",
        "These items, when placed, will turn out into assigned container with a shop created on it:",
        "-- Items:",
        "---- <container_type>: # See 'Allowed_Containers'",
        "------ <item settings> # " + WIKI_ITEMS_URL
    );

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
        "Supported Plugins: " + HookId.LANDS + ", " + HookId.GRIEF_PREVENTION + ", " + HookId.WORLD_GUARD + ", " + HookId.KINGDOMS);

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



    public static final ConfigValue<Map<String, ItemStack>> DISPLAY_DEFAULT_SHOWCASE = ConfigValue.forMap("Display.Showcase",
        (cfg, path, id) -> cfg.getItem(path + "." + id),
        (cfg, path, map) -> map.forEach((type, item) -> cfg.setItem(path + "." + type, item)),
        () -> Map.of(
            DEFAULT, new ItemStack(Material.GLASS),
            BukkitThing.toString(Material.CHEST), new ItemStack(Material.WHITE_STAINED_GLASS)
        ),
        "Sets an item that will be used as default shop showcase.",
        "You can provide different showcases for different shop types you set in 'Allowed_Containers' option.",
        "Use '" + DEFAULT + "' keyword to set showcase item for all unlisted here container types.",
        "Available item options including model data: " + WIKI_ITEMS_URL
    );

    public static final ConfigValue<Boolean> DISPLAY_PLAYER_CUSTOMIZATION_ENABLED = ConfigValue.create("Display.PlayerCustomization.Enabled",
        true,
        "Sets whether or not players can customize displays for their shops personally.",
        "This feature can also be regulated by the following permission: '" + ChestPerms.DISPLAY_CUSTOMIZATION.getName() + "."
    );

    public static final ConfigValue<Map<String, ItemStack>> DISPLAY_PLAYER_CUSTOMIZATION_SHOWCASE_LIST = ConfigValue.forMap("Display.PlayerCustomization.Showcases",
        (cfg, path, id) -> cfg.getItem(path + "." + id),
        (cfg, path, map) -> map.forEach((id, item) -> cfg.setItem(path + "." + id, item)),
        () -> {
            Map<String, ItemStack> map = new HashMap<>();
            map.put("glass", new ItemStack(Material.GLASS));
            map.put("white_glass", new ItemStack(Material.WHITE_STAINED_GLASS));
            map.put("lime_glass", new ItemStack(Material.LIME_STAINED_GLASS));
            map.put("gray_glass", new ItemStack(Material.GRAY_STAINED_GLASS));
            map.put("black_glass", new ItemStack(Material.BLACK_STAINED_GLASS));
            map.put("blue_glass", new ItemStack(Material.BLUE_STAINED_GLASS));
            map.put("brown_glass", new ItemStack(Material.BROWN_STAINED_GLASS));
            map.put("cyan_glass", new ItemStack(Material.CYAN_STAINED_GLASS));
            map.put("green_glass", new ItemStack(Material.GREEN_STAINED_GLASS));
            map.put("light_blue_glass", new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS));
            map.put("light_gray_glass", new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS));
            map.put("magenta_glass", new ItemStack(Material.MAGENTA_STAINED_GLASS));
            map.put("orange_glass", new ItemStack(Material.ORANGE_STAINED_GLASS));
            map.put("pink_glass", new ItemStack(Material.PINK_STAINED_GLASS));
            map.put("purple_glass", new ItemStack(Material.PURPLE_STAINED_GLASS));
            map.put("red_glass", new ItemStack(Material.RED_STAINED_GLASS));
            map.put("yellow_glass", new ItemStack(Material.YELLOW_STAINED_GLASS));
            return map;
        },
        "List of items available to be used by players as shop showcases."
    );

    public static final ConfigValue<Integer> DISPLAY_UPDATE_INTERVAL = ConfigValue.create("Display.Update_Interval",
        3,
        "Defines update interval for shop displays (in seconds).");

    public static final ConfigValue<Integer> DISPLAY_VISIBLE_DISTANCE = ConfigValue.create("Display.Visible_Distance",
        10,
        "Sets shop display visibility distance.",
        "Players will see shop displays when they are close enough.");

    public static final ConfigValue<Boolean> DISPLAY_HOLOGRAM_ENABLED = ConfigValue.create("Display.Title.Enabled",
        true,
        "When 'true', creates a client-side hologram above the shop."
    );

    public static final ConfigValue<Boolean> DISPLAY_HOLOGRAM_FORCE_ARMOR_STAND = ConfigValue.create("Display.Hologram.Force_ArmorStands",
        false,
        "When enabled, forces plugin to use Armor Stand entities instead of Display ones."
    );

    public static final ConfigValue<Double> DISPLAY_HOLOGRAM_LINE_GAP = ConfigValue.create("Display.Hologram.LineGap",
        0.3D,
        "Sets distance between hologram lines."
    );

    public static final ConfigValue<List<String>> DISPLAY_HOLOGRAM_TEXT_ADMIN = ConfigValue.create("Display.Title.Values.ADMIN",
        Lists.newList(
            LIGHT_YELLOW.wrap(BOLD.wrap(SHOP_NAME)),
            LIGHT_GRAY.wrap(GENERIC_PRODUCT_NAME),
            GENERIC_BUY + " " + GENERIC_SELL
        ),
        "Sets hologram text format for player and admin shops when both options, buying and selling, are available.",
        "You can use 'Chest Shop' placeholders: " + URL_WIKI_PLACEHOLDERS,
        "Display item name: " + GENERIC_PRODUCT_NAME,
        "Display item price: " + GENERIC_PRODUCT_PRICE.apply(TradeType.BUY) + ", " + GENERIC_PRODUCT_PRICE.apply(TradeType.SELL),
        Plugins.PLACEHOLDER_API + " is also supported here."
    );

    public static final ConfigValue<List<String>> DISPLAY_HOLOGRAM_TEXT_NORMAL = ConfigValue.create("Display.Title.Values.PLAYER",

        Lists.newList(
            LIGHT_YELLOW.wrap(BOLD.wrap(SHOP_NAME)),
            LIGHT_GRAY.wrap(GENERIC_PRODUCT_NAME),
            GENERIC_BUY + " " + GENERIC_SELL,
            LIGHT_GRAY.wrap("Stock: " + LIGHT_YELLOW.wrap(GENERIC_PRODUCT_STOCK))
        ),
        "Sets hologram text format for player and admin shops when both options, buying and selling, are available.",
        "You can use 'Chest Shop' placeholders: " + URL_WIKI_PLACEHOLDERS,
        "Display item name: " + GENERIC_PRODUCT_NAME,
        "Display item price: " + GENERIC_PRODUCT_PRICE.apply(TradeType.BUY) + ", " + GENERIC_PRODUCT_PRICE.apply(TradeType.SELL),
        "Display item stock: " + GENERIC_PRODUCT_STOCK,
        Plugins.PLACEHOLDER_API + " is also supported here."
    );

    public static final ConfigValue<List<String>> DISPLAY_HOLOGRAM_TEXT_RENT = ConfigValue.create("Display.Title.Rent",
        Lists.newList(
            LIGHT_YELLOW.wrap(BOLD.wrap("For Rent")),
            LIGHT_GRAY.wrap(CHEST_SHOP_RENT_DURATION),
            LIGHT_GRAY.wrap(CHEST_SHOP_RENT_PRICE)
        ),
        "Sets hologram text for rentable shops."
    );

    public static final ConfigValue<Map<ShopType, String>> DISPLAY_HOLOGRAM_TEXT_BUY = ConfigValue.forMap("Display.Title.BuyValue",
        str -> StringUtil.getEnum(str, ShopType.class).orElse(null),
        (cfg, path, type) -> cfg.getString(path + "." + type),
        (cfg, path, map) -> map.forEach((type, list) -> cfg.set(path + "." + type.name(), list)),
        Map.of(
            ShopType.ADMIN, LIGHT_GRAY.wrap(GREEN.wrap("B: ") + LIGHT_GREEN.wrap(GENERIC_PRODUCT_PRICE.apply(TradeType.BUY))),
            ShopType.PLAYER, LIGHT_GRAY.wrap(GREEN.wrap("B: ") + LIGHT_GREEN.wrap(GENERIC_PRODUCT_PRICE.apply(TradeType.BUY)))
        ),
        "Sets hologram text to appear for '" + GENERIC_BUY + "' placeholder when buying option is available for displayed product.",
        "Price placeholder: " + GENERIC_PRODUCT_PRICE.apply(TradeType.BUY),
        "All placeholders from 'Title -> Values' option are available here too."
    );

    public static final ConfigValue<Map<ShopType, String>> DISPLAY_HOLOGRAM_TEXT_SELL = ConfigValue.forMap("Display.Title.SellValue",
        str -> StringUtil.getEnum(str, ShopType.class).orElse(null),
        (cfg, path, type) -> cfg.getString(path + "." + type),
        (cfg, path, map) -> map.forEach((type, list) -> cfg.set(path + "." + type.name(), list)),
        Map.of(
            ShopType.ADMIN, LIGHT_GRAY.wrap(RED.wrap("S: ") + LIGHT_RED.wrap(GENERIC_PRODUCT_PRICE.apply(TradeType.SELL))),
            ShopType.PLAYER, LIGHT_GRAY.wrap(RED.wrap("S: ") + LIGHT_RED.wrap(GENERIC_PRODUCT_PRICE.apply(TradeType.SELL)))
        ),
        "Sets hologram text to appear for '" + GENERIC_SELL + "' placeholder when selling option is available for displayed product.",
        "Price placeholder: " + GENERIC_PRODUCT_PRICE.apply(TradeType.SELL),
        "All placeholders from 'Title -> Values' option are available here too."
    );

    public static boolean isAutoBankEnabled() {
        return SHOP_AUTO_BANK.get();
    }

    public static boolean isRentEnabled() {
        return RENT_ENABLED.get();
    }
}
