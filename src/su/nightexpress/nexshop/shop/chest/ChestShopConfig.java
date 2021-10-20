package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.currency.CurrencyType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChestShopConfig {

    public static JYML YML_SHOP_VIEW;
    public static JYML YML_LIST_OWN;
    public static JYML YML_LIST_GLOBAL;
    public static JYML YML_LIST_SEARCH;

    public static ItemStack    DISPLAY_SHOWCASE;
    public static List<String> DISPLAY_TEXT;
    public static int          DISPLAY_SLIDE_TIME;

    public static String DEFAULT_CURRENCY;
    public static Set<String> ALLOWED_CURRENCIES;

    public static  double               SHOP_CREATION_COST_CREATE;
    public static  double               SHOP_CREATION_COST_REMOVE;
    private static Map<String, Integer> SHOP_CREATION_MAX_PER_RANK;
    public static  Set<String>          SHOP_CREATION_WORLD_BLACKLIST;
    public static  boolean              SHOP_CREATION_CLAIM_ONLY;
    public static  Set<String>          SHOP_CREATION_CLAIM_PLUGINS;

    private static Map<String, Integer> SHOP_PRODUCTS_MAX_PER_RANK;
    public static  Set<String>          SHOP_PRODUCT_DENIED_MATERIALS;
    public static  Set<String>          SHOP_PRODUCT_DENIED_LORES;
    public static  Set<String>          SHOP_PRODUCT_DENIED_NAMES;

    public static Sound SOUND_CREATION;
    public static Sound SOUND_REMOVAL;

    public static void load(@NotNull ChestShop chestShop) {
        JYML cfg = chestShop.getConfig();

        chestShop.plugin().getConfigManager().extractFullPath(chestShop.getFullPath() + "editor");
        YML_SHOP_VIEW = JYML.loadOrExtract(chestShop.plugin(), chestShop.getPath() + "view.yml");
        YML_LIST_OWN = JYML.loadOrExtract(chestShop.plugin(), chestShop.getPath() + "list.own.menu.yml");
        YML_LIST_GLOBAL = JYML.loadOrExtract(chestShop.plugin(), chestShop.getPath() + "list.global.menu.yml");
        YML_LIST_SEARCH = JYML.loadOrExtract(chestShop.plugin(), chestShop.getPath() + "list.search.menu.yml");

        String path = "Shops.";
        cfg.addMissing(path + "Default_Currency", CurrencyType.VAULT);

        DEFAULT_CURRENCY = cfg.getString(path + "Default_Currency", CurrencyType.VAULT);
        ALLOWED_CURRENCIES = cfg.getStringSet(path + "Allowed_Currencies").stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        ALLOWED_CURRENCIES.removeIf(currencyId -> {
            IShopCurrency currency = chestShop.plugin().getCurrencyManager().getCurrency(currencyId);
            return currency == null || !currency.hasOfflineSupport();
        });

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
        SHOP_CREATION_CLAIM_PLUGINS = cfg.getStringSet(path + "Plugins");

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
        DISPLAY_SLIDE_TIME = cfg.getInt(path + "Title.Slide_Interval", 3);
        DISPLAY_TEXT = StringUT.color(cfg.getStringList(path + "Title.Values"));

        path = "Sounds.";
        SOUND_CREATION = cfg.getEnum(path + "Create", Sound.class, Sound.BLOCK_NOTE_BLOCK_BELL);
        SOUND_REMOVAL = cfg.getEnum(path + "Remove", Sound.class, Sound.BLOCK_ANVIL_PLACE);

        cfg.saveChanges();
    }

    public static boolean isClaimPlugin(@NotNull String plugin) {
        return SHOP_CREATION_CLAIM_PLUGINS.contains(plugin);
    }

    public static int getMaxShops(@NotNull Player player) {
        return Hooks.getGroupValueInt(player, SHOP_CREATION_MAX_PER_RANK, true);
    }

    public static int getMaxShopProducts(@NotNull Player player) {
        return Hooks.getGroupValueInt(player, SHOP_PRODUCTS_MAX_PER_RANK, true);
    }

    public static boolean isAllowedItem(@NotNull ItemStack item) {
        String type = item.getType().name();
        if (ChestShopConfig.SHOP_PRODUCT_DENIED_MATERIALS.contains(type)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                String name = meta.getDisplayName();
                if (ChestShopConfig.SHOP_PRODUCT_DENIED_NAMES.stream().anyMatch(black -> name.contains(black))) {
                    return false;
                }
            }
            List<String> lore = meta.getLore();
            if (lore != null) {
                if (lore.stream().anyMatch(line -> {
                    return ChestShopConfig.SHOP_PRODUCT_DENIED_LORES.stream().anyMatch(black -> line.contains(black));
                })) {
                    return false;
                }
            }
        }
        return true;
    }
}
