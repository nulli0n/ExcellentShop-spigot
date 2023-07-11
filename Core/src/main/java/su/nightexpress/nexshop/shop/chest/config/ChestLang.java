package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.Sound;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;

public class ChestLang extends EngineLang {

    public static final LangKey COMMAND_LIST_DESC    = LangKey.of("ChestShop.Command.List.Desc", "Browse your own or other player's shops.");
    public static final LangKey COMMAND_LIST_USAGE   = LangKey.of("ChestShop.Command.List.Usage", "[player]");
    public static final LangKey COMMAND_CREATE_DESC  = LangKey.of("ChestShop.Command.Create.Desc", "Creates shop of the chest that you're looking on.");
    public static final LangKey COMMAND_CREATE_USAGE = LangKey.of("ChestShop.Command.Create.Usage", "[type]");
    public static final LangKey COMMAND_SEARCH_DESC  = LangKey.of("ChestShop.Command.Search.Desc", "Search for shops with specified item.");
    public static final LangKey COMMAND_SEARCH_USAGE = LangKey.of("ChestShop.Command.Search.Usage", "<material>");
    public static final LangKey COMMAND_REMOVE_DESC  = LangKey.of("ChestShop.Command.Remove.Desc", "Removes the shop from the chest that you're looking at.");

    public static final LangKey SHOP_ERROR_NOT_OWNER        = LangKey.of("ChestShop.Shop.Error.NotOwner", "&cYou're not the owner of this shop!");
    public static final LangKey SHOP_CREATION_INFO_DONE          = LangKey.of("ChestShop.Shop.Creation.Info.Done", "<! type:\"titles:10:80:10\" sound:\"" + Sound.BLOCK_NOTE_BLOCK_BELL.name() + "\" !>" + "&a&lShop Created!\n&7Use &aShift-Click &7to enter in &aEdit Mode&7.");
    public static final LangKey SHOP_CREATION_ERROR_ALREADY_SHOP = LangKey.of("ChestShop.Shop.Creation.Error.AlreadyShop", "This chest is already shop!");
    public static final LangKey SHOP_CREATION_ERROR_NOT_EMPTY   = LangKey.of("ChestShop.Shop.Creation.Error.NotEmpty", "Please remove all items from the chest first.");
    public static final LangKey SHOP_CREATION_ERROR_NOT_A_CHEST  = LangKey.of("ChestShop.Shop.Creation.Error.NotAChest", "This block is not a chest!");
    public static final LangKey SHOP_CREATION_ERROR_BAD_LOCATION  = LangKey.of("ChestShop.Shop.Creation.Error.BadLocation", "You can't create shop here!");
    public static final LangKey SHOP_CREATION_ERROR_LIMIT_REACHED  = LangKey.of("ChestShop.Shop.Creation.Error.LimitReached", "You have reached the limit of shops! You can't create more.");
    public static final LangKey SHOP_CREATION_ERROR_BAD_AREA         = LangKey.of("ChestShop.Shop.Creation.Error.BadArea", "You can create shops only inside your own claim!");
    public static final LangKey SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS = LangKey.of("ChestShop.Shop.Creation.Error.NotEnoughFunds", "You don't have enough funds!");
    public static final LangKey SHOP_CREATION_ERROR_TYPE_PERMISSION = LangKey.of("ChestShop.Shop.Creation.Error.TypePermission", "You don't have permission to create this type shops!");
    public static final LangKey SHOP_REMOVAL_INFO_DONE        = LangKey.of("ChestShop.Shop.Removal.Info.Done", "<! type:\"titles:10:80:10\" sound:\"" + Sound.ENTITY_GENERIC_EXPLODE.name() + "\" !>" + "&c&lShop Removed.");
    public static final LangKey SHOP_REMOVAL_ERROR_NOT_A_SHOP = LangKey.of("ChestShop.Shop.Removal.Error.NotAShop", "This block is not a shop!");
    public static final LangKey SHOP_PRODUCT_ERROR_BAD_ITEM = LangKey.of("ChestShop.Product.Error.BadItem", "This item can not be traded!");
    public static final LangKey SHOP_TRADE_BUY_INFO_USER  = LangKey.of("ChestShop.Shop.Trade.Buy.Info.User", "<! prefix:\"false\" !>" + "&eYou bought &6x%amount% %item% &efor &6%price% &efrom &6%shop_name%&e shop owned by &6%shop_owner%&e.");
    public static final LangKey SHOP_TRADE_BUY_INFO_OWNER  = LangKey.of("ChestShop.Shop.Trade.Buy.Info.Owner", "<! prefix:\"false\" !>" + "&6%player% &ejust bought &6x%amount% %item% &efor &6%price% &efrom your &6%shop_name%&e shop.");
    public static final LangKey SHOP_TRADE_SELL_INFO_USER       = LangKey.of("ChestShop.Shop.Trade.Sell.Info.User", "<! prefix:\"false\" !>" + "&eYou sold &6x%amount% %item% &efor &6%price% &eto &6%shop_name%&e shop owned by &6%shop_owner%&e.");
    public static final LangKey SHOP_TRADE_SELL_INFO_OWNER       = LangKey.of("ChestShop.Shop.Trade.Sell.Info.Owner", "<! prefix:\"false\" !>" + "&6%player% &ejust sold &6x%amount% %item% &efor &6%price% &eto your &6%shop_name%&e shop.");
    public static final LangKey SHOP_BANK_ERROR_INVALID_CURRENCY  = LangKey.of("ChestShop.Shop.Bank.Error.InvalidCurrency", """
        <! type:"titles:10:80:10" !>
        &c&lOperation Failed!
        &7This currency is invalid or is not allowed!
        """);
    public static final LangKey SHOP_BANK_DEPOSIT_SUCCESS          = LangKey.of("ChestShop.Shop.Bank.Deposit.Success", """
        <! type:"titles:10:80:10" !>
        &a&lSuccessful Deposit!
        &7You deposit &a%amount% &7to shop bank!
        """);
    public static final LangKey SHOP_BANK_DEPOSIT_ERROR_NOT_ENOUGH = LangKey.of("ChestShop.Shop.Bank.Deposit.Error.NotEnough", """
        <! type:"titles:10:80:10" !>
        &c&lUnable to Deposit!
        &7You don't have enough funds!
        """);
    public static final LangKey SHOP_BANK_WITHDRAW_SUCCESS          = LangKey.of("ChestShop.Shop.Bank.Withdraw.Success", """
        <! type:"titles:10:80:10" !>
        &a&lSuccessful Withdraw!
        &7You withdraw &a%amount% &7from shop bank!
        """);
    public static final LangKey SHOP_BANK_WITHDRAW_ERROR_NOT_ENOUGH = LangKey.of("ChestShop.Shop.Bank.Withdraw.NotEnough", """
        <! type:"titles:10:80:10" !>
        &c&lUnable to Withdraw!
        &7Bank don't have enough funds!
        """);
    public static final LangKey EDITOR_ERROR_PRODUCT_LEFT = LangKey.of("ChestShop.Editor.Error.ProductLeft", "&cFirst you have to take all of this product from the chest!");
}
