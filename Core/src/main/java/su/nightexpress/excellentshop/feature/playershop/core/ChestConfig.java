package su.nightexpress.excellentshop.feature.playershop.core;

import org.bukkit.Material;
import su.nightexpress.excellentshop.feature.playershop.ChestUtils;
import su.nightexpress.excellentshop.feature.playershop.impl.Showcase;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nightcore.integration.item.ItemPlugins;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.RankMap;

import java.util.Map;
import java.util.Set;

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

    public static final ConfigValue<Boolean> CHECK_SAFE_LOCATION = ConfigValue.create("Shops.Check_Safe_Location",
        true,
        "Controls whether plugin should check shop's location safety before player teleports to it."
    );

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
        "Supported Plugins: https://nightexpressdev.com/excellentshop/chest/claim-integrations/");

    public static final ConfigValue<Double> PRODUCT_MAX_PRICE = ConfigValue.create("Shops.Products.Max-Price",
        10_000_000D,
        "Max. possible price value players can set for selling and buying.",
        "Set -1 to disable."
    );

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

    public static final ConfigValue<Map<String, Showcase>> SHOWCASE_CATALOG = ConfigValue.forMapById("Display.Showcase.Catalog",
        Showcase::read,
        map -> map.putAll(ChestUtils.getDefaultShowcaseCatalog()),
        "The following showcases will be available for players to select for their shops."
    );

    public static boolean isRentEnabled() {
        return RENT_ENABLED.get();
    }
}
