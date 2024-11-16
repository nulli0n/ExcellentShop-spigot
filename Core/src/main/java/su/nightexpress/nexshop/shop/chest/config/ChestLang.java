package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.Sound;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.*;
import static su.nightexpress.nexshop.Placeholders.*;

public class ChestLang extends CoreLang {

    public static final LangEnum<ShopType> SHOP_TYPES = LangEnum.of("ChestShop.ShopType", ShopType.class);

    public static final LangString COMMAND_ARGUMENT_NAME_BUY_PRICE  = LangString.of("ChestShop.Command.Argument.Name.BuyPrice", "buyPrice");
    public static final LangString COMMAND_ARGUMENT_NAME_SELL_PRICE = LangString.of("ChestShop.Command.Argument.Name.SellPrice", "sellPrice");

    public static final LangString COMMAND_LIST_DESC = LangString.of("ChestShop.Command.List.Desc", "List of [player's] shops.");

    public static final LangString COMMAND_BANK_DESC = LangString.of("ChestShop.Command.Bank.Desc", "Open [player's] bank.");

    public static final LangString COMMAND_CREATE_DESC = LangString.of("ChestShop.Command.Create.Desc", "Create a shop.");

    public static final LangString COMMAND_BROWSE_DESC = LangString.of("ChestShop.Command.Browse.Desc", "Player shops GUI.");

    public static final LangString COMMAND_GIVE_ITEM_DESC = LangString.of("ChestShop.Command.GiveItem.Desc", "Give shop creation item.");

    public static final LangText COMMAND_GIVE_ITEM_BAD_MATERIAL = LangText.of("ChestShop.Command.GiveItem.BadMaterial",
        LIGHT_RED.enclose("Invalid shop type!"));

    public static final LangText COMMAND_GIVE_ITEM_DONE = LangText.of("ChestShop.Command.GiveItem.Done",
        LIGHT_GRAY.enclose("Given " + LIGHT_YELLOW.enclose(GENERIC_NAME) + " to " + LIGHT_YELLOW.enclose(PLAYER_DISPLAY_NAME) + "."));

    public static final LangString COMMAND_REMOVE_DESC = LangString.of("ChestShop.Command.Remove.Desc", "Removes the shop from the chest that you're looking at.");

    public static final LangString COMMAND_OPEN_DESC = LangString.of("ChestShop.Command.Open.Desc", "Open inventory of the target shop.");

    public static final LangText SHOP_ERROR_NOT_OWNER = LangText.of("ChestShop.Shop.Error.NotOwner",
        LIGHT_RED.enclose("You don't own this shop!"));

