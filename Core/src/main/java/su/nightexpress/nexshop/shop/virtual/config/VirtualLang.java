package su.nightexpress.nexshop.shop.virtual.config;

import org.bukkit.Sound;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;
import su.nightexpress.nexshop.Placeholders;

public class VirtualLang extends EngineLang {

    public static final LangKey COMMAND_EDITOR_DESC = LangKey.of("VirtualShop.Command.Editor.Desc", "Open VirtualShop editor.");

    public static final LangKey COMMAND_OPEN_DESC  = LangKey.of("VirtualShop.Command.Open.Desc", "Opens specified shop.");
    public static final LangKey COMMAND_OPEN_USAGE = LangKey.of("VirtualShop.Command.Open.Usage", "[player]");

    public static final LangKey COMMAND_MENU_DESC  = LangKey.of("VirtualShop.Command.Menu.Desc", "Opens Main Menu.");
    public static final LangKey COMMAND_MENU_USAGE = LangKey.of("VirtualShop.Command.Menu.Usage", "[player]");

    public static final LangKey COMMAND_SHOP_DESC = LangKey.of("VirtualShop.Command.Shop.Desc", "Open specified shop or main menu.");
    public static final LangKey COMMAND_SHOP_USAGE = LangKey.of("VirtualShop.Command.Shop.Usage", "[shopId]");

    public static final LangKey COMMAND_SELL_MENU_DESC = LangKey.of("VirtualShop.Command.SellMenu.Desc", "Open Sell GUI.");
    public static final LangKey COMMAND_SELL_MENU_USAGE = LangKey.of("VirtualShop.Command.SellMenu.Usage", "");

    public static final LangKey SHOP_ERROR_BAD_WORLD    = LangKey.of("VirtualShop.Shop.Error.BadWorld", "&cShop is disabled in this world!");
    public static final LangKey SHOP_ERROR_BAD_GAMEMODE = LangKey.of("VirtualShop.Shop.Error.BadGamemode", "&cYou can't use shop while in &e" + Placeholders.GENERIC_TYPE + " &cgamemode!");
    public static final LangKey SHOP_ERROR_INVALID      = LangKey.of("VirtualShop.Shop.Error.Invalid", "&cNo such shop!");

    public static final LangKey PRODUCT_PURCHASE_SELL    = LangKey.of("VirtualShop.Product.Purchase.Sell",
        "<! type:\"titles:15:60:15\" sound:\"" + Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name() + "\" !>" +
            "\n&a&lSuccessful Sale! " +
            "\n&7You sold &ax" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + " &7for &a" + Placeholders.GENERIC_PRICE + "&7!");

    public static final LangKey PRODUCT_PURCHASE_BUY     = LangKey.of("VirtualShop.Product.Purchase.Buy",
        "<! type:\"titles:15:60:15\" sound:\"" + Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name() + "\" !>" +
            "\n&a&lSuccessful Purchase!" +
            "\n &7You bought &ax" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + " &7for &a" + Placeholders.GENERIC_PRICE  +"&7!");

    public static final LangKey SELL_MENU_SOLD = LangKey.of("VirtualShop.SellMenu.SaleResult",
        "<! prefix:\"false\" sound:\"" + Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name() + "\" !>" +
            "\n&a&lItems Sold:" +
            "\n" + "&7x" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + ": &f" + Placeholders.GENERIC_PRICE);

    public static final LangKey EDITOR_SHOP_CREATE_ERROR_EXIST = LangKey.of("VirtualShop.Editor.Create.Error.Exist", "&cShop with such ID already exist!");
    public static final LangKey EDITOR_ENTER_SHOP_ID           = LangKey.of("VirtualShop.Editor.Enter.Id", "&7Enter &a[Shop Identifier]");
    public static final LangKey EDITOR_ENTER_DESCRIPTION       = LangKey.of("VirtualShop.Editor.Enter.Description", "&7Enter &a[Description]");
    public static final LangKey EDITOR_ENTER_NPC_ID            = LangKey.of("VirtualShop.Editor.Enter.NpcId", "&7Enter &a[NPC ID]");
    public static final LangKey EDITOR_ENTER_TITLE             = LangKey.of("VirtualShop.Editor.Enter.Title", "&7Enter &a[Title]");
    public static final LangKey EDITOR_ENTER_COMMAND           = LangKey.of("VirtualShop.Editor.Enter.Command", "&7Enter &a[Command]");
}
