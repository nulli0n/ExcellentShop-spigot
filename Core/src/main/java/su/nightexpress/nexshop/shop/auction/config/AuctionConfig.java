package su.nightexpress.nexshop.shop.auction.config;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.PlayerRankMap;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.auction.AuctionCategory;
import su.nightexpress.nexshop.shop.auction.AuctionManager;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AuctionConfig {

    public static DateTimeFormatter DATE_FORMAT;
    public static Set<String>       DISABLED_WORLDS;
    public static Set<String>       DISABLED_GAMEMODES;

    public static  Map<String, AuctionCurrencySetting> CURRENCIES;

    public static long     LISTINGS_EXPIRE_IN;
    public static long     LISTINGS_PURGE_IN;
    public static  boolean                             LISTINGS_ANNOUNCE;
    private static PlayerRankMap<Integer>                       LISTINGS_PER_RANK;

    public static final JOption<Boolean> LISINGS_AUTO_CLAIM = JOption.create("Settings.Listings.AutoClaim", false,
        "Sets whether or not profits from sold listings will be auto-claimed when player is online.");

    public static final JOption<Boolean> LISTINGS_PRICE_ROUND_TO_INT = JOption.create("Settings.Listings.Price.Round_To_Integer", false,
        "When 'true', removes decimals from listing's price on sell.");

    private static Map<String, double[]> LISTINGS_PRICE_PER_CURRENCY;
    private static  Map<String, double[]> LISTINGS_PRICE_PER_MATERIAL;
    public static  double                LISTINGS_TAX_ON_LISTING_ADD;
    public static  double                LISTINGS_TAX_ON_LISTING_PURCHASE;
    public static  Set<String>           LISTINGS_DISABLED_MATERIALS;
    public static  Set<String>           LISTINGS_DISABLED_NAMES;
    public static  Set<String>           LISTINGS_DISABLED_LORES;

    public static final JOption<Map<Material, Set<Integer>>> LISTINGS_DISABLED_MODELS = JOption.forMap("Settings.Listings.Disabled_Models",
        key -> Material.getMaterial(key.toUpperCase()),
        (cfg, path, key) -> IntStream.of(cfg.getIntArray(path + "." + key)).boxed().collect(Collectors.toSet()),
        () -> Map.of(Material.NETHERITE_SWORD, Set.of(1001, 1015)),
        "List of item's model data values to be disabled from adding in Auction.",
        "Syntax:",
        "  ITEM_TYPE: 1,2,3,4,etc",
        "Example:",
        "  NETHERITE_SWORD: 1001,1015"
    ).setWriter((cfg, path, map) -> map.forEach((type, nums) -> cfg.setIntArray(path + "." + type.name(), nums.stream().mapToInt(i -> i).toArray())));

    public static final JOption<Boolean> MENU_CONTAINER_PREVIEW_ENABLED = JOption.create("Menu.Container_Preview.Enabled", true,
        "Sets whether or not container preview feature is available in Auction GUI.",
        "This allows players to see content of Shulker Boxes, Chests, Barrels, etc. before purchase.",
        "Uses Right Mouse Button (RMB).");

    public static final JOption<String> MENU_CONTAINER_PREVIEW_TITLE = JOption.create("Menu.Container_Preview.Title",
        "Preview",
        "Sets title for the Preview GUI."
    ).mapReader(Colorizer::apply);

    public static final JOption<Integer> MENU_UPDATE_INTERVAL = JOption.create("Menu.Update_Interval", 1,
        "Sets how often auction menus will be refreshed to players who are viewing them.",
        "Set this to -1 to disable refreshing task. Then menus will be updated only when there are listings changes.");

    public static Map<String, AuctionCategory> CATEGORIES_MAP;

    public static void load(@NotNull AuctionManager manager) {
        JYML cfgCategories = JYML.loadOrExtract(manager.plugin(), manager.getLocalPath(), "categories.yml");
        JYML cfg = manager.getConfig();
        cfg.initializeOptions(AuctionConfig.class);

        /*Map<String, List<String>> categories = V1_19_R1.getItemsWithCategory();
        categories.forEach((catName, catItems) -> {
            cfgCategories.set(catName.toLowerCase() + ".Name", catName);
            cfgCategories.set(catName.toLowerCase() + ".Materials", catItems);
        });
        cfgCategories.saveChanges();*/

        manager.plugin().getCurrencyManager().getCurrencies().forEach(currency -> {
            String path2 = "Settings.Currency." + currency.getId() + ".";
            cfg.addMissing(path2 + "Default", false);
            cfg.addMissing(path2 + "Enabled", true);
            cfg.addMissing(path2 + "Need_Permission", false);

            String path3 = "Settings.Listings.Price.Per_Currency." + currency.getId() + ".";
            cfg.addMissing(path3 + "Min", -1D);
            cfg.addMissing(path3 + "Max", -1D);
        });
        cfg.addMissing("Settings.Listings.Expire_In", 604800L);
        cfg.addMissing("Settings.Listings.Tax.On_Listing_Add", cfg.getDouble("Settings.Listings.Price.Tax", 10D));
        cfg.addMissing("Settings.Listings.Tax.On_Listing_Purchase", 0D);
        cfg.remove("Settings.Listings.Price.Min");
        cfg.remove("Settings.Listings.Price.Max");
        cfg.remove("Settings.Listings.Price.Tax");
        cfg.remove("Settings.Listings.Price.Tax_After_Purchase");
        cfg.saveChanges();

        String path = "Settings.";
        DATE_FORMAT = DateTimeFormatter.ofPattern(cfg.getString(path + "Date_Format", "MM/dd/yyyy HH:mm:ss"));
        DISABLED_WORLDS = cfg.getStringSet(path + "Disabled_Worlds");
        DISABLED_GAMEMODES = cfg.getStringSet(path + "Disabled_Gamemodes").stream()
                .map(String::toUpperCase).collect(Collectors.toSet());

        CURRENCIES = new HashMap<>();
        for (String curId : cfg.getSection(path + "Currency")) {
            String path2 = path + "Currency." + curId + ".";
            boolean isDefault = cfg.getBoolean(path2 + "Default");
            boolean isEnabled = cfg.getBoolean(path2 + "Enabled");
            boolean isPermRequired = cfg.getBoolean(path2 + "Need_Permission");

            if (!isEnabled) continue;

            Currency currency = manager.plugin().getCurrencyManager().getCurrency(curId);
            if (currency == null) {
                manager.error("Invalid/Unknown currency provided: '" + curId + "'. Ignoring...");
                continue;
            }

            AuctionCurrencySetting setting = new AuctionCurrencySetting(currency, isDefault, isPermRequired);
            CURRENCIES.put(setting.getCurrency().getId(), setting);
        }

        path = "Settings.Listings.";
        LISTINGS_EXPIRE_IN = cfg.getLong(path + "Expire_In", 604800) * 1000L;
        LISTINGS_PURGE_IN = TimeUnit.MILLISECONDS.convert(cfg.getLong("Database.Purge.For_Period", 30), TimeUnit.DAYS);
        LISTINGS_ANNOUNCE = cfg.getBoolean(path + "Announce");
        LISTINGS_PER_RANK = PlayerRankMap.read(cfg, path + "Listings_Per_Rank", Integer.class).setNegativeBetter(true);
        LISTINGS_DISABLED_MATERIALS = cfg.getStringSet(path + "Disabled_Materials").stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        LISTINGS_DISABLED_NAMES = Colorizer.apply(cfg.getStringSet(path + "Disabled_Names"));
        LISTINGS_DISABLED_LORES = Colorizer.apply(cfg.getStringSet(path + "Disabled_Lores"));

        path = "Settings.Listings.Price.";

        LISTINGS_PRICE_PER_CURRENCY = new HashMap<>();
        for (String curId : cfg.getSection(path + "Per_Currency")) {
            Currency currency = manager.plugin().getCurrencyManager().getCurrency(curId);
            if (currency == null || !manager.getCurrencies().contains(currency)) continue;

            double pMin = cfg.getDouble(path + "Per_Currency." + curId + ".Min", -1);
            double pMax = cfg.getDouble(path + "Per_Currency." + curId + ".Max", -1);
            LISTINGS_PRICE_PER_CURRENCY.put(curId.toLowerCase(), new double[]{pMin, pMax});
        }

        LISTINGS_PRICE_PER_MATERIAL = new HashMap<>();
        for (String mRaw : cfg.getSection(path + "Per_Material")) {
            Material material = Material.getMaterial(mRaw.toUpperCase());
            if (material == null) continue;

            double pMin = cfg.getDouble(path + "Per_Material." + mRaw + ".Min");
            double pMax = cfg.getDouble(path + "Per_Material." + mRaw + ".Max");
            LISTINGS_PRICE_PER_MATERIAL.put(mRaw.toLowerCase(), new double[]{pMin, pMax});
        }

        path = "Settings.Listings.Tax.";
        LISTINGS_TAX_ON_LISTING_ADD = cfg.getDouble(path + "On_Listing_Add", 0D);
        LISTINGS_TAX_ON_LISTING_PURCHASE = cfg.getDouble(path + "On_Listing_Purchase", 0D);

        CATEGORIES_MAP = new HashMap<>();
        for (String sId : cfgCategories.getSection("")) {
            String path2 = sId + ".";
            String cName = cfgCategories.getString(path2 + "Name", sId);
            ItemStack cIcon = cfgCategories.getItem(path2 + "Icon");
            Set<String> cMaterials = cfgCategories.getStringSet(path2 + "Materials");
            AuctionCategory category = new AuctionCategory(sId, cName, cIcon, cMaterials);
            CATEGORIES_MAP.put(category.getId(), category);
        }

        cfg.saveChanges();
    }

    public static int getPossibleListings(@NotNull Player player) {
        return LISTINGS_PER_RANK.getBestValue(player, 0);
    }

    public static double getMaterialPriceMin(@NotNull Material material) {
        return getMaterialPriceLimit(material, 0);
    }

    public static double getMaterialPriceMax(@NotNull Material material) {
        return getMaterialPriceLimit(material, 1);
    }

    private static double getMaterialPriceLimit(@NotNull Material material, int index) {
        return LISTINGS_PRICE_PER_MATERIAL.getOrDefault(material.name().toLowerCase(), new double[]{-1, -1})[index];
    }

    public static double getCurrencyPriceMin(@NotNull Currency currency) {
        return getCurrencyPriceLimit(currency, 0);
    }

    public static double getCurrencyPriceMax(@NotNull Currency currency) {
        return getCurrencyPriceLimit(currency, 1);
    }

    private static double getCurrencyPriceLimit(@NotNull Currency currency, int index) {
        return LISTINGS_PRICE_PER_CURRENCY.getOrDefault(currency.getId(), new double[]{-1, -1})[index];
    }

    @NotNull
    public static Collection<AuctionCategory> getCategories() {
        return CATEGORIES_MAP.values();
    }
}
