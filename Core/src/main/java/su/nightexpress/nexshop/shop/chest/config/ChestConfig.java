package su.nightexpress.nexshop.shop.chest.config;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.currency.CurrencyId;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.type.ChestShopType;

import java.util.*;
import java.util.stream.Collectors;

public class ChestConfig {

    public static  ItemStack                        DISPLAY_SHOWCASE;
    public static final JOption<Boolean> DISPLAY_HOLOGRAM_ENABLED = JOption.create("Display.Title.Enabled", true, "When 'true', creates a client-side hologram above the shop.");
    private static Map<ChestShopType, List<String>> DISPLAY_TEXT;
    public static  int                              DISPLAY_SLIDE_INTERVAL;

    public static final JOption<String> EDITOR_TITLE = JOption.create("Shops.Editor_Title", "Shop Editor",
        "Sets title for Editor GUIs."
    );
    public static boolean        DELETE_INVALID_SHOP_CONFIGS;
    public static ICurrency      DEFAULT_CURRENCY;
    public static final JOption<Set<String>> ALLOWED_CONTAINERS = new JOption<Set<String>>("Shops.Allowed_Containers",
        (cfg, path, def) -> cfg.getStringSet(path).stream().map(String::toUpperCase).collect(Collectors.toSet()),
        () -> Sets.newHashSet(Material.CHEST.name(), Material.TRAPPED_CHEST.name(), Material.BARREL.name(), Material.SHULKER_BOX.name()),
        "A list of Materials, that can be used for shop creation.",
        "Allowed: CHEST, TRAPPED_CHEST, BARREL, SHULKER_BOX (and colored ones).",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html"
    );
    public static Set<ICurrency> ALLOWED_CURRENCIES;
    public static String         ADMIN_SHOP_NAME;
    public static final JOption<String> DEFAULT_NAME = JOption.create("Shops.Default_Name",
        "&a" + Placeholders.Player.NAME + "'s Shop",
        "Default shop name, that will be used on shop creation."
    );

    public static  double               SHOP_CREATION_COST_CREATE;
    public static  double               SHOP_CREATION_COST_REMOVE;
    private static Map<String, Integer> SHOP_CREATION_MAX_PER_RANK;
    public static  Set<String>          SHOP_CREATION_WORLD_BLACKLIST;
    public static  boolean              SHOP_CREATION_CLAIM_ONLY;

    private static Map<String, Integer> SHOP_PRODUCTS_MAX_PER_RANK;
    public static  Set<String>          SHOP_PRODUCT_DENIED_MATERIALS;
    public static  Set<String>          SHOP_PRODUCT_DENIED_LORES;
    public static  Set<String>          SHOP_PRODUCT_DENIED_NAMES;

