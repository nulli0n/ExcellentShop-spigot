package su.nightexpress.nexshop.shop.auction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.shop.auction.object.AuctionCategory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AuctionConfig {

    public static DateTimeFormatter DATE_FORMAT;
    public static Set<String>       DISABLED_WORLDS;
    public static Set<String>       DISABLED_GAMEMODES;

    public static  boolean               LISTINGS_ANNOUNCE;
    private static Map<String, Integer>  LISTINGS_PER_RANK;
    public static  double                LISTINGS_PRICE_MIN;
    public static  double                LISTINGS_PRICE_MAX;
    public static  double                LISTINGS_PRICE_TAX;
    public static  Map<String, double[]> LISTINGS_PRICE_PER_MATERIAL;
    public static  Set<String>           LISTINGS_DISABLED_MATERIALS;
    public static  Set<String>           LISTINGS_DISABLED_NAMES;
    public static  Set<String>           LISTINGS_DISABLED_LORES;

    public static long   STORAGE_EXPIRE_IN;
    public static long   STORAGE_DELETE_EXPIRED;
    public static long   STORAGE_SALES_HISTORY;
    public static String STORAGE_IMPORT_PLUGIN;

    public static LinkedHashMap<String, AuctionCategory> CATEGORIES_MAP;
    public static List<AuctionCategory>                  CATEGORIES;

    public static void load(@NotNull AuctionManager manager) {
        JYML cfgCategories = JYML.loadOrExtract(manager.plugin(), manager.getPath() + "categories.yml");
        JYML cfg = manager.getConfig();
		
        /*Map<String, List<String>> categories = new HashMap<>();
        for (Material material : Material.values()) {
            Item item = CraftMagicNumbers.getItem(material);
            if (item == null) continue;

            CreativeModeTab tab = item.t();
            if (tab == null) continue;

            String name = tab.b();
            categories.computeIfAbsent(name, list -> new ArrayList<>()).add(material.name());
        }
        categories.entrySet().forEach(en -> {
            YML_CATEGORIES.set(en.getKey().toLowerCase() + ".Name", en.getKey());
            YML_CATEGORIES.set(en.getKey().toLowerCase() + ".Materials", en.getValue());
        });
        YML_CATEGORIES.saveChanges();*/

        String path = "Settings.";
        DATE_FORMAT = DateTimeFormatter.ofPattern(cfg.getString(path + "Date_Format", "MM/dd/yyyy HH:mm:ss"));
        DISABLED_WORLDS = cfg.getStringSet(path + "disabled-worlds");
        DISABLED_GAMEMODES = cfg.getStringSet(path + "disabled-gamemodes").stream()
                .map(String::toUpperCase).collect(Collectors.toSet());

        path = "Settings.Listings.";
        LISTINGS_ANNOUNCE = cfg.getBoolean(path + "Announce");
        LISTINGS_PER_RANK = new HashMap<>();
        for (String rank : cfg.getSection(path + "Listings_Per_Rank")) {
            LISTINGS_PER_RANK.put(rank.toLowerCase(), cfg.getInt(path + "Listings_Per_Rank." + rank));
        }
        LISTINGS_DISABLED_MATERIALS = cfg.getStringSet(path + "Disabled_Materials").stream()
                .map(String::toUpperCase).collect(Collectors.toSet());
        LISTINGS_DISABLED_NAMES = StringUT.color(cfg.getStringSet(path + "Disabled_Names"));
        LISTINGS_DISABLED_LORES = StringUT.color(cfg.getStringSet(path + "Disabled_Lores"));

        path = "Settings.Listings.Price.";
        LISTINGS_PRICE_MIN = cfg.getDouble(path + "Min", -1D);
        LISTINGS_PRICE_MAX = cfg.getDouble(path + "Max", -1D);
        LISTINGS_PRICE_TAX = cfg.getDouble(path + "Tax", 0D);
        LISTINGS_PRICE_PER_MATERIAL = new HashMap<>();
        for (String mRaw : cfg.getSection(path + "Per_Material")) {
            Material material = Material.getMaterial(mRaw.toUpperCase());
            if (material == null) continue;

            double pMin = cfg.getDouble(path + "Per_Material." + mRaw + ".Min");
            double pMax = cfg.getDouble(path + "Per_Material." + mRaw + ".Max");
            LISTINGS_PRICE_PER_MATERIAL.put(mRaw.toLowerCase(), new double[]{pMin, pMax});
        }

        path = "Settings.Storage.";
        STORAGE_EXPIRE_IN = cfg.getLong(path + "Expire_In", 604800) * 1000L;
        STORAGE_DELETE_EXPIRED = cfg.getLong(path + "Delete_Expired", 604800) * 1000L;
        STORAGE_SALES_HISTORY = cfg.getLong(path + "Sales_History", 604800) * 1000L;
        STORAGE_IMPORT_PLUGIN = cfg.getString(path + "Import_From", Constants.NONE);

        CATEGORIES_MAP = new LinkedHashMap<>();
        for (String sId : cfgCategories.getSection("")) {
            String path2 = sId + ".";
            String cName = cfgCategories.getString(path2 + "Name", sId);
            Set<String> cMaterials = cfgCategories.getStringSet(path2 + "Materials");
            AuctionCategory category = new AuctionCategory(sId, cName, cMaterials);
            CATEGORIES_MAP.put(category.getId(), category);
        }
        CATEGORIES = new ArrayList<>(CATEGORIES_MAP.values());
    }

    public static int getPossibleListings(@NotNull Player player) {
        return Hooks.getGroupValueInt(player, LISTINGS_PER_RANK, true);
    }

    public static double getMaterialMinPrice(@NotNull Material material) {
        return getMaterialPrice(material, 0);
    }

    public static double getMaterialMaxPrice(@NotNull Material material) {
        return getMaterialPrice(material, 1);
    }

    private static double getMaterialPrice(@NotNull Material material, int index) {
        return LISTINGS_PRICE_PER_MATERIAL.getOrDefault(material.name().toLowerCase(), new double[]{-1, -1})[index];
    }

    @Nullable
    public static AuctionCategory getCategoryDefault() {
        List<AuctionCategory> categories = getCategories();
        return categories.isEmpty() ? null : categories.get(0);
    }

    @NotNull
    public static List<AuctionCategory> getCategories() {
        return CATEGORIES;
    }
}
