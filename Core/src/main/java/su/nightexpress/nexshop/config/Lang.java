package su.nightexpress.nexshop.config;

import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;


public class Lang extends EngineLang {

    public static final LangKey COMMAND_CURRENCY_DESC = LangKey.of("Command.Currency.Desc", "Manage plugin currencies.");
    public static final LangKey COMMAND_CURRENCY_USAGE = LangKey.of("Command.Currency.Usage", "[help]");

    public static final LangKey COMMAND_CURRENCY_GIVE_DESC = LangKey.of("Command.Currency.Give.Desc", "Give specified currency to a player.");
    public static final LangKey COMMAND_CURRENCY_GIVE_USAGE = LangKey.of("Command.Currency.Give.Usage", "<currency> <player> <amount>");
    public static final LangKey COMMAND_CURRENCY_GIVE_DONE = LangKey.of("Command.Currency.Give.Done", "Given &ax%amount% %currency_name%&7 to &a%player_name%&7.");

    public static final LangKey COMMAND_CURRENCY_TAKE_DESC = LangKey.of("Command.Currency.Take.Desc", "Take specified currency from a player.");
    public static final LangKey COMMAND_CURRENCY_TAKE_USAGE = LangKey.of("Command.Currency.Take.Usage", "<currency> <player> <amount>");
    public static final LangKey COMMAND_CURRENCY_TAKE_DONE = LangKey.of("Command.Currency.Take.Done", "Took &ax%amount% %currency_name%&7 from &a%player_name%&7.");

    public static final LangKey COMMAND_CURRENCY_CREATE_DESC = LangKey.of("Command.Currency.Create.Desc", "Create/replace a currency from/with the item in hand.");
    public static final LangKey COMMAND_CURRENCY_CREATE_USAGE = LangKey.of("Command.Currency.Create.Usage", "<name>");
    public static final LangKey COMMAND_CURRENCY_CREATE_DONE_NEW = LangKey.of("Command.Currency.Create.Done.New", "Created a new currency &a%currency_id%&7 as &a%item%&7.");
    public static final LangKey COMMAND_CURRENCY_CREATE_DONE_REPLACE = LangKey.of("Command.Currency.Create.Done.Replace", "Replaced item in the currency &a%currency_id%&7 with &a%item%&7.");
    public static final LangKey COMMAND_CURRENCY_CREATE_ERROR_EXIST = LangKey.of("Command.Currency.Create.Error.Exist", "Currency &c%currency_id%&7 is already exist and is not an Item Currency.");

    public static final LangKey Module_Cmd_Reload = new LangKey("Module.Cmd.Reload", "Module &a%module% &7reloaded!");

    public static final LangKey Editor_Enter_Command  = new LangKey("Editor.Enter.Command", "&7Enter new command...");
    public static final LangKey Editor_Enter_Price    = new LangKey("Editor.Enter.Price", "&7Enter new price...");
    public static final LangKey Editor_Enter_Currency = new LangKey("Editor.Enter.Currency", "&7Enter currency id...");

    public static final LangKey ERROR_CURRENCY_INVALID = LangKey.of("Error.Currency.Invalid", "&cInvalid currency!");

