package su.nightexpress.nexshop.config;

import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.type.ShopClickAction;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public static final String DIR_MENU = "/menu/";
    public static final String DIR_CARTS = "/menu/product_carts/";

    public static final ConfigValue<String> DATE_FORMAT = ConfigValue.create("General.DateFormat",
        "MM/dd/yyyy HH:mm");

    public static final ConfigValue<Integer> SHOP_UPDATE_INTERVAL = ConfigValue.create("General.Shop_Update_Interval",
        60,
        "Sets how often (in seconds) plugin will check shops for possible 'updates', such as:",
        "- Rotation time for Rotating Shops.",
        "- Available discounts for Static Shops.",
        "- Update time for Float prices of both, Virtual and Static Shops.",
        "Do not touch unless you know what and why you're doing.",
        "[Default is 60 seconds]"
    );

    public static final ConfigValue<Boolean> MODULES_VIRTUAL_SHOP_ENABLED = ConfigValue.create("Modules.VirtualShop.Enabled",
        true,
        "Sets whether or not Virtual Shop module is enabled.");

    public static final ConfigValue<String[]> MODULES_VIRTUAL_SHOP_ALIASES = ConfigValue.create("Modules.VirtualShop.Command_Aliases",
        new String[]{"vshop"},
        "Command aliases (names) for the Virtual Shop module. Split with commas.",
        "[*] You must reboot the server to apply changes.");

    public static final ConfigValue<Boolean> MODULES_CHEST_SHOP_ENABLED = ConfigValue.create("Modules.ChestShop.Enabled",
        true,
        "Sets whether or not Chest Shop module is enabled.");

    public static final ConfigValue<String[]> MODULES_CHEST_SHOP_ALIASES = ConfigValue.create("Modules.ChestShop.Command_Aliases",
        new String[]{"chestshop", "cshop", "cs"},
        "Command aliases (names) for the Chest Shop module. Split with commas.",
        "[*] You must reboot the server to apply changes."
    );

    public static final ConfigValue<Boolean> MODULES_AUCTION_ENABLED = ConfigValue.create("Modules.Auction.Enabled",
        true,
        "Sets whether or not Auction module is enabled.");

    public static final ConfigValue<String[]> MODULES_AUCTION_ALIASES = ConfigValue.create("Modules.Auction.Command_Aliases",
        new String[]{"auction", "auc", "ah"},
        "Command aliases (names) for the Auction module. Split with commas.",
        "[*] You must reboot the server to apply changes.");

    public static final ConfigValue<Boolean> GENERAL_BUY_WITH_FULL_INVENTORY = ConfigValue.create("General.Buy_With_Full_Inventory",
        false, "Sets wheter players can purchase items from shop with full inventory.");

    public static final ConfigValue<Boolean> GENERAL_CLOSE_GUI_AFTER_TRADE = ConfigValue.create("General.Close_GUI_After_Trade",
        false, "Sets whether or not Shop GUI should be closed when you sold/bought items.");

    public static final ConfigValue<Boolean> GUI_PLACEHOLDER_API = ConfigValue.create("GUI.Use_PlaceholderAPI",
        false,
        "Sets whether PlaceholderAPI placeholders will be applied to non-product items in Shop GUIs.");

    public static final ConfigValue<Map<ClickType, ShopClickAction>> GUI_CLICK_ACTIONS = ConfigValue.create(
        "GUI.Click_Types",
        (cfg, path, def) -> {
            Map<ClickType, ShopClickAction> map = new HashMap<>();
            for (String typeRaw : cfg.getSection(path)) {
                ClickType clickType = StringUtil.getEnum(typeRaw, ClickType.class).orElse(null);
                ShopClickAction shopClick = cfg.getEnum(path + "." + typeRaw, ShopClickAction.class);
                if (clickType == null || shopClick == null) continue;

                map.put(clickType, shopClick);
            }
            return map;
        },
        (cfg, path, map) -> map.forEach((click, action) -> cfg.set(path + "." + click.name(), action.name())),
        () -> Map.of(ClickType.LEFT, ShopClickAction.BUY_SELECTION, ClickType.RIGHT, ShopClickAction.SELL_SELECTION,
            ClickType.SHIFT_LEFT, ShopClickAction.BUY_SINGLE, ClickType.SHIFT_RIGHT, ShopClickAction.SELL_SINGLE,
            ClickType.SWAP_OFFHAND, ShopClickAction.SELL_ALL),
        "Sets actions for specified clicks on products in shop GUIs.",
        "Allowed click types: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/ClickType.html",
        "Allowed values: " + String.join(", ", Lists.getEnums(ShopClickAction.class))
    );

    public static String[] getVirtualShopAliases() {
        return getAliases(MODULES_VIRTUAL_SHOP_ALIASES.get(), "vshop");
    }

    public static String[] getChestShopAliases() {
        return getAliases(MODULES_CHEST_SHOP_ALIASES.get(), "chestshop");
    }

    public static String[] getAuctionAliases() {
        return getAliases(MODULES_AUCTION_ALIASES.get(), "auction");
    }

    private static String[] getAliases(@NotNull String[] aliases, @NotNull String fallback) {
        return aliases.length == 0 ? new String[]{fallback} : aliases;
    }
}
