package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.Sound;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;
import su.nightexpress.nexshop.Placeholders;

import static su.nexmedia.engine.utils.Colors.*;

public class ChestLang extends EngineLang {

    public static final LangKey COMMAND_LIST_DESC  = LangKey.of("ChestShop.Command.List.Desc", "List of [player's] shops.");
    public static final LangKey COMMAND_LIST_USAGE = LangKey.of("ChestShop.Command.List.Usage", "[player]");

    public static final LangKey COMMAND_BANK_DESC  = LangKey.of("ChestShop.Command.Bank.Desc", "Open [player's] bank.");
    public static final LangKey COMMAND_BANK_USAGE = LangKey.of("ChestShop.Command.Bank.Usage", "[player]");

    public static final LangKey COMMAND_CREATE_DESC  = LangKey.of("ChestShop.Command.Create.Desc", "Creates shop of the chest that you're looking on.");
    public static final LangKey COMMAND_CREATE_USAGE = LangKey.of("ChestShop.Command.Create.Usage", "[type]");

    public static final LangKey COMMAND_BROWSE_DESC  = LangKey.of("ChestShop.Command.Browse.Desc", "Player shops GUI.");
    public static final LangKey COMMAND_BROWSE_USAGE = LangKey.of("ChestShop.Command.Browse.Usage", "");

    public static final LangKey COMMAND_REMOVE_DESC = LangKey.of("ChestShop.Command.Remove.Desc", "Removes the shop from the chest that you're looking at.");

    public static final LangKey COMMAND_OPEN_DESC = LangKey.of("ChestShop.Command.Open.Desc", "Open inventory of the target shop.");

    public static final LangKey SHOP_ERROR_NOT_OWNER = LangKey.of("ChestShop.Shop.Error.NotOwner", RED + "You're not the owner of this shop!");

    public static final LangKey SHOP_CREATION_INFO_DONE = LangKey.of("ChestShop.Shop.Creation.Info.Done",
        "<! type:\"titles:10:80:10\" sound:\"" + Sound.BLOCK_NOTE_BLOCK_BELL.name() + "\" !>" +
            "\n" + GREEN + "&lShop Created!" +
            "\n" + GRAY + "Do " + GREEN + "Right-Click" + GRAY + " while sneaking for " + GREEN + "Settings");

    public static final LangKey SHOP_CREATION_ERROR_ALREADY_SHOP     = LangKey.of("ChestShop.Shop.Creation.Error.AlreadyShop", RED + "This chest is already shop!");
    public static final LangKey SHOP_CREATION_ERROR_NOT_EMPTY        = LangKey.of("ChestShop.Shop.Creation.Error.NotEmpty", RED + "Please remove all items from the chest first.");
    public static final LangKey SHOP_CREATION_ERROR_NOT_A_CHEST      = LangKey.of("ChestShop.Shop.Creation.Error.NotAChest", RED + "This block is not a chest!");
    public static final LangKey SHOP_CREATION_ERROR_BAD_LOCATION     = LangKey.of("ChestShop.Shop.Creation.Error.BadLocation", RED + "You can't create shop here!");
    public static final LangKey SHOP_CREATION_ERROR_LIMIT_REACHED    = LangKey.of("ChestShop.Shop.Creation.Error.LimitReached", RED + "You have reached the limit of shops! You can't create more.");
    public static final LangKey SHOP_CREATION_ERROR_BAD_AREA         = LangKey.of("ChestShop.Shop.Creation.Error.BadArea", RED + "You can create shops only inside your own claim!");
    public static final LangKey SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS = LangKey.of("ChestShop.Shop.Creation.Error.NotEnoughFunds", RED + "You don't have enough funds!");
    public static final LangKey SHOP_CREATION_ERROR_TYPE_PERMISSION  = LangKey.of("ChestShop.Shop.Creation.Error.TypePermission", RED + "You don't have permission to create this type shops!");

    public static final LangKey SHOP_REMOVAL_INFO_DONE = LangKey.of("ChestShop.Shop.Removal.Info.Done",
        "<! type:\"titles:10:60:10\" sound:\"" + Sound.ENTITY_GENERIC_EXPLODE.name() + "\" !>" +
            "\n" + RED + "&lShop Removed!" +
            "\n" + GRAY);

    public static final LangKey SHOP_REMOVAL_ERROR_NOT_A_SHOP = LangKey.of("ChestShop.Shop.Removal.Error.NotAShop", RED + "This block is not a shop!");

    public static final LangKey SHOP_PRODUCT_ERROR_BAD_ITEM = LangKey.of("ChestShop.Product.Error.BadItem", RED + "This item can not be used in shop!");

    public static final LangKey SHOP_TRADE_BUY_INFO_USER = LangKey.of("ChestShop.Shop.Trade.Buy.Info.User",
        "<! prefix:\"false\" !>" +
            LIGHT_YELLOW + "You bought " + ORANGE + "x" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + LIGHT_YELLOW + " for " + ORANGE + Placeholders.GENERIC_PRICE + LIGHT_YELLOW + " from " + ORANGE + Placeholders.SHOP_NAME + LIGHT_YELLOW + " shop.");