    public static final LangKey Shop_Product_Error_Unbuyable      = new LangKey("Shop.Product.Error.Unbuyable", "&cYou can not buy this item.");
    public static final LangKey Shop_Product_Error_Unsellable     = new LangKey("Shop.Product.Error.Unsellable", "&cYou can not sell this item!");
    public static final LangKey Shop_Product_Error_OutOfStock = new LangKey("Shop.Product.Error.OutOfStock", "&cThis product is out of stock!");
    public static final LangKey Shop_Product_Error_OutOfSpace = new LangKey("Shop.Product.Error.OutOfSpace", "&cThis shop is out of space!");
    public static final LangKey Shop_Product_Error_OutOfFunds = new LangKey("Shop.Product.Error.OutOfFunds", "This shop is out of money!");
    public static final LangKey Shop_Product_Error_FullStock  = new LangKey("Shop.Product.Error.FullStock", "&cThis product is full of stock!");
    public static final LangKey Shop_Product_Error_FullInventory  = new LangKey("Shop.Product.Error.FullInventory", "&cYou can't buy items while your inventory is full!");
    public static final LangKey Shop_Product_Error_TooExpensive   = new LangKey("Shop.Product.Error.TooExpensive", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&c&lToo Expensive! \n &7You need: &c%price%&7!");
    public static final LangKey Shop_Product_Error_NotEnoughItems = new LangKey("Shop.Product.Error.NotEnoughItems", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&c&lNot Enough Items! \n &7You need: &cx%amount% %item%&7!");

    public static final LangKey Virtual_Shop_Command_Open_Desc  = new LangKey("VirtualShop.Command.Open.Desc", "Opens specified shop.");
    public static final LangKey Virtual_Shop_Command_Open_Usage = new LangKey("VirtualShop.Command.Open.Usage", "[shop] [player]");

    public static final LangKey Virtual_Shop_MainMenu_Error_Disabled = new LangKey("VirtualShop.MainMenu.Error.Disabled", "&cMain shop menu is disabled!");
    public static final LangKey Virtual_Shop_Open_Error_BadWorld     = new LangKey("VirtualShop.Open.Error.BadWorld", "&cShop is disabled in this world!");
    public static final LangKey Virtual_Shop_Open_Error_BadGamemode  = new LangKey("VirtualShop.Open.Error.BadGamemode", "&cYou can't use shop while in &e%mode% &cgamemode!");
    public static final LangKey Virtual_Shop_Open_Error_InvalidShop  = new LangKey("VirtualShop.Open.Error.InvalidShop", "&cNo such shop!");

    public static final LangKey Virtual_Shop_Product_Purchase_Sell = new LangKey("VirtualShop.Product.Purchase.Sell", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&a&lSuccessful! \n &7You sold &ax%amount% %item% &7for &a%price%&7!");
    public static final LangKey Virtual_Shop_Product_Purchase_Buy  = new LangKey("VirtualShop.Product.Purchase.Buy", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}&a&lSuccessful Purchase! \n &7You bought &ax%amount% %item% &7for &a%price%&7!");

    public static final LangKey Virtual_Shop_Editor_Enter_Id    = new LangKey("VirtualShop.Editor.Enter.Id", "&7Enter shop id...");
    public static final LangKey Virtual_Shop_Editor_Enter_NpcId = new LangKey("VirtualShop.Editor.Enter.NpcId", "&7Enter Citizens id...");
    public static final LangKey Virtual_Shop_Editor_Enter_Title = new LangKey("VirtualShop.Editor.Enter.Title", "&7Enter shop title...");
    public static final LangKey Virtual_Shop_Editor_Enter_Amount           = new LangKey("VirtualShop.Editor.Enter.Amount", "&7Enter new amount...");
    public static final LangKey Virtual_Shop_Editor_Enter_Time_Seconds     = new LangKey("VirtualShop.Editor.Enter.Time.Seconds", "&7Enter &cseconds&7 amount...");
    public static final LangKey Virtual_Shop_Editor_Enter_Day              = new LangKey("VirtualShop.Editor.Enter.Day", "&7Enter day name &cin English&7...");
    public static final LangKey Virtual_Shop_Editor_Enter_Time_Full        = new LangKey("VirtualShop.Editor.Enter.Time.Full", "&7Enter 2 times like &c18:00 19:00&7...");
    public static final LangKey Virtual_Shop_Editor_Create_Error_Exist     = new LangKey("VirtualShop.Editor.Create.Error.Exist", "&cShop with such ID already exist!");
    @Deprecated
    public static final LangKey Virtual_Shop_Editor_Product_Error_Currency = new LangKey("VirtualShop.Editor.Product.Error.Currency", "&cInvalid currency!");


    // CHEST SHOP


    public static final LangKey Command_List_Desc = new LangKey("ChestShop.Command.List.Desc", "List of your shops.");
    public static final LangKey Command_Create_Desc                = new LangKey("ChestShop.Command.Create.Desc", "Creates shop of the chest that you're looking on.");
    public static final LangKey Command_Create_Usage               = new LangKey("ChestShop.Command.Create.Usage", "[type]");
    public static final LangKey Command_Search_Desc                = new LangKey("ChestShop.Command.Search.Desc", "Search for shops with specified item.");
    public static final LangKey Command_Search_Usage               = new LangKey("ChestShop.Command.Search.Usage", "<material>");
    public static final LangKey Command_Remove_Desc                = new LangKey("ChestShop.Command.Remove.Desc", "Removes the shop from the chest that you're looking at.");
    public static final LangKey Shop_Error_NotOwner                = new LangKey("ChestShop.Shop.Error.NotOwner", "&cYou're not the owner of this shop!");
    public static final LangKey Shop_Error_Currency_Invalid        = new LangKey("ChestShop.Shop.Errpr.Currency.Invalid", "&cThis currency is invalid or is not allowed!");
    public static final LangKey Shop_Creation_Info_Done            = new LangKey("ChestShop.Shop.Creation.Info.Done", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10; ~sound: BLOCK_NOTE_BLOCK_BELL;}&a&lShop Created!\n&7Use &aShift-Click &7to enter in &aEdit Mode&7.");
    public static final LangKey Shop_Creation_Error_AlreadyShop    = new LangKey("ChestShop.Shop.Creation.Error.AlreadyShop", "This chest is already shop!");
    public static final LangKey Shop_Creation_Error_NotEmpty       = new LangKey("ChestShop.Shop.Creation.Error.NotEmpty", "Please remove all items from the chest first.");
    public static final LangKey Shop_Creation_Error_NotAChest      = new LangKey("ChestShop.Shop.Creation.Error.NotAChest", "This block is not a chest!");
    public static final LangKey Shop_Creation_Error_BadLocation    = new LangKey("ChestShop.Shop.Creation.Error.BadLocation", "You can't create shop here!");
    public static final LangKey Shop_Creation_Error_LimitReached   = new LangKey("ChestShop.Shop.Creation.Error.LimitReached", "You have reached the limit of shops! You can't create more.");
    public static final LangKey Shop_Creation_Error_BadArea        = new LangKey("ChestShop.Shop.Creation.Error.BadArea", "You can create shops only inside your own claim!");
    public static final LangKey Shop_Creation_Error_NotEnoughFunds = new LangKey("ChestShop.Shop.Creation.Error.NotEnoughFunds", "You don't have enough funds!");
    public static final LangKey Shop_Creation_Error_TypePermission = new LangKey("ChestShop.Shop.Creation.Error.TypePermission", "You don't have permission to create this type shops!");
    public static final LangKey Shop_Removal_Info_Done             = new LangKey("ChestShop.Shop.Removal.Info.Done", "{message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10; ~sound: ENTITY_GENERIC_EXPLODE;}&c&lShop Removed.");
    public static final LangKey Shop_Removal_Error_NotAShop        = new LangKey("ChestShop.Shop.Removal.Error.NotAShop", "This block is not a shop!");
    public static final LangKey Shop_Product_Error_BadItem         = new LangKey("ChestShop.Product.Error.BadItem", "This item can not be traded!");
    @Deprecated
    public static final LangKey ShopList_Info_Switch               = new LangKey("ChestShop.ShopList.Info.Switch", "Global shop list: %state%&7.");
    public static final LangKey Shop_Trade_Buy_Info_User           = new LangKey("ChestShop.Shop.Trade.Buy.Info.User", "{message: ~prefix: false;}&eYou bought &6x%amount% %item% &efor &6%price% &efrom &6%shop_name%&e shop owned by &6%shop_owner%&e.");
    public static final LangKey Shop_Trade_Buy_Info_Owner          = new LangKey("ChestShop.Shop.Trade.Buy.Info.Owner", "{message: ~prefix: false;}&6%player% &ejust bought &6x%amount% %item% &efor &6%price% &efrom your &6%shop_name%&e shop.");
    public static final LangKey Shop_Trade_Sell_Info_User          = new LangKey("ChestShop.Shop.Trade.Sell.Info.User", "{message: ~prefix: false;}&eYou sold &6x%amount% %item% &efor &6%price% &eto &6%shop_name%&e shop owned by &6%shop_owner%&e.");
    public static final LangKey Shop_Trade_Sell_Info_Owner         = new LangKey("ChestShop.Shop.Trade.Sell.Info.Owner", "{message: ~prefix: false;}&6%player% &ejust sold &6x%amount% %item% &efor &6%price% &eto your &6%shop_name%&e shop.");
    public static final LangKey Shop_Bank_Error_InvalidCurrency    = new LangKey("ChestShop.Shop.Bank.Error.InvalidCurrency", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &c&lOperation Failed!
        &7This currency is invalid or is not allowed!
        """);
    public static final LangKey Shop_Bank_Deposit_Success          = new LangKey("ChestShop.Shop.Bank.Deposit.Success", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &a&lSuccessful Deposit!
        &7You deposit &a%amount% &7to shop bank!
        """);
    public static final LangKey Shop_Bank_Deposit_Error_NotEnough  = new LangKey("ChestShop.Shop.Bank.Deposit.Error.NotEnough", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &c&lUnable to Deposit!
        &7You don't have enough funds!
        """);
    public static final LangKey Shop_Bank_Withdraw_Success         = new LangKey("ChestShop.Shop.Bank.Withdraw.Success", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &a&lSuccessful Withdraw!
        &7You withdraw &a%amount% &7from shop bank!
        """);
    public static final LangKey Shop_Bank_Withdraw_Error_NotEnough = new LangKey("ChestShop.Shop.Bank.Withdraw.NotEnough", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 80; ~fadeOut: 10;}
        &c&lUnable to Withdraw!
        &7Bank don't have enough funds!
        """);
    public static final LangKey Editor_Tip_Name                    = new LangKey("ChestShop.Editor.Enter.Name", "&7Enter shop name...");
    public static final LangKey Editor_Tip_Product_Currency        = new LangKey("ChestShop.Editor.Tip.Product.Currency", """
        &7
        &b&lSelect a Currency &7(Click)&b&l:
        &2▸ {json: ~showText: &7Click to select; ~runCommand: %currency_id% ;}&a%currency_name% &7(&f%currency_id%&7){end-json}
        &7
        """);
    public static final LangKey Editor_Tip_Bank_Exchange           = new LangKey("ChestShop.Editor.Tip.Bank.Exchange", "&7Enter currency and amount...");
    public static final LangKey Editor_Tip_Bank_Currency           = new LangKey("ChestShop.Editor.Tip.Bank.Currency", """
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
    public static final LangKey Editor_Error_Bank_InvalidSyntax    = new LangKey("ChestShop.Editor.Bank.InvalidSyntax", "&7Wrong syntax! Use &c<currency> <amount>");
    public static final LangKey Editor_Error_Negative              = new LangKey("ChestShop.Editor.Error.Negative", "&7Number must be positive!");
    public static final LangKey Editor_Error_ProductLeft           = new LangKey("ChestShop.Editor.Error.ProductLeft", "&cFirst you have to take all of this product from the chest!");
}
