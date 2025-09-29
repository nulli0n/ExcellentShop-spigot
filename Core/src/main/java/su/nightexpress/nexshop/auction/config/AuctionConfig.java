package su.nightexpress.nexshop.auction.config;

import org.bukkit.GameMode;
import org.bukkit.Material;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nightcore.integration.item.ItemPlugins;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.RankMap;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AuctionConfig {

    public static final ConfigValue<Set<String>> DISABLED_WORLDS = ConfigValue.create("Settings.Disabled_Worlds",
        Lists.newSet("custom_world", "another_world"),
        "List of worlds where Auction can not be used.",
        "[*] Case sensetive!"
    );

    public static final ConfigValue<Set<GameMode>> DISABLED_GAMEMODES = ConfigValue.forSet("Settings.Disabled_Gamemodes",
        name -> StringUtil.getEnum(name, GameMode.class).orElse(null),
        (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
        Lists.newSet(GameMode.CREATIVE),
        "Players can not add items to the Auction in the following gamemodes.",
        "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/GameMode.html"
    );

    public static final ConfigValue<Boolean> NOTIFY_UNCLAIMED_ON_JOIN = ConfigValue.create("Settings.Notify_Unclaimed_On_Join",
        true,
        "When enabled, players will receive a message about unclaimed listings when they join the server."
    );

    public static final ConfigValue<Boolean> NOTIFY_EXPIRED_ON_JOIN = ConfigValue.create("Settings.Notify_Expired_On_Join",
        true,
        "When enabled, players will receive a message about expired listings when they join the server."
    );

    public static final ConfigValue<Boolean> LISTINGS_HIDE_ATTRIBUTES = ConfigValue.create("Settings.Listings.Hide_Attributes",
        false,
        "When enabled, will hide item attributes (damage, durability, etc.) of auction listings.");

    public static final ConfigValue<Long> LISTINGS_EXPIRE_TIME = ConfigValue.create("Settings.Listings.Expire_In",
        604800L,
        "Sets how long items remain in the Auction before being removed if they are not bought within that time.",
        "Players can return expired items in the Auction GUI during a limited amount of time (see Purge_In).",
        "[*] This setting will not affect existing listings.",
        "[Default is 604800 aka 7 days]"
    );

    public static final ConfigValue<Long> LISTINGS_PURGE_TIME = ConfigValue.create("Settings.Listings.Purge_In",
        604800L,
        "Sets the time (in seconds) for expired and unclaimed items to be completely removed.",
        "Players can claim and return expired items in the Auction GUI.",
        "[*] This setting will not affect existing listings.",
        "[Default is 604800 aka 7 days]"
    );

    public static final ConfigValue<Boolean> LISTINGS_ANNOUNCE = ConfigValue.create("Settings.Listings.Announce",
        true,
        "Sets whether or not a broadcast message will be sent when player adds new listing on the Auction."
    );

    public static final ConfigValue<RankMap<Integer>> LISTINGS_PER_RANK = ConfigValue.create("Settings.Listings.Listings_Per_Rank",
        (cfg, path, def) -> RankMap.readInt(cfg, path, 10),
        (cfg, path, map) -> map.write(cfg, path),
        () -> new RankMap<>(
            RankMap.Mode.RANK, "auction.listings.amount.", 10,
            Map.of(
                "vip", 15,
                "gold", 20,
                "admin", -1
            )
        ),
        "Sets max possible amount of active listings for players depends on their rank or permissions.",
        "Use '-1' for unlimited amount."
    );

    public static final ConfigValue<Boolean> LISINGS_AUTO_CLAIM = ConfigValue.create("Settings.Listings.AutoClaim",
        false,
        "Sets whether or sold listings are auto-claimed when seller is online."
    );

    public static final ConfigValue<Boolean> LISTINGS_FLOOR_PRICE = ConfigValue.create("Settings.Listings.Price.Round_To_Integer",
        false,
        "When 'true', removes decimals from listing's price on sell."
    );

    public static final ConfigValue<Map<String, UniDouble>> LISTINGS_PRICE_PER_CURRENCY = ConfigValue.forMap("Settings.Listings.Price.Per_Currency",
        (cfg, path, id) -> UniDouble.read(cfg, path + "." + id),
        (cfg, path, map) -> map.forEach((id, range) -> range.write(cfg, path + "." + id)),
        () -> Map.of(
            Placeholders.DEFAULT, UniDouble.of(-1, -1),
            CurrencyId.VAULT, UniDouble.of(1, 10_000_000)
        ),
        "Sets min. and max. possible listing price for specified currencies.",
        "Use '-1' for unlimited amount.",
        "Use '" + Placeholders.DEFAULT + "' keyword to define price range for all unlisted currencies.",
        "If listing's currency is not listed here and '" + Placeholders.DEFAULT + "' value is not defined, no limits will be used!"
    );

    public static final ConfigValue<Map<String, UniDouble>> LISTINGS_PRICE_PER_MATERIAL = ConfigValue.forMap("Settings.Listings.Price.Per_Material",
        (cfg, path, id) -> UniDouble.read(cfg, path + "." + id),
        (cfg, path, map) -> map.forEach((id, range) -> range.write(cfg, path + "." + id)),
        () -> Map.of(
            BukkitThing.toString(Material.DIAMOND), UniDouble.of(500, -1),
            BukkitThing.toString(Material.EMERALD), UniDouble.of(300, -1),
            BukkitThing.toString(Material.DIRT), UniDouble.of(1, 15)
        ),
        "Sets min. and max. possible listing price for specified blocks/items.",
        "Use '-1' for unlimited amount.",
        "Item / Block Names: https://minecraft.wiki/w/Java_Edition_data_values -> Blocks / Items -> Resource location column."
    );

    public static final ConfigValue<Double> LISTINGS_SELL_TAX = ConfigValue.create("Settings.Listings.Tax.On_Listing_Add",
        10D,
        "Tax amount (in % of the listing price) the seller have to pay to add an item on the Auction.",
        "Example: Player wants to sell an item for $50, with 10% tax amount he will have to pay $5 to do that."
    );

    public static final ConfigValue<Double> LISTINGS_CLAIM_TAX = ConfigValue.create("Settings.Listings.Tax.On_Listing_Purchase",
        0D,
        "Tax amount (in % of the listing price) that is deducted from the final price when sold listing is claimed.",
        "Example: Player sold item for $50, with 10% tax amount he will claim only $45."
    );

    public static final ConfigValue<Set<String>> LISTINGS_DISABLED_ITEMS = ConfigValue.create("Settings.Listings.Disabled_Materials",
        Set.of(
            BukkitThing.toString(Material.BARRIER),
            BukkitThing.toString(Material.BEDROCK),
            "custom_item_name",
            "flame_sword"
        ),
        "List of items that can not be added as shop products.",
        "Vanilla Names: https://minecraft.wiki/w/Java_Edition_data_values -> Blocks / Items -> Resource location column.",
        "Supported Plugins: " + String.join(", ", ItemPlugins.values())
    );

    public static final ConfigValue<Set<String>> LISTINGS_DISABLED_NAMES = ConfigValue.create("Settings.Listings.Disabled_Names",
        Lists.newSet(
            "badword",
            "bad text"
        ),
        "Items containing the following text in their names can not be added to the Auction."
    );

    public static final ConfigValue<Set<String>> LISTINGS_DISABLED_LORES = ConfigValue.create("Settings.Listings.Disabled_Lores",
        Lists.newSet(
            "badword",
            "bad text line"
        ),
        "Items containing the following text in their lore can not be added to the Auction."
    );

    public static final ConfigValue<Map<Material, Set<Integer>>> LISTINGS_DISABLED_MODELS = ConfigValue.forMap("Settings.Listings.Disabled_Models",
        key -> Material.getMaterial(key.toUpperCase()),
        (cfg, path, key) -> IntStream.of(cfg.getIntArray(path + "." + key)).boxed().collect(Collectors.toSet()),
        (cfg, path, map) -> map.forEach((type, nums) -> cfg.setIntArray(path + "." + type.name(), nums.stream().mapToInt(i -> i).toArray())),
        () -> Map.of(
            Material.NETHERITE_SWORD, Set.of(1001, 1015),
            Material.DIAMOND_HELMET, Set.of(1002, 1016)
        ),
        "List of item's model data values to be disabled from adding in Auction."
    );

    public static final ConfigValue<Boolean> MENU_REOPEN_ON_PURCHASE = ConfigValue.create("Menu.ReOpen_On_Purchase",
        true,
        "Controls whether Auction GUI should re-open on purchase instead of closing."
    );

    public static final ConfigValue<Boolean> MENU_CONTAINER_PREVIEW_ENABLED = ConfigValue.create("Menu.Container_Preview.Enabled",
        true,
        "Sets whether or not container preview feature is available in Auction GUI.",
        "This allows players to see content of Shulker Boxes, Chests, Barrels, etc. before purchase.",
        "Uses Right Mouse Button (RMB).");

    public static final ConfigValue<String> MENU_CONTAINER_PREVIEW_TITLE = ConfigValue.create("Menu.Container_Preview.Title",
        "Preview",
        "Sets title for the Preview GUI."
    );

}