    public static final LangKey SHOP_TRADE_BUY_INFO_OWNER = LangKey.of("ChestShop.Shop.Trade.Buy.Info.Owner",
        "<! prefix:\"false\" !>" +
            ORANGE + Placeholders.PLAYER_DISPLAY_NAME + LIGHT_YELLOW + " just bought " + ORANGE + "x" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + LIGHT_YELLOW + " for " + ORANGE + Placeholders.GENERIC_PRICE + LIGHT_YELLOW + " from your " + ORANGE + Placeholders.SHOP_NAME + LIGHT_YELLOW + " shop.");

    public static final LangKey SHOP_TRADE_SELL_INFO_USER = LangKey.of("ChestShop.Shop.Trade.Sell.Info.User",
        "<! prefix:\"false\" !>" +
            LIGHT_YELLOW + "You sold " + ORANGE + "x" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + LIGHT_YELLOW + " for " + ORANGE + Placeholders.GENERIC_PRICE + LIGHT_YELLOW + " to " + ORANGE + Placeholders.SHOP_NAME + LIGHT_YELLOW + " shop.");

    public static final LangKey SHOP_TRADE_SELL_INFO_OWNER = LangKey.of("ChestShop.Shop.Trade.Sell.Info.Owner",
        "<! prefix:\"false\" !>" +
            ORANGE + Placeholders.PLAYER_DISPLAY_NAME + LIGHT_YELLOW + " just sold " + ORANGE + "x" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + LIGHT_YELLOW + " for " + ORANGE + Placeholders.GENERIC_PRICE + LIGHT_YELLOW + " to your " + ORANGE + Placeholders.SHOP_NAME + LIGHT_YELLOW + " shop.");

    public static final LangKey NOTIFICATION_SHOP_EARNINGS = LangKey.of("ChestShop.Notification.ShopEarnings",
        "<! prefix:\"false\" !>" +
            "\n" + LIGHT_YELLOW +
            "\n" + ORANGE + BOLD + "Chest Shop Notification:" +
            "\n" + LIGHT_YELLOW + "Since your last online, your shops earned:" +
            "\n" + ORANGE + "▪ " + Placeholders.GENERIC_AMOUNT +
            "\n" + LIGHT_YELLOW);

    public static final LangKey SEARCH_ENTER_ITEM = LangKey.of("ChestShop.Search.ItemPrompt",
        "<! type:\"titles:20:-1:20\" sound:\"" + Sound.BLOCK_LAVA_POP.name() + "\" !>" +
            "\n" + LIGHT_YELLOW + BOLD + "Shop Search" +
            "\n" + GRAY + "Enter item " + LIGHT_YELLOW + "name" + GRAY + " or " + LIGHT_YELLOW + "material" + GRAY + " to search for.");

    public static final LangKey BANK_ERROR_INVALID_CURRENCY = LangKey.of("ChestShop.Shop.Bank.Error.InvalidCurrency",
        "<! type:\"titles:10:80:10\" !>" +
            "\n" + RED + "&lOperation Failed!" +
            "\n" + GRAY + "This currency is invalid or is not allowed!");

    public static final LangKey BANK_DEPOSIT_SUCCESS = LangKey.of("ChestShop.Shop.Bank.Deposit.Success",
        "<! type:\"titles:10:80:10\" !>" +
            "\n" + GREEN + "&lSuccessful Deposit!" +
            "\n" + GRAY + "You deposit " + GREEN + Placeholders.GENERIC_AMOUNT + GRAY + " to shop bank!");

    public static final LangKey BANK_DEPOSIT_ERROR_NOT_ENOUGH = LangKey.of("ChestShop.Shop.Bank.Deposit.Error.NotEnough",
        "<! type:\"titles:10:80:10\" !>" +
            "\n" + RED + "&lUnable to Deposit!" +
            "\n" + GRAY + "You don't have enough funds!");

    public static final LangKey BANK_WITHDRAW_SUCCESS = LangKey.of("ChestShop.Shop.Bank.Withdraw.Success",
        "<! type:\"titles:10:80:10\" !>" +
            "\n" + GREEN + "&lSuccessful Withdraw!" +
            "\n" + GRAY + "You withdraw " + GREEN + Placeholders.GENERIC_AMOUNT + GRAY + " from shop bank!");

    public static final LangKey BANK_WITHDRAW_ERROR_NOT_ENOUGH = LangKey.of("ChestShop.Shop.Bank.Withdraw.NotEnough",
        "<! type:\"titles:10:80:10\" !>" +
            "\n" + RED + "&lUnable to Withdraw!" +
            "\n" + GRAY + "Bank don't have enough funds!");

    public static final LangKey EDITOR_ERROR_PRODUCT_LEFT = LangKey.of("ChestShop.Editor.Error.ProductLeft", "&cFirst you have to take all of this product from the chest!");
}
