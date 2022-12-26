package su.nightexpress.nexshop.shop.virtual.config;

import su.nexmedia.engine.api.lang.LangKey;
import su.nightexpress.nexshop.Placeholders;

public class VirtualLang {

    public static final LangKey COMMAND_OPEN_DESC  = LangKey.of("VirtualShop.Command.Open.Desc", "Opens specified shop.");
    public static final LangKey COMMAND_OPEN_USAGE = LangKey.of("VirtualShop.Command.Open.Usage", "[shop] [player]");

    public static final LangKey COMMAND_SELL_MENU_DESC = LangKey.of("VirtualShop.Command.SellMenu.Desc", "Open Sell GUI.");
    public static final LangKey COMMAND_SELL_MENU_USAGE = LangKey.of("VirtualShop.Command.SellMenu.Usage", "");

    public static final LangKey MAIN_MENU_ERROR_DISABLED = LangKey.of("VirtualShop.MainMenu.Error.Disabled", "&cMain shop menu is disabled!");
    public static final LangKey OPEN_ERROR_BAD_WORLD     = LangKey.of("VirtualShop.Open.Error.BadWorld", "&cShop is disabled in this world!");
    public static final LangKey OPEN_ERROR_BAD_GAMEMODE  = LangKey.of("VirtualShop.Open.Error.BadGamemode", "&cYou can't use shop while in &e" + Placeholders.GENERIC_TYPE + " &cgamemode!");
    public static final LangKey OPEN_ERROR_INVALID_SHOP  = LangKey.of("VirtualShop.Open.Error.InvalidShop", "&cNo such shop!");
    public static final LangKey PRODUCT_PURCHASE_SELL    = LangKey.of("VirtualShop.Product.Purchase.Sell", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&a&lSuccessful! \n &7You sold &ax%amount% %item% &7for &a%price%&7!");
    public static final LangKey PRODUCT_PURCHASE_BUY     = LangKey.of("VirtualShop.Product.Purchase.Buy", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&a&lSuccessful Purchase! \n &7You bought &ax%amount% %item% &7for &a%price%&7!");

    public static final LangKey SELL_MENU_SOLD = LangKey.of("VirtualShop.SellMenu.Sold", "<! type:\"titles:10:60:20\" !>&a&lItems Sold!\n&7You sold your items!");

    public static final LangKey EDITOR_ENTER_ID                = LangKey.of("VirtualShop.Editor.Enter.Id", "&7Enter shop id...");
    public static final LangKey EDITOR_ENTER_NAME              = LangKey.of("VirtualShop.Editor.Enter.Name", "&7Enter shop &aname&7...");
    public static final LangKey EDITOR_ENTER_DESCRIPTION       = LangKey.of("VirtualShop.Editor.Enter.Description", "&7Enter &adescription&7...");
    public static final LangKey EDITOR_ENTER_NPC_ID            = LangKey.of("VirtualShop.Editor.Enter.NpcId", "&7Enter Citizens id...");
    public static final LangKey EDITOR_ENTER_TITLE             = LangKey.of("VirtualShop.Editor.Enter.Title", "&7Enter shop title...");
    public static final LangKey EDITOR_ENTER_AMOUNT            = LangKey.of("VirtualShop.Editor.Enter.Amount", "&7Enter new amount...");
    public static final LangKey EDITOR_SHOP_CREATE_ERROR_EXIST = LangKey.of("VirtualShop.Editor.Create.Error.Exist", "&cShop with such ID already exist!");
    public static final LangKey EDITOR_PRODUCT_ENTER_COMMAND   = LangKey.of("VirtualShop.Editor.Product.Enter.Command", "&7Enter new command...");
}