    public static final LangText SHOP_CREATION_INFO_DONE = LangText.of("ChestShop.Shop.Creation.Info.Done",
        OUTPUT.enclose(10, 80) + SOUND.enclose(Sound.BLOCK_NOTE_BLOCK_BELL),
        LIGHT_GREEN.enclose(BOLD.enclose("Shop Created!")),
        LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("Right-Click") + " while sneaking to open settings.")
    );

    public static final LangText SHOP_CREATION_ERROR_ALREADY_SHOP = LangText.of("ChestShop.Shop.Creation.Error.AlreadyShop",
        LIGHT_RED.enclose("This chest is already shop!"));

    public static final LangText SHOP_CREATION_ERROR_NOT_EMPTY = LangText.of("ChestShop.Shop.Creation.Error.NotEmpty",
        LIGHT_RED.enclose("Please remove all items from the chest first."));

    public static final LangText SHOP_CREATION_ERROR_NOT_A_CHEST = LangText.of("ChestShop.Shop.Creation.Error.NotAChest",
        LIGHT_RED.enclose("This block is not a chest!"));

    public static final LangText SHOP_CREATION_ERROR_BAD_LOCATION = LangText.of("ChestShop.Shop.Creation.Error.BadLocation",
        LIGHT_RED.enclose("You can't create shop here!"));

    public static final LangText SHOP_CREATION_ERROR_LIMIT_REACHED = LangText.of("ChestShop.Shop.Creation.Error.LimitReached",
        LIGHT_RED.enclose("You have reached the limit of shops! You can't create more."));

    public static final LangText SHOP_CREATION_ERROR_BAD_AREA = LangText.of("ChestShop.Shop.Creation.Error.BadArea",
        LIGHT_RED.enclose("You can create shops only inside your own claim!"));

    public static final LangText SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS = LangText.of("ChestShop.Shop.Creation.Error.NotEnoughFunds",
        LIGHT_RED.enclose("You don't have enough funds!"));

    public static final LangText SHOP_CREATION_ERROR_TYPE_PERMISSION = LangText.of("ChestShop.Shop.Creation.Error.TypePermission",
        LIGHT_RED.enclose("You don't have permission to create this type shops!"));

    public static final LangText SHOP_REMOVAL_ERROR_NOT_EMPTY = LangText.of("ChestShop.Shop.Removal.Error.NotEmpty",
        LIGHT_RED.enclose("Please withdraw items from shop storage to remove it."));

    public static final LangText SHOP_REMOVAL_INFO_DONE = LangText.of("ChestShop.Shop.Removal.Info.Done",
        OUTPUT.enclose(10, 80) + SOUND.enclose(Sound.ENTITY_GENERIC_EXPLODE),
        LIGHT_RED.enclose(BOLD.enclose("Shop Removed!"))
    );

    public static final LangText SHOP_TELEPORT_ERROR_UNSAFE = LangText.of("ChestShop.Shop.Teleport.Error.Unsafe",
        LIGHT_RED.enclose("Teleport cancelled due to unsafe shop location.")
    );

    public static final LangText ERROR_BLOCK_IS_NOT_SHOP = LangText.of("ChestShop.Shop.Removal.Error.NotAShop",
        LIGHT_RED.enclose("This block is not a shop!"));

    public static final LangText SHOP_PRODUCT_ERROR_BAD_ITEM = LangText.of("ChestShop.Product.Error.BadItem",
        LIGHT_RED.enclose("This item can not be used in shop!"));

    public static final LangText EDITOR_ERROR_PRODUCT_LEFT = LangText.of("ChestShop.Editor.Error.ProductLeft",
        LIGHT_RED.enclose("You must take all of this product from the chest!"));

    public static final LangText SHOP_TRADE_BUY_INFO_USER = LangText.of("ChestShop.Shop.Trade.Buy.Info.User",
        TAG_NO_PREFIX,
        LIGHT_YELLOW.enclose("You bought " + ORANGE.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + ORANGE.enclose(GENERIC_PRICE) + " from " + ORANGE.enclose(SHOP_NAME) + " shop.")
    );

    public static final LangText SHOP_TRADE_BUY_INFO_OWNER = LangText.of("ChestShop.Shop.Trade.Buy.Info.Owner",
        TAG_NO_PREFIX,
        LIGHT_YELLOW.enclose(ORANGE.enclose(PLAYER_DISPLAY_NAME) + " bought " + ORANGE.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + ORANGE.enclose(GENERIC_PRICE) + " from your " + ORANGE.enclose(SHOP_NAME) + " shop.")
    );

    public static final LangText SHOP_TRADE_SELL_INFO_USER = LangText.of("ChestShop.Shop.Trade.Sell.Info.User",
        TAG_NO_PREFIX,
        LIGHT_YELLOW.enclose("You sold " + ORANGE.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + ORANGE.enclose(GENERIC_PRICE) + " to " + ORANGE.enclose(SHOP_NAME) + " shop.")
    );

    public static final LangText SHOP_TRADE_SELL_INFO_OWNER = LangText.of("ChestShop.Shop.Trade.Sell.Info.Owner",
        TAG_NO_PREFIX,
        LIGHT_YELLOW.enclose(ORANGE.enclose(PLAYER_DISPLAY_NAME) + " sold " + ORANGE.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + ORANGE.enclose(GENERIC_PRICE) + " to your " + ORANGE.enclose(SHOP_NAME) + " shop.")
    );

    public static final LangText NOTIFICATION_SHOP_EARNINGS = LangText.of("ChestShop.Notification.ShopEarnings",
        TAG_NO_PREFIX,
        " ",
        LIGHT_GREEN.enclose(BOLD.enclose("Shops Income:")),
        LIGHT_GRAY.enclose("While you were offline your shops earned: " + LIGHT_GREEN.enclose(GENERIC_AMOUNT)),
        " "
    );

    public static final LangText SEARCH_ENTER_ITEM = LangText.of("ChestShop.Search.ItemPrompt",
        OUTPUT.enclose(20, -1) + SOUND.enclose(Sound.BLOCK_LAVA_POP),
        LIGHT_YELLOW.enclose(BOLD.enclose("Shop Search")),
        LIGHT_GRAY.enclose("Enter item " + LIGHT_YELLOW.enclose("name") + " to search for.")
    );

    public static final LangText BANK_ERROR_INVALID_CURRENCY = LangText.of("ChestShop.Shop.Bank.Error.InvalidCurrency",
        OUTPUT.enclose(20, 60),
        LIGHT_RED.enclose(BOLD.enclose("Operation Failed!")),
        LIGHT_GRAY.enclose("Incorrect currency!")
    );

    public static final LangText BANK_DEPOSIT_SUCCESS = LangText.of("ChestShop.Shop.Bank.Deposit.Success",
        //OUTPUT.enclose(20, 60),
        //LIGHT_GREEN.enclose(BOLD.enclose("Successful Deposit!")),
        LIGHT_GRAY.enclose("You deposit " + LIGHT_GREEN.enclose(GENERIC_AMOUNT) + " to shop bank!")
    );

    public static final LangText BANK_DEPOSIT_ERROR_NOT_ENOUGH = LangText.of("ChestShop.Shop.Bank.Deposit.Error.NotEnough",
        //OUTPUT.enclose(20, 60),
        //LIGHT_RED.enclose(BOLD.enclose("Unable to Deposit!")),
        LIGHT_GRAY.enclose("You don't have enough funds!")
    );

    public static final LangText BANK_WITHDRAW_SUCCESS = LangText.of("ChestShop.Shop.Bank.Withdraw.Success",
        //OUTPUT.enclose(20, 60),
        //LIGHT_GREEN.enclose(BOLD.enclose("Successful Withdraw!")),
        LIGHT_GRAY.enclose("You withdraw " + LIGHT_GREEN.enclose(GENERIC_AMOUNT) + " from shop bank!")
    );

    public static final LangText BANK_WITHDRAW_ERROR_NOT_ENOUGH = LangText.of("ChestShop.Shop.Bank.Withdraw.NotEnough",
        //OUTPUT.enclose(20, 60),
        //LIGHT_RED.enclose(BOLD.enclose("Unable to Withdraw!")),
        LIGHT_GRAY.enclose("There is not enough funds in bank!")
    );

    public static final LangText STORAGE_DEPOSIT_SUCCESS = LangText.of("ChestShop.Shop.InfiniteStorage.Deposit.Success",
        //OUTPUT.enclose(20, 60),
        //LIGHT_GREEN.enclose(BOLD.enclose("Successful Deposit!")),
        LIGHT_GRAY.enclose("You deposit " + LIGHT_GREEN.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " to shop storage!")
    );

    public static final LangText STORAGE_DEPOSIT_ERROR_NOT_ENOUGH = LangText.of("ChestShop.Shop.InfiniteStorage.Deposit.Error.NotEnough",
        //OUTPUT.enclose(20, 60),
        //LIGHT_RED.enclose(BOLD.enclose("Unable to Deposit!")),
        LIGHT_GRAY.enclose("You don't have enough items!")
    );

    public static final LangText STORAGE_WITHDRAW_SUCCESS = LangText.of("ChestShop.Shop.InfiniteStorage.Withdraw.Success",
        //OUTPUT.enclose(20, 60),
        //LIGHT_GREEN.enclose(BOLD.enclose("Successful Withdraw!")),
        LIGHT_GRAY.enclose("You withdraw " + LIGHT_GREEN.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " from shop storage!")
    );

    public static final LangText STORAGE_WITHDRAW_ERROR_NOT_ENOUGH = LangText.of("ChestShop.Shop.InfiniteStorage.Withdraw.NotEnough",
        //OUTPUT.enclose(20, 60),
        //LIGHT_RED.enclose(BOLD.enclose("Unable to Withdraw!")),
        LIGHT_GRAY.enclose("There is not enough items in storage!")
    );
}
