package su.nightexpress.nexshop.shop.virtual.lang;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.IconLocale;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class VirtualIconsLang implements LangContainer {

    private static final String PREFIX = "VirtualShop.Editor.Icon.";

    @NotNull
    private static IconLocale.Builder builder(@NotNull String key) {
        return LangEntry.iconBuilder(PREFIX + key);
    }

    public static final IconLocale ICON_ADD_SHOP = builder("AddShop")
        .accentColor(GREEN)
        .name("Add Shop")
        .appendInfo("Create a new GUI shop.").br()
        .appendClick("Click to create")
        .build();

    public static final IconLocale ICON_SHOP = builder("Shop")
        .name(SHOP_NAME)
        .appendCurrent("ID", SHOP_ID, WHITE)
        .appendCurrent("Items", SHOP_PRODUCTS)
        .appendCurrent("Pages", VIRTUAL_SHOP_PAGES)
        .br()
        .appendClick("Click to edit")
        .build();

    public static final IconLocale ICON_SHOP_NAME = builder("ShopName")
        .name("Name")
        .appendCurrent("Current", SHOP_NAME).br()
        .appendClick("Click to change")
        .build();

    public static final IconLocale ICON_SHOP_DESCRIPTION = builder("ShopDescription")
        .name("Description")
        .appendInfo(VIRTUAL_SHOP_DESCRIPTION).br()
        .appendClick("Click to change")
        .build();

    public static final IconLocale ICON_SHOP_ICON = builder("ShopIcon")
        .name("Icon")
        .appendInfo("Just a shop icon.").br()
        .appendClick("Click inv. item to set")
        .build();

    public static final IconLocale ICON_SHOP_ALIASES = builder("ShopAliases")
        .name("Aliases")
        .appendInfo(VIRTUAL_SHOP_ALIASES).br()
        .appendInfo("Add custom aliases for the shop.").br()
        .appendInfo("Each alias becomes a server command", "that opens this shop.").br()
        .appendClick("Click to change")
        .build();

    public static final IconLocale ICON_SHOP_SLOTS = builder("ShopSlots")
        .name("Menu Slots")
        .appendCurrent("Current", VIRTUAL_SHOP_MENU_SLOTS).br()
        .appendClick("Click to change")
        .build();

    public static final IconLocale ICON_SHOP_PAGES = builder("ShopPages")
        .name("Pages Amount")
        .appendCurrent("Current", VIRTUAL_SHOP_PAGES).br()
        .appendClick("Click to change")
        .build();
}