    public static void load(@NotNull ChestShopModule chestShop) {
        ExcellentShop plugin = chestShop.plugin();
        JYML cfg = chestShop.getConfig();
        cfg.initializeOptions(ChestConfig.class);

        cfg.addMissing("Shops.Default_Currency", CurrencyId.VAULT);
        cfg.addMissing("Shops.Delete_Invalid_Shop_Configs", false);
        cfg.addMissing("Shops.AdminShop_Name", "MyServerCraft");
        if (!cfg.isConfigurationSection("Display.Title.Values")) {
            cfg.remove("Display.Title.Values");
        }
        cfg.addMissing("Display.Title.Values." + ChestShopType.PLAYER, Arrays.asList("&a%shop_name%", "&7Owner: &6%shop_owner%"));
        cfg.addMissing("Display.Title.Values." + ChestShopType.ADMIN, Arrays.asList("&a%shop_name%", "&7Server Shop"));

        String path = "Shops.";
        DELETE_INVALID_SHOP_CONFIGS = cfg.getBoolean(path + "Delete_Invalid_Shop_Configs", false);
        DEFAULT_CURRENCY = plugin.getCurrencyManager().getCurrency(cfg.getString(path + "Default_Currency", CurrencyId.VAULT));
        ALLOWED_CURRENCIES = cfg.getStringSet(path + "Allowed_Currencies").stream().map(String::toLowerCase)
            .map(currencyId -> plugin.getCurrencyManager().getCurrency(currencyId))
            .filter(Objects::nonNull).collect(Collectors.toSet());
        ADMIN_SHOP_NAME = StringUtil.color(cfg.getString(path + "AdminShop_Name", "MyServerCraft"));

        path = "Shops.Creation.";
        SHOP_CREATION_COST_CREATE = cfg.getDouble(path + "Cost.Create");
        SHOP_CREATION_COST_REMOVE = cfg.getDouble(path + "Cost.Remove");
        SHOP_CREATION_WORLD_BLACKLIST = cfg.getStringSet(path + "World_Blacklist");

        SHOP_CREATION_MAX_PER_RANK = new HashMap<>();
        for (String rank : cfg.getSection(path + "Max_Shops_Per_Rank")) {
            SHOP_CREATION_MAX_PER_RANK.put(rank.toLowerCase(), cfg.getInt(path + "Max_Shops_Per_Rank." + rank));
        }

        path = "Shops.Creation.In_Player_Claims_Only.";
        SHOP_CREATION_CLAIM_ONLY = cfg.getBoolean(path + "Enabled");

        path = "Shops.Products.";
        SHOP_PRODUCTS_MAX_PER_RANK = new HashMap<>();
        for (String rank : cfg.getSection(path + "Max_Products_Per_Shop")) {
            SHOP_PRODUCTS_MAX_PER_RANK.put(rank.toLowerCase(), cfg.getInt(path + "Max_Products_Per_Shop." + rank));
        }

        SHOP_PRODUCT_DENIED_MATERIALS = cfg.getStringSet(path + "Material_Blacklist").stream()
            .map(String::toUpperCase).collect(Collectors.toSet());
        SHOP_PRODUCT_DENIED_LORES = cfg.getStringSet(path + "Lore_Blacklist");
        SHOP_PRODUCT_DENIED_NAMES = cfg.getStringSet(path + "Name_Blacklist");

        path = "Display.";
        DISPLAY_SHOWCASE = cfg.getItem(path + "Showcase");
        DISPLAY_SLIDE_INTERVAL = cfg.getInt(path + "Title.Slide_Interval", 3);
        DISPLAY_TEXT = new HashMap<>();
        for (ChestShopType type : ChestShopType.values()) {
            DISPLAY_TEXT.put(type, StringUtil.color(cfg.getStringList(path + "Title.Values." + type.name())));
        }

        cfg.saveChanges();
    }

    public static int getMaxShops(@NotNull Player player) {
        return Hooks.getGroupValueInt(player, SHOP_CREATION_MAX_PER_RANK, true);
    }

    public static int getMaxShopProducts(@NotNull Player player) {
        return Hooks.getGroupValueInt(player, SHOP_PRODUCTS_MAX_PER_RANK, true);
    }

    @NotNull
    public static List<String> getDisplayText(@NotNull ChestShopType chestType) {
        return DISPLAY_TEXT.getOrDefault(chestType, Collections.emptyList());
    }

    public static boolean isAllowedItem(@NotNull ItemStack item) {
        String type = item.getType().name();
        if (ChestConfig.SHOP_PRODUCT_DENIED_MATERIALS.contains(type)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                String name = meta.getDisplayName();
                if (ChestConfig.SHOP_PRODUCT_DENIED_NAMES.stream().anyMatch(name::contains)) {
                    return false;
                }
            }
            List<String> lore = meta.getLore();
            if (lore != null) {
                return lore.stream().noneMatch(line -> SHOP_PRODUCT_DENIED_LORES.stream().anyMatch(line::contains));
            }
        }
        return true;
    }
}
