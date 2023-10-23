package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.PlayerRankMap;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.chest.util.ShopType;

import java.util.*;
import java.util.stream.Collectors;

import static su.nexmedia.engine.utils.Colors.BOLD;
import static su.nexmedia.engine.utils.Colors.YELLOW;

public class ChestConfig {

    public static final JOption<String> EDITOR_TITLE = JOption.create("Shops.Editor_Title", "Shop Editor",
        "Sets title for Editor GUIs."
    ).mapReader(Colorizer::apply);

    public static final JOption<Boolean> DELETE_INVALID_SHOP_CONFIGS = JOption.create("Shops.Delete_Invalid_Shop_Configs", false,
        "Sets whether or not invalid shops (that can not be loaded properly) will be auto deleted.");

    public static final JOption<String> ADMIN_SHOP_NAME = JOption.create("Shops.AdminShop_Name", "AdminShop",
        "Sets the custom name for admin shops instead of default owner's name.");

    public static final JOption<String> DEFAULT_NAME = JOption.create("Shops.Default_Name",
        YELLOW + BOLD + Placeholders.PLAYER_NAME + "'s Shop",
        "Default shop name, that will be used on shop creation."
    ).mapReader(Colorizer::apply);

    public static final JOption<String> DEFAULT_CURRENCY = JOption.create("Shops.Default_Currency", CurrencyManager.VAULT,
        "Sets the default ChestShop currency. It will be used for new products and when no other currencies are available.",
        "IMPORTANT: Make sure you have this currency in 'Allowed_Currencies' list!"
    ).mapReader(String::toLowerCase);

    public static final JOption<Set<String>> ALLOWED_CURRENCIES = JOption.create("Shops.Allowed_Currencies",
        Set.of(CurrencyManager.VAULT),
        "A list of currencies that can be used for Chest Shop products."
    ).mapReader(set -> set.stream().map(String::toLowerCase).collect(Collectors.toSet()));

    public static final JOption<Set<Material>> ALLOWED_CONTAINERS = JOption.forSet("Shops.Allowed_Containers",
        str -> Material.getMaterial(str.toUpperCase()),
        () -> {
            Set<Material> set = new HashSet<>(Tag.SHULKER_BOXES.getValues());
            set.add(Material.CHEST);
            set.add(Material.TRAPPED_CHEST);
            set.add(Material.BARREL);
            return set;
        },
        "A list of Materials, that can be used for shop creation.",
        "Only 'Container' block materials, can be used!",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Container.html"
    ).setWriter((cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()));

    public static final JOption<Boolean> SHOP_AUTO_BANK = JOption.create("Shops.Auto_Bank", true,
        "Sets whether or not player's shop bank will be auto-managed without manual operations.",
        "This means that players will gain/lose their funds instantly for each transaction in their shops.");

    public static final JOption<Boolean> SHOP_OFFLINE_TRANSACTIONS = JOption.create("Shops.OfflineTransactions",
        false,
        "[EXPERIMENTAL FEATURE]",
        "Sets whether or not plugin will try to withdraw / deposit currency directly",
        "to shop owner's account when player is offline and the shop bank don't have enough funds.",
        "Example: Even if you have 'Auto_Bank' option enabled, players still will be unable to sell items",
        "to offline shops until someone purchased something from them (so the bank is filled up a bit).",
        "This setting will fix that.",
        "NOTE: Not all supported currencies (and/or their plugins) are available for this feature.");

    public static final JOption<Double> SHOP_CREATION_COST_CREATE = JOption.create("Shops.Creation.Cost.Create", 0D,
        "Sets how much player have to pay in order to create a chest shop.");

    public static final JOption<Double> SHOP_CREATION_COST_REMOVE = JOption.create("Shops.Creation.Cost.Remove", 0D,
        "Sets how much player have to pay in order to remove a chest shop.");

    public static final JOption<Set<String>> SHOP_CREATION_WORLD_BLACKLIST = JOption.create("Shops.Creation.World_Blacklist",
        Set.of("custom_world", "another_world"),
        "List of worlds, where chest shop creation is not allowed.");

    public static final JOption<PlayerRankMap<Integer>> SHOP_CREATION_MAX_PER_RANK = new JOption<>("Shops.Creation.Max_Shops_Per_Rank",
        (cfg, path, rank) -> PlayerRankMap.read(cfg, path, Integer.class),
        new PlayerRankMap<>(Map.of(
            Placeholders.DEFAULT, 10,
            "vip", 20,
            "admin", -1
        )),
        "Sets how many shops a player with certain rank can create at the same time.",
        "No extra permissions are required. Simply provide your permisson group names.",
        "Use '-1' to make unlimited amount."
    ).mapReader(map -> map.setNegativeBetter(true)).setWriter((cfg, path, map) -> map.write(cfg, path));

