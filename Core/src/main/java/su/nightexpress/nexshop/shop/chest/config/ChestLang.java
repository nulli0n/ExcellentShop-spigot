package su.nightexpress.nexshop.shop.chest.config;

import su.nexmedia.engine.api.lang.LangKey;

public class ChestLang {

    public static final LangKey COMMAND_LIST_DESC    = new LangKey("ChestShop.Command.List.Desc", "List of your shops.");
    public static final LangKey COMMAND_CREATE_DESC  = new LangKey("ChestShop.Command.Create.Desc", "Creates shop of the chest that you're looking on.");
    public static final LangKey COMMAND_CREATE_USAGE = new LangKey("ChestShop.Command.Create.Usage", "[type]");
    public static final LangKey COMMAND_SEARCH_DESC  = new LangKey("ChestShop.Command.Search.Desc", "Search for shops with specified item.");
    public static final LangKey COMMAND_SEARCH_USAGE = new LangKey("ChestShop.Command.Search.Usage", "<material>");
    public static final LangKey COMMAND_REMOVE_DESC  = new LangKey("ChestShop.Command.Remove.Desc", "Removes the shop from the chest that you're looking at.");

    public static final LangKey SHOP_ERROR_NOT_OWNER        = new LangKey("ChestShop.Shop.Error.NotOwner", "&cYou're not the owner of this shop!");
    public static final LangKey SHOP_ERROR_CURRENCY_INVALID     = new LangKey("ChestShop.Shop.Errpr.Currency.Invalid", "&cThis currency is invalid or is not allowed!");
    public static final LangKey SHOP_CREATION_INFO_DONE          = new LangKey("ChestShop.Shop.Creation.Info.Done", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10; ~sound: BLOCK_NOTE_BLOCK_BELL;}&a&lShop Created!\n&7Use &aShift-Click &7to enter in &aEdit Mode&7.");
    public static final LangKey SHOP_CREATION_ERROR_ALREADY_SHOP = new LangKey("ChestShop.Shop.Creation.Error.AlreadyShop", "This chest is already shop!");
    public static final LangKey SHOP_CREATION_ERROR_NOT_EMPTY   = new LangKey("ChestShop.Shop.Creation.Error.NotEmpty", "Please remove all items from the chest first.");
    public static final LangKey SHOP_CREATION_ERROR_NOT_A_CHEST  = new LangKey("ChestShop.Shop.Creation.Error.NotAChest", "This block is not a chest!");
    public static final LangKey SHOP_CREATION_ERROR_BAD_LOCATION  = new LangKey("ChestShop.Shop.Creation.Error.BadLocation", "You can't create shop here!");
    public static final LangKey SHOP_CREATION_ERROR_LIMIT_REACHED  = new LangKey("ChestShop.Shop.Creation.Error.LimitReached", "You have reached the limit of shops! You can't create more.");
    public static final LangKey SHOP_CREATION_ERROR_BAD_AREA         = new LangKey("ChestShop.Shop.Creation.Error.BadArea", "You can create shops only inside your own claim!");
    public static final LangKey SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS = new LangKey("ChestShop.Shop.Creation.Error.NotEnoughFunds", "You don't have enough funds!");
    public static final LangKey SHOP_CREATION_ERROR_TYPE_PERMISSION = new LangKey("ChestShop.Shop.Creation.Error.TypePermission", "You don't have permission to create this type shops!");
    public static final LangKey SHOP_REMOVAL_INFO_DONE        = new LangKey("ChestShop.Shop.Removal.Info.Done", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10; ~sound: ENTITY_GENERIC_EXPLODE;}&c&lShop Removed.");
    public static final LangKey SHOP_REMOVAL_ERROR_NOT_A_SHOP = new LangKey("ChestShop.Shop.Removal.Error.NotAShop", "This block is not a shop!");
    public static final LangKey SHOP_PRODUCT_ERROR_BAD_ITEM = new LangKey("ChestShop.Product.Error.BadItem", "This item can not be traded!");
    @Deprecated
    public static final LangKey SHOP_LIST_INFO_SWITCH     = new LangKey("ChestShop.ShopList.Info.Switch", "Global shop list: %state%&7.");
    public static final LangKey SHOP_TRADE_BUY_INFO_USER  = new LangKey("ChestShop.Shop.Trade.Buy.Info.User", "{message: ~prefix: false;}&eYou bought &6x%amount% %item% &efor &6%price% &efrom &6%shop_name%&e shop owned by &6%shop_owner%&e.");
    public static final LangKey SHOP_TRADE_BUY_INFO_OWNER  = new LangKey("ChestShop.Shop.Trade.Buy.Info.Owner", "{message: ~prefix: false;}&6%player% &ejust bought &6x%amount% %item% &efor &6%price% &efrom your &6%shop_name%&e shop.");
    public static final LangKey SHOP_TRADE_SELL_INFO_USER       = new LangKey("ChestShop.Shop.Trade.Sell.Info.User", "{message: ~prefix: false;}&eYou sold &6x%amount% %item% &efor &6%price% &eto &6%shop_name%&e shop owned by &6%shop_owner%&e.");
    public static final LangKey SHOP_TRADE_SELL_INFO_OWNER       = new LangKey("ChestShop.Shop.Trade.Sell.Info.Owner", "{message: ~prefix: false;}&6%player% &ejust sold &6x%amount% %item% &efor &6%price% &eto your &6%shop_name%&e shop.");
    public static final LangKey SHOP_BANK_ERROR_INVALID_CURRENCY  = new LangKey("ChestShop.Shop.Bank.Error.InvalidCurrency", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &c&lOperation Failed!
        &7This currency is invalid or is not allowed!
        """);
    public static final LangKey SHOP_BANK_DEPOSIT_SUCCESS          = new LangKey("ChestShop.Shop.Bank.Deposit.Success", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &a&lSuccessful Deposit!
        &7You deposit &a%amount% &7to shop bank!
        """);
    public static final LangKey SHOP_BANK_DEPOSIT_ERROR_NOT_ENOUGH = new LangKey("ChestShop.Shop.Bank.Deposit.Error.NotEnough", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &c&lUnable to Deposit!
        &7You don't have enough funds!
        """);
    public static final LangKey SHOP_BANK_WITHDRAW_SUCCESS          = new LangKey("ChestShop.Shop.Bank.Withdraw.Success", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &a&lSuccessful Withdraw!
        &7You withdraw &a%amount% &7from shop bank!
        """);
    public static final LangKey SHOP_BANK_WITHDRAW_ERROR_NOT_ENOUGH = new LangKey("ChestShop.Shop.Bank.Withdraw.NotEnough", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &c&lUnable to Withdraw!
        &7Bank don't have enough funds!
        """);
    public static final LangKey EDITOR_TIP_NAME             = new LangKey("ChestShop.Editor.Enter.Name", "&7Enter shop name...");
    public static final LangKey EDITOR_TIP_PRODUCT_CURRENCY = new LangKey("ChestShop.Editor.Tip.Product.Currency", """
        &7
        &b&lSelect a Currency &7(Click)&b&l:
        &2▸ {json: ~showText: &7Click to select; ~runCommand: /%currency_id%;}&a%currency_name% &7(&f%currency_id%&7){end-json}
        &7
        """);
    public static final LangKey EDITOR_TIP_BANK_EXCHANGE = new LangKey("ChestShop.Editor.Tip.Bank.Exchange", "&7Enter currency and amount...");
    public static final LangKey EDITOR_TIP_BANK_CURRENCY         = new LangKey("ChestShop.Editor.Tip.Bank.Currency", """
        &7
        &b&lSelect a Currency &7(Click)&b&l:
        &2▸ {json: ~showText: &7Click to select; ~suggestCommand: %currency_id% ;}&a%currency_name% &7(&f%currency_id%&7){end-json}
        &7
        &b&lSome Tips:
        &2▸ &aUse &2'-1'&a to deposit/withdraw all.
        &7
        &b&lExample:
        &2▸ &avault 500
        &7
        """);
    public static final LangKey EDITOR_ERROR_BANK_INVALID_SYNTAX = new LangKey("ChestShop.Editor.Bank.InvalidSyntax", "&7Wrong syntax! Use &c<currency> <amount>");
    public static final LangKey EDITOR_ERROR_NEGATIVE     = new LangKey("ChestShop.Editor.Error.Negative", "&7Number must be positive!");
    public static final LangKey EDITOR_ERROR_PRODUCT_LEFT = new LangKey("ChestShop.Editor.Error.ProductLeft", "&cFirst you have to take all of this product from the chest!");
}
