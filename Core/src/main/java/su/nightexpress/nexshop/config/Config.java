package su.nightexpress.nexshop.config;

import org.bukkit.event.inventory.ClickType;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.api.type.ShopClickAction;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public static final String DIR_MENU = "/menu/";

    public static final JOption<Boolean> MODULES_VIRTUAL_SHOP_ENABLED = JOption.create("Modules.VirtualShop.Enabled", true,
        "Sets whether or not Virtual Shop module is enabled.");

    public static final JOption<Boolean> MODULES_CHEST_SHOP_ENABLED = JOption.create("Modules.ChestShop.Enabled", true,
        "Sets whether or not Chest Shop module is enabled.");

    public static final JOption<Boolean> MODULES_AUCTION_ENABLED = JOption.create("Modules.Auction.Enabled", true,
        "Sets whether or not Auction module is enabled.");

    public static final JOption<Boolean> GENERAL_BUY_WITH_FULL_INVENTORY = JOption.create("General.Buy_With_Full_Inventory",
        false, "Sets wheter players can purchase items from shop with full inventory.");

    public static final JOption<Boolean> GUI_PLACEHOLDER_API = JOption.create("GUI.Use_PlaceholderAPI", false,
        "Sets whether PlaceholderAPI placeholders will be applied to non-product items in Shop GUIs.");

    public static final JOption<Map<ClickType, ShopClickAction>> GUI_CLICK_ACTIONS = new JOption<>(
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
        Map.of(ClickType.LEFT, ShopClickAction.BUY_SELECTION, ClickType.RIGHT, ShopClickAction.SELL_SELECTION,
            ClickType.SHIFT_LEFT, ShopClickAction.BUY_SINGLE, ClickType.SHIFT_RIGHT, ShopClickAction.SELL_SINGLE,
            ClickType.SWAP_OFFHAND, ShopClickAction.SELL_ALL),
        "Sets actions for specified clicks on products in shop GUIs.",
        "Allowed click types: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/ClickType.html",
        "Allowed values: " + String.join(", ", CollectionsUtil.getEnumsList(ShopClickAction.class))
    ).setWriter((cfg, path, map) -> map.forEach((click, action) -> cfg.set(path + "." + click.name(), action.name())));
}