    public static final JOption<Boolean> SHOP_CREATION_CLAIM_ONLY = JOption.create("Shops.Creation.In_Player_Claims_Only.Enabled",
        true,
        "Sets whether or not players can create shops in their own claims only.",
        "Supported Plugins: " + HookId.LANDS + ", " + HookId.GRIEF_PREVENTION + ", " + HookId.WORLD_GUARD,
        "For all other plugins it will simply check if player is able to build at that location.");

    public final static JOption<PlayerRankMap<Integer>> SHOP_PRODUCTS_MAX_PER_RANK = new JOption<>("Shops.Products.Max_Products_Per_Shop",
        (cfg, path, rank) -> PlayerRankMap.read(cfg, path, Integer.class),
        new PlayerRankMap<>(Map.of(
            Placeholders.DEFAULT, 5,
            "vip", 7,
            "admin", -1
        )),
        "Sets how many products a player with certain rank can put in a shop at the same time.",
        "No extra permissions are required. Simply provide your permisson group names.",
        "Use '-1' to make unlimited amount."
    ).mapReader(map -> map.setNegativeBetter(true)).setWriter((cfg, path, map) -> map.write(cfg, path));

    public static final JOption<Set<Material>> SHOP_PRODUCT_DENIED_MATERIALS = JOption.forSet("Shops.Products.Material_Blacklist",
        str -> Material.getMaterial(str.toUpperCase()),
        Set.of(),
        "List of Materials that can not be added as shop products.",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html"
    ).setWriter((cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()));

    public static final JOption<Set<String>> SHOP_PRODUCT_DENIED_LORES = JOption.create("Shops.Products.Lore_Blacklist",
        Set.of("fuck", "ass hole bitch"),
        "Items with the following words in their lore will be disallowed from being used as shop products.");

    public static final JOption<Set<String>> SHOP_PRODUCT_DENIED_NAMES = JOption.create("Shops.Products.Name_Blacklist",
        Set.of("shit", "sample text"),
        "Items with the following words in their names will be disallowed from being used as shop products.");


    public static final JOption<Map<String, ItemStack>> DISPLAY_SHOWCASE = new JOption<Map<String, ItemStack>>("Display.Showcase",
        (cfg, path, def) -> {
            return cfg.getSection(path).stream().collect(Collectors.toMap(k -> k, v -> cfg.getItem(path + "." + v)));
        },
        () -> Map.of(
            Placeholders.DEFAULT, new ItemStack(Material.GLASS),
            Material.CHEST.name(), new ItemStack(Material.WHITE_STAINED_GLASS)
        ),
        "Sets an item that will be used as a shop showcase.",
        "You can provide different showcases for different shop types you set in 'Allowed_Containers' option.",
        "Showcase is basically an invisible armor stand with equipped item on the head.",
        "Feel free to use custom-modeled items and such!",
        Placeholders.WIKI_ITEMS_URL
    ).setWriter((cfg, path, map) -> map.forEach((type, item) -> cfg.setItem(path + "." + type, item)));

    public static final JOption<Boolean> DISPLAY_HOLOGRAM_ENABLED = JOption.create("Display.Title.Enabled",
        true,
        "When 'true', creates a client-side hologram above the shop."
    );

    public static final JOption<Integer> DISPLAY_SLIDE_INTERVAL = JOption.create("Display.Title.Slide_Interval", 3,
        "Sets interval (in seconds) between hologram line changes.");

    public static final JOption<Map<ShopType, List<String>>> DISPLAY_TEXT = JOption.forMap("Display.Title.Values",
        str -> StringUtil.getEnum(str, ShopType.class).orElse(null),
        (cfg, path, type) -> cfg.getStringList(path + "." + type),
        Map.of(
            ShopType.ADMIN, Collections.singletonList(Placeholders.SHOP_NAME),
            ShopType.PLAYER, Arrays.asList(Placeholders.SHOP_NAME, "&7Owner: &6" + Placeholders.SHOP_CHEST_OWNER)
        ),
        "Sets hologram lines format for player and admin shops.",
        "You can use 'Chest Shop' placeholders here: " + Placeholders.URL_WIKI_PLACEHOLDERS
    ).setWriter((cfg, path, map) -> map.forEach((type, list) -> cfg.set(path + "." + type.name(), list)));

}
