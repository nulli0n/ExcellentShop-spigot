package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.Sound;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.OUTPUT;
import static su.nightexpress.nightcore.language.tag.MessageTags.SOUND;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ChestLang extends CoreLang {

    public static final LangString COMMAND_ARGUMENT_NAME_BUY_PRICE  = LangString.of("ChestShop.Command.Argument.Name.BuyPrice", "buyPrice");
    public static final LangString COMMAND_ARGUMENT_NAME_SELL_PRICE = LangString.of("ChestShop.Command.Argument.Name.SellPrice", "sellPrice");
    public static final LangString COMMAND_ARGUMENT_NAME_SHOP_BLOCK = LangString.of("ChestShop.Command.Argument.Name.ShopBlock", "blockType");

    public static final LangString COMMAND_BROWSE_DESC    = LangString.of("ChestShop.Command.Browse.Desc", "Browse shop owners.");
    public static final LangString COMMAND_LIST_DESC      = LangString.of("ChestShop.Command.List.Desc", "Browse your shops.");
    public static final LangString COMMAND_LIST_ALL_DESC  = LangString.of("ChestShop.Command.List.Desc", "Browse all player shops.");
    public static final LangString COMMAND_BANK_DESC      = LangString.of("ChestShop.Command.Bank.Desc", "Open [player's] bank.");
    public static final LangString COMMAND_CREATE_DESC    = LangString.of("ChestShop.Command.Create.Desc", "Create a shop.");
    public static final LangString COMMAND_GIVE_ITEM_DESC = LangString.of("ChestShop.Command.GiveItem.Desc", "Give shop creation item.");
    public static final LangString COMMAND_REMOVE_DESC    = LangString.of("ChestShop.Command.Remove.Desc", "Removes the shop from the chest that you're looking at.");
    public static final LangString COMMAND_OPEN_INV_DESC  = LangString.of("ChestShop.Command.OpenInv.Desc", "Open shop's inventory.");

    public static final LangText COMMAND_GIVE_ITEM_DONE = LangText.of("ChestShop.Command.GiveItem.Done",
        LIGHT_GRAY.wrap("Given " + LIGHT_YELLOW.wrap(GENERIC_NAME) + " to " + LIGHT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + "."));

    public static final LangText SHOP_ERROR_NOT_OWNER = LangText.of("ChestShop.Shop.Error.NotOwner",
        LIGHT_RED.wrap("You don't own this shop!"));

    public static final LangText SHOP_CREATION_INFO_DONE = LangText.of("ChestShop.Shop.Creation.Info.Done",
        OUTPUT.wrap(10, 80) + SOUND.wrap(Sound.BLOCK_NOTE_BLOCK_BELL),
        LIGHT_GREEN.wrap(BOLD.wrap("Shop Created!")),
        LIGHT_GRAY.wrap(LIGHT_GREEN.wrap("Right-Click") + " while sneaking to open settings.")
    );

    public static final LangText SHOP_CREATION_ERROR_ALREADY_SHOP = LangText.of("ChestShop.Shop.Creation.Error.AlreadyShop",
        LIGHT_RED.wrap("This chest is already shop!"));

    public static final LangText SHOP_CREATION_ERROR_NOT_EMPTY = LangText.of("ChestShop.Shop.Creation.Error.NotEmpty",
        LIGHT_RED.wrap("Please remove all items from the chest first."));

    public static final LangText SHOP_CREATION_ERROR_BAD_BLOCK = LangText.of("ChestShop.Shop.Creation.Error.NotAChest",
        LIGHT_RED.wrap("This block can not be used for shops!"));

    public static final LangText SHOP_CREATION_ERROR_BAD_LOCATION = LangText.of("ChestShop.Shop.Creation.Error.BadLocation",
        LIGHT_RED.wrap("You can't create shop here!"));

    public static final LangText SHOP_CREATION_ERROR_LIMIT_REACHED = LangText.of("ChestShop.Shop.Creation.Error.LimitReached",
        LIGHT_RED.wrap("You have reached the limit of shops! You can't create more."));

    public static final LangText SHOP_CREATION_ERROR_BAD_AREA = LangText.of("ChestShop.Shop.Creation.Error.BadArea",
        LIGHT_RED.wrap("You can create shops only inside your own claim!"));

    public static final LangText SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS = LangText.of("ChestShop.Shop.Creation.Error.NotEnoughFunds",
        LIGHT_RED.wrap("You don't have enough funds!"));

    public static final LangText SHOP_REMOVAL_ERROR_NOT_EMPTY = LangText.of("ChestShop.Shop.Removal.Error.NotEmpty",
        LIGHT_RED.wrap("Please withdraw items from shop storage to remove it."));

    public static final LangText SHOP_REMOVAL_INFO_DONE = LangText.of("ChestShop.Shop.Removal.Info.Done",
        OUTPUT.wrap(10, 80) + SOUND.wrap(Sound.ENTITY_GENERIC_EXPLODE),
        LIGHT_RED.wrap(BOLD.wrap("Shop Removed!"))
    );

    public static final LangText SHOP_RENAME_ERROR_LONG_NAME = LangText.of("ChestShop.Shop.Rename.Error.TooLong",
        LIGHT_RED.wrap("Name is too long! Max. length is " + GENERIC_AMOUNT));

    public static final LangText SHOP_TELEPORT_ERROR_UNSAFE = LangText.of("ChestShop.Shop.Teleport.Error.Unsafe",
        LIGHT_RED.wrap("Teleport cancelled due to unsafe shop location.")
    );

    public static final LangText ERROR_BLOCK_IS_NOT_SHOP = LangText.of("ChestShop.Shop.Removal.Error.NotAShop",
        LIGHT_RED.wrap("This block is not a shop!"));

    public static final LangText SHOP_PRODUCT_ERROR_BAD_ITEM = LangText.of("ChestShop.Product.Error.BadItem",
        LIGHT_RED.wrap("This item can not be used in shop!"));

    public static final LangText EDITOR_ERROR_PRODUCT_LEFT = LangText.of("ChestShop.Editor.Error.ProductLeft",
        LIGHT_RED.wrap("You must take all of this product from the chest!"));

    public static final LangText SHOP_TRADE_BUY_INFO_USER = LangText.of("ChestShop.Shop.Trade.Buy.Info.User",
        TAG_NO_PREFIX,
        LIGHT_YELLOW.wrap("You bought " + ORANGE.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + ORANGE.wrap(GENERIC_PRICE) + " from " + ORANGE.wrap(SHOP_NAME) + " shop.")
    );

    public static final LangText SHOP_TRADE_BUY_INFO_OWNER = LangText.of("ChestShop.Shop.Trade.Buy.Info.Owner",
        TAG_NO_PREFIX,
        LIGHT_YELLOW.wrap(ORANGE.wrap(PLAYER_DISPLAY_NAME) + " bought " + ORANGE.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + ORANGE.wrap(GENERIC_PRICE) + " from your " + ORANGE.wrap(SHOP_NAME) + " shop.")
    );

    public static final LangText SHOP_TRADE_SELL_INFO_USER = LangText.of("ChestShop.Shop.Trade.Sell.Info.User",
        TAG_NO_PREFIX,
        LIGHT_YELLOW.wrap("You sold " + ORANGE.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + ORANGE.wrap(GENERIC_PRICE) + " to " + ORANGE.wrap(SHOP_NAME) + " shop.")
    );

    public static final LangText SHOP_TRADE_SELL_INFO_OWNER = LangText.of("ChestShop.Shop.Trade.Sell.Info.Owner",
        TAG_NO_PREFIX,
        LIGHT_YELLOW.wrap(ORANGE.wrap(PLAYER_DISPLAY_NAME) + " sold " + ORANGE.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + ORANGE.wrap(GENERIC_PRICE) + " to your " + ORANGE.wrap(SHOP_NAME) + " shop.")
    );

    public static final LangText NOTIFICATION_SHOP_EARNINGS = LangText.of("ChestShop.Notification.ShopEarnings",
        TAG_NO_PREFIX,
        " ",
        LIGHT_GREEN.wrap(BOLD.wrap("Shops Income:")),
        LIGHT_GRAY.wrap("While you were offline your shops earned: " + LIGHT_GREEN.wrap(GENERIC_AMOUNT)),
        " "
    );

    public static final LangText SEARCH_PROMPT_ITEM_NAME = LangText.of("ChestShop.Search.ItemPrompt",
        OUTPUT.wrap(20, -1) + SOUND.wrap(Sound.BLOCK_LAVA_POP),
        LIGHT_YELLOW.wrap(BOLD.wrap("Shop Search")),
        GRAY.wrap("Enter " + LIGHT_YELLOW.wrap("item name") + " to search.")
    );

    public static final LangText SEARCH_PROMPT_PLAYER_NAME = LangText.of("ChestShop.Search.PlayerPrompt",
        OUTPUT.wrap(20, -1) + SOUND.wrap(Sound.BLOCK_LAVA_POP),
        LIGHT_YELLOW.wrap(BOLD.wrap("Shop Search")),
        GRAY.wrap("Enter " + LIGHT_YELLOW.wrap("player name") + " to search.")
    );

    public static final LangString SHOP_PRICE_MENU_TITLE = LangString.of("ChestShop.Editor.PriceMenu", BLACK.wrap("Price Settings"));


    public static final LangText ERROR_SHOP_INACTIVE = LangText.of("ChestShop.Shop.Error.Inactive", "This shop is not available currently.");

    public static final LangText RENT_ERROR_ALREADY_RENTED = LangText.of("ChestShop.Rent.Error.AlreadyRented",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Already Rented!")),
        LIGHT_GRAY.wrap("This shop is already rented by someone.")
    );

    public static final LangText RENT_ERROR_NOT_RENTED = LangText.of("ChestShop.Rent.Error.NotRented",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Not Rented!")),
        LIGHT_GRAY.wrap("You don't rent this shop.")
    );

    public static final LangText RENT_ERROR_NOT_RENTABLE = LangText.of("ChestShop.Rent.Error.NotRentable",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Not Rentable!")),
        LIGHT_GRAY.wrap("This shop is not available for rent.")
    );

    public static final LangText RENT_ERROR_INSUFFICIENT_FUNDS = LangText.of("ChestShop.Rent.Error.InsufficientFunds",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Insufficient Funds!")),
        LIGHT_GRAY.wrap("You need " + LIGHT_RED.wrap(GENERIC_PRICE) + " to rent this shop.")
    );

    public static final LangString RENT_PROMPT_DURATION = LangString.of("ChestShop.Rent.Prompt.Duration",
        GRAY.wrap("Enter " + GREEN.wrap("[Days Amount]")));

//    public static final LangString RENT_PROMPT_CURRENCY = LangString.of("ChestShop.Rent.Prompt.Currency",
//        GRAY.wrap("Enter " + GREEN.wrap("[Currency]")));

    public static final LangString RENT_PROMPT_PRICE = LangString.of("ChestShop.Rent.Prompt.Price",
        GRAY.wrap("Enter " + GREEN.wrap("[Rent Price]")));

    public static final LangText RENT_RENT_SUCCESS = LangText.of("ChestShop.Rent.Rent.Success",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.BLOCK_IRON_DOOR_OPEN),
        LIGHT_GREEN.wrap(BOLD.wrap("Shop Rented!")),
        LIGHT_GRAY.wrap("You rented this shop for " + LIGHT_GREEN.wrap(GENERIC_TIME) + ".")
    );

    public static final LangText RENT_EXTEND_SUCCESS = LangText.of("ChestShop.Rent.Extend.Success",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.BLOCK_IRON_DOOR_OPEN),
        LIGHT_GREEN.wrap(BOLD.wrap("Rent Extended!")),
        LIGHT_GRAY.wrap("You extended shop rent for " + LIGHT_GREEN.wrap(GENERIC_TIME) + ".")
    );

    public static final LangText RENT_CANCEL_BY_RENTER = LangText.of("ChestShop.Rent.Cancel.ByRenter",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR),
        LIGHT_GREEN.wrap(BOLD.wrap("Rent Cancelled!")),
        LIGHT_GRAY.wrap("You're no longer renting the " + LIGHT_GREEN.wrap(SHOP_NAME) + " shop.")
    );

    public static final LangText RENT_CANCEL_BY_OWNER = LangText.of("ChestShop.Rent.Cancel.ByOwner",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR),
        LIGHT_GREEN.wrap(BOLD.wrap("Rent Cancelled!")),
        LIGHT_GRAY.wrap(LIGHT_GREEN.wrap(CHEST_SHOP_RENTER_NAME) + " is no longer renting the " + LIGHT_GREEN.wrap(SHOP_NAME) + " shop.")
    );

    public static final LangText BANK_ERROR_INVALID_CURRENCY = LangText.of("ChestShop.Shop.Bank.Error.InvalidCurrency",
        OUTPUT.wrap(20, 60),
        LIGHT_RED.wrap(BOLD.wrap("Operation Failed!")),
        LIGHT_GRAY.wrap("Incorrect currency!")
    );

    public static final LangText BANK_DEPOSIT_SUCCESS = LangText.of("ChestShop.Shop.Bank.Deposit.Success",
        //OUTPUT.wrap(20, 60),
        //LIGHT_GREEN.wrap(BOLD.wrap("Successful Deposit!")),
        LIGHT_GRAY.wrap("You deposit " + LIGHT_GREEN.wrap(GENERIC_AMOUNT) + " to shop bank!")
    );

    public static final LangText BANK_DEPOSIT_ERROR_NOT_ENOUGH = LangText.of("ChestShop.Shop.Bank.Deposit.Error.NotEnough",
        //OUTPUT.wrap(20, 60),
        //LIGHT_RED.wrap(BOLD.wrap("Unable to Deposit!")),
        LIGHT_GRAY.wrap("You don't have enough funds!")
    );

    public static final LangText BANK_WITHDRAW_SUCCESS = LangText.of("ChestShop.Shop.Bank.Withdraw.Success",
        //OUTPUT.wrap(20, 60),
        //LIGHT_GREEN.wrap(BOLD.wrap("Successful Withdraw!")),
        LIGHT_GRAY.wrap("You withdraw " + LIGHT_GREEN.wrap(GENERIC_AMOUNT) + " from shop bank!")
    );

    public static final LangText BANK_WITHDRAW_ERROR_NOT_ENOUGH = LangText.of("ChestShop.Shop.Bank.Withdraw.NotEnough",
        //OUTPUT.wrap(20, 60),
        //LIGHT_RED.wrap(BOLD.wrap("Unable to Withdraw!")),
        LIGHT_GRAY.wrap("There is not enough funds in bank!")
    );

    public static final LangText STORAGE_DEPOSIT_SUCCESS = LangText.of("ChestShop.Shop.InfiniteStorage.Deposit.Success",
        //OUTPUT.wrap(20, 60),
        //LIGHT_GREEN.wrap(BOLD.wrap("Successful Deposit!")),
        LIGHT_GRAY.wrap("You deposit " + LIGHT_GREEN.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " to shop storage!")
    );

    public static final LangText STORAGE_DEPOSIT_ERROR_NOT_ENOUGH = LangText.of("ChestShop.Shop.InfiniteStorage.Deposit.Error.NotEnough",
        //OUTPUT.wrap(20, 60),
        //LIGHT_RED.wrap(BOLD.wrap("Unable to Deposit!")),
        LIGHT_GRAY.wrap("You don't have enough items!")
    );

    public static final LangText STORAGE_WITHDRAW_SUCCESS = LangText.of("ChestShop.Shop.InfiniteStorage.Withdraw.Success",
        //OUTPUT.wrap(20, 60),
        //LIGHT_GREEN.wrap(BOLD.wrap("Successful Withdraw!")),
        LIGHT_GRAY.wrap("You withdraw " + LIGHT_GREEN.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " from shop storage!")
    );

    public static final LangText STORAGE_WITHDRAW_ERROR_NOT_ENOUGH = LangText.of("ChestShop.Shop.InfiniteStorage.Withdraw.NotEnough",
        //OUTPUT.wrap(20, 60),
        //LIGHT_RED.wrap(BOLD.wrap("Unable to Withdraw!")),
        LIGHT_GRAY.wrap("There is not enough items in storage!")
    );

    public static final LangText ERROR_COMMAND_INVALID_SHOP_BLOCK_ARGUMENT = LangText.of("Error.Command.Argument.InvalidShopBlock",
        LIGHT_GRAY.wrap(LIGHT_RED.wrap(GENERIC_VALUE) + " is not a valid shop block!"));
}
