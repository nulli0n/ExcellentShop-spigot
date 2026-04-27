package su.nightexpress.excellentshop.feature.playershop.core;

import org.bukkit.Sound;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.locale.message.MessageData;

import static su.nightexpress.excellentshop.ShopPlaceholders.*;
import static su.nightexpress.nightcore.util.Placeholders.GENERIC_INPUT;
import static su.nightexpress.nightcore.util.Placeholders.PLAYER_DISPLAY_NAME;
import static su.nightexpress.nightcore.util.Placeholders.PLAYER_NAME;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ChestLang implements LangContainer {

    private static final String PREFIX = "ChestShop";

    public static LangEntry.@NonNull Builder builder(@NonNull String path) {
        return LangEntry.builder(PREFIX + "." + path);
    }

    public static IconLocale.@NonNull Builder iconBuilder(@NonNull String path) {
        return LangEntry.iconBuilder(PREFIX + "." + path);
    }

    public static final TextLocale COMMAND_ARGUMENT_NAME_BUY_PRICE  = LangEntry.builder(
        "ChestShop.Command.Argument.Name.BuyPrice").text("buyPrice");
    public static final TextLocale COMMAND_ARGUMENT_NAME_SELL_PRICE = LangEntry.builder(
        "ChestShop.Command.Argument.Name.SellPrice").text("sellPrice");
    public static final TextLocale COMMAND_ARGUMENT_NAME_SHOP_BLOCK = LangEntry.builder(
        "ChestShop.Command.Argument.Name.ShopBlock").text("blockType");
    public static final TextLocale COMMAND_ARGUMENT_NAME_ITEM_NAME  = LangEntry.builder(
        "ChestShop.Command.Argument.Name.ItemName").text("itemName");

    public static final TextLocale COMMAND_BROWSE_DESC        = LangEntry.builder("ChestShop.Command.Browse.Desc").text(
        "Browse shop owners.");
    public static final TextLocale COMMAND_LIST_DESC          = LangEntry.builder("ChestShop.Command.List.Desc").text(
        "Browse your shops.");
    public static final TextLocale COMMAND_LIST_ALL_DESC      = LangEntry.builder("ChestShop.Command.List.Desc").text(
        "Browse all player shops.");
    public static final TextLocale COMMAND_SEARCH_DESC        = LangEntry.builder("ChestShop.Command.Search.Desc").text(
        "Search shops by item.");
    public static final TextLocale COMMAND_PLAYER_SEARCH_DESC = LangEntry.builder("ChestShop.Command.PlayerSearch.Desc")
        .text("Search shops by player.");
    public static final TextLocale COMMAND_BANK_DESC          = LangEntry.builder("ChestShop.Command.Bank.Desc").text(
        "Open [player's] bank.");
    public static final TextLocale COMMAND_CREATE_DESC        = LangEntry.builder("ChestShop.Command.Create.Desc").text(
        "Create a shop.");
    public static final TextLocale COMMAND_GIVE_ITEM_DESC     = LangEntry.builder("ChestShop.Command.GiveItem.Desc")
        .text("Give shop creation item.");
    public static final TextLocale COMMAND_REMOVE_DESC        = LangEntry.builder("ChestShop.Command.Remove.Desc").text(
        "Removes the shop from the chest that you're looking at.");
    public static final TextLocale COMMAND_OPEN_INV_DESC      = LangEntry.builder("ChestShop.Command.OpenInv.Desc")
        .text("Open shop's inventory.");

    public static final MessageLocale COMMAND_GIVE_ITEM_DONE = LangEntry.builder("ChestShop.Command.GiveItem.Done")
        .chatMessage(
            GRAY.wrap("Given " + SOFT_YELLOW.wrap(GENERIC_NAME) + " to " + SOFT_YELLOW.wrap(PLAYER_DISPLAY_NAME) +
                "."));

    public static final MessageLocale SHOP_ERROR_NOT_OWNER = LangEntry.builder("ChestShop.Shop.Error.NotOwner")
        .chatMessage(
            SOFT_RED.wrap("You don't own this shop!"));

    public static final MessageLocale SHOP_CREATION_INFO_DONE = LangEntry.builder("ChestShop.Shop.Creation.Info.Done")
        .titleMessage(
            SOFT_GREEN.wrap(BOLD.wrap("Shop Created!")),
            GRAY.wrap(SOFT_GREEN.wrap("Right-Click") + " while sneaking to open settings."),
            Sound.BLOCK_NOTE_BLOCK_BELL
        );

    public static final MessageLocale SHOP_CREATION_ERROR_ALREADY_SHOP = LangEntry.builder(
        "ChestShop.Shop.Creation.Error.AlreadyShop").chatMessage(
            SOFT_RED.wrap("This chest is already shop!"));

    public static final MessageLocale SHOP_CREATION_ERROR_NOT_EMPTY = LangEntry.builder(
        "ChestShop.Shop.Creation.Error.NotEmpty").chatMessage(
            SOFT_RED.wrap("Please remove all items from the chest first."));

    public static final MessageLocale SHOP_CREATION_ERROR_BAD_BLOCK = LangEntry.builder(
        "ChestShop.Shop.Creation.Error.NotAChest").chatMessage(
            SOFT_RED.wrap("This block can not be used for shops!"));

    public static final MessageLocale SHOP_CREATION_ERROR_BAD_LOCATION = LangEntry.builder(
        "ChestShop.Shop.Creation.Error.BadLocation").chatMessage(
            SOFT_RED.wrap("You can't create shop here!"));

    public static final MessageLocale SHOP_CREATION_ERROR_LIMIT_REACHED = LangEntry.builder(
        "ChestShop.Shop.Creation.Error.LimitReached").chatMessage(
            SOFT_RED.wrap("You have reached the limit of shops! You can't create more."));

    public static final MessageLocale SHOP_CREATION_ERROR_BAD_AREA = LangEntry.builder(
        "ChestShop.Shop.Creation.Error.BadArea").chatMessage(
            SOFT_RED.wrap("You can create shops only inside your own claim!"));

    public static final MessageLocale SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS = LangEntry.builder(
        "ChestShop.Shop.Creation.Error.NotEnoughFunds").chatMessage(
            SOFT_RED.wrap("You don't have enough funds!"));

    public static final MessageLocale SHOP_REMOVAL_ERROR_NOT_EMPTY = LangEntry.builder(
        "ChestShop.Shop.Removal.Error.NotEmpty").chatMessage(
            SOFT_RED.wrap("Please withdraw items from shop storage to remove it."));

    public static final MessageLocale SHOP_REMOVAL_INFO_DONE = LangEntry.builder("ChestShop.Shop.Removal.Info.Done")
        .titleMessage(
            SOFT_RED.wrap(BOLD.wrap("Shop Removed!")), "",
            Sound.ENTITY_GENERIC_EXPLODE
        );

    @Deprecated
    public static final MessageLocale SHOP_RENAME_ERROR_LONG_NAME = LangEntry.builder(
        "ChestShop.Shop.Rename.Error.TooLong").chatMessage(
            SOFT_RED.wrap("Name is too long! Max. length is " + GENERIC_AMOUNT));

    public static final MessageLocale SHOP_TELEPORT_ERROR_UNSAFE = LangEntry.builder(
        "ChestShop.Shop.Teleport.Error.Unsafe").chatMessage(
            SOFT_RED.wrap("Teleport cancelled due to unsafe shop location.")
        );

    public static final MessageLocale SHOP_ADD_TRUSTED_DONE = builder("Shop.AddTrusted.Done")
        .chatMessage("You have successfully added " + GREEN.wrap(PLAYER_NAME) + " as a trusted player to " + WHITE.wrap(
            SHOP_NAME) + " shop.");

    public static final MessageLocale SHOP_ADD_TRUSTED_ALREADY_ADDED = builder("Shop.AddTrusted.AlreadyAdded")
        .chatMessage("Player " + RED.wrap(PLAYER_NAME) + " is already a trusted player of " + WHITE.wrap(
            SHOP_NAME) + " shop.");

    public static final MessageLocale SHOP_ADD_TRUSTED_OWNER = builder("Shop.AddTrusted.Owner")
        .chatMessage("Shop owner " + RED.wrap(PLAYER_NAME) + " can not be trusted.");


    public static final MessageLocale SHOP_REMOVE_TRUSTED_DONE = builder("Shop.RemoveTrusted.Done")
        .chatMessage("You have successfully removed " + GREEN.wrap(PLAYER_NAME) + " from trusted players of " + WHITE
            .wrap(SHOP_NAME) + " shop.");

    public static final MessageLocale SHOP_REMOVE_TRUSTED_NOT_ADDED = builder("Shop.RemoveTrusted.NotAdded")
        .chatMessage("Player " + RED.wrap(PLAYER_NAME) + " is not a trusted player of " + WHITE.wrap(
            SHOP_NAME) + " shop.");

    public static final MessageLocale ERROR_BLOCK_IS_NOT_SHOP = LangEntry.builder(
        "ChestShop.Shop.Removal.Error.NotAShop").chatMessage(
            SOFT_RED.wrap("This block is not a shop!"));

    public static final MessageLocale SHOP_PRODUCT_ERROR_BAD_ITEM = LangEntry.builder("ChestShop.Product.Error.BadItem")
        .chatMessage(
            SOFT_RED.wrap("This item can not be used in shop!"));

    public static final MessageLocale EDITOR_ERROR_PRODUCT_LEFT = LangEntry.builder(
        "ChestShop.Editor.Error.ProductLeft").chatMessage(
            SOFT_RED.wrap("You must take all of this product from the chest!"));


    public static final MessageLocale PRODUCT_PURCHASE_BUY_SINGLE = LangEntry.builder(
        "ChestShop.Product.Purchase.BuySingle").chatMessage(
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            GRAY.wrap("You bought " + WHITE.wrap(GENERIC_PRODUCTS) + " for " + GREEN.wrap(GENERIC_WORTH))
        );

    public static final MessageLocale PRODUCT_PURCHASE_BUY_MULTIPLE = LangEntry.builder(
        "ChestShop.Product.Purchase.BuyMultiple").chatMessage(
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            GRAY.wrap("You bought " + SHOW_TEXT.with(GENERIC_PRODUCTS).wrap(GREEN.and(UNDERLINED).wrap(GENERIC_SIZE +
                " items")) + " " + GRAY.wrap("(hover for info)") + " for " + GREEN.wrap(GENERIC_WORTH) + ".")
        );

    public static final MessageLocale PRODUCT_PURCHASE_SELL_SINGLE = LangEntry.builder(
        "ChestShop.Product.Purchase.SellSingle").chatMessage(
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            GRAY.wrap("You sold " + WHITE.wrap(GENERIC_PRODUCTS) + " for " + GREEN.wrap(GENERIC_WORTH))
        );

    public static final MessageLocale PRODUCT_PURCHASE_SELL_MULTIPLE = LangEntry.builder(
        "ChestShop.Product.Purchase.SellMultiple").chatMessage(
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            GRAY.wrap("You sold " + SHOW_TEXT.with(GENERIC_PRODUCTS).wrap(GREEN.and(UNDERLINED).wrap(GENERIC_SIZE +
                " items")) + " " + GRAY.wrap("(hover for info)") + " for " + GREEN.wrap(GENERIC_WORTH) + ".")
        );


    public static final MessageLocale PURCHASE_NOTIFY_BUY_SINGLE = LangEntry.builder(
        "ChestShop.Purchase.Notify.BuySingle").chatMessage(
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            GRAY.wrap(ORANGE.wrap(PLAYER_DISPLAY_NAME) + " bought " + WHITE.wrap(GENERIC_PRODUCTS) + " for " + GREEN
                .wrap(GENERIC_WORTH) + " from your " + WHITE.wrap(SHOP_NAME) + ".")
        );

    public static final MessageLocale PURCHASE_NOTIFY_SELL_SINGLE = LangEntry.builder(
        "ChestShop.Purchase.Notify.SellSingle").chatMessage(
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            GRAY.wrap(ORANGE.wrap(PLAYER_DISPLAY_NAME) + " sold " + WHITE.wrap(GENERIC_PRODUCTS) + " for " + GREEN.wrap(
                GENERIC_WORTH) + " to your " + WHITE.wrap(SHOP_NAME) + ".")
        );


    public static final MessageLocale NOTIFICATION_SHOP_EARNINGS = LangEntry.builder(
        "ChestShop.Notification.ShopEarnings").message(
            MessageData.CHAT_NO_PREFIX,
            " ",
            SOFT_GREEN.wrap(BOLD.wrap("Shops Income:")),
            GRAY.wrap("While you were offline your shops earned: " + SOFT_GREEN.wrap(GENERIC_AMOUNT)),
            " "
        );

    public static final MessageLocale SEARCH_PROMPT_ITEM_NAME = LangEntry.builder("ChestShop.Search.ItemPrompt")
        .titleMessage(
            SOFT_YELLOW.wrap(BOLD.wrap("Shop Search")),
            GRAY.wrap("Enter " + SOFT_YELLOW.wrap("item name") + " to search."),
            20, -1, Sound.BLOCK_LAVA_POP
        );

    public static final MessageLocale SEARCH_PROMPT_PLAYER_NAME = LangEntry.builder("ChestShop.Search.PlayerPrompt")
        .titleMessage(
            SOFT_YELLOW.wrap(BOLD.wrap("Shop Search")),
            GRAY.wrap("Enter " + SOFT_YELLOW.wrap("player name") + " to search."),
            20, -1, Sound.BLOCK_LAVA_POP
        );

    public static final TextLocale SHOP_PRICE_MENU_TITLE = LangEntry.builder("ChestShop.Editor.PriceMenu").text(BLACK
        .wrap("Price Settings"));


    public static final MessageLocale ERROR_SHOP_INACTIVE = LangEntry.builder("ChestShop.Shop.Error.Inactive")
        .chatMessage("This shop is not available currently.");

    public static final MessageLocale RENT_ERROR_ALREADY_RENTED = LangEntry.builder(
        "ChestShop.Rent.Error.AlreadyRented").titleMessage(
            SOFT_RED.wrap(BOLD.wrap("Already Rented!")),
            GRAY.wrap("This shop is already rented by someone."),
            Sound.ENTITY_VILLAGER_NO
        );

    public static final MessageLocale RENT_ERROR_NOT_RENTED = LangEntry.builder("ChestShop.Rent.Error.NotRented")
        .titleMessage(
            SOFT_RED.wrap(BOLD.wrap("Not Rented!")),
            GRAY.wrap("You don't rent this shop."),
            Sound.ENTITY_VILLAGER_NO
        );

    public static final MessageLocale RENT_ERROR_NOT_RENTABLE = LangEntry.builder("ChestShop.Rent.Error.NotRentable")
        .titleMessage(
            SOFT_RED.wrap(BOLD.wrap("Not Rentable!")),
            GRAY.wrap("This shop is not available for rent."),
            Sound.ENTITY_VILLAGER_NO
        );

    public static final MessageLocale RENT_ERROR_INSUFFICIENT_FUNDS = LangEntry.builder(
        "ChestShop.Rent.Error.InsufficientFunds").titleMessage(
            SOFT_RED.wrap(BOLD.wrap("Insufficient Funds!")),
            GRAY.wrap("You need " + SOFT_RED.wrap(GENERIC_PRICE) + " to rent this shop."),
            Sound.ENTITY_VILLAGER_NO
        );

    public static final TextLocale RENT_PROMPT_DURATION = LangEntry.builder("ChestShop.Rent.Prompt.Duration").text(GRAY
        .wrap("Enter " + GREEN.wrap("[Days Amount]")));

    //    public static final TextLocale RENT_PROMPT_CURRENCY = LangEntry.builder("ChestShop.Rent.Prompt.Currency",
    //        GRAY.wrap("Enter " + GREEN.wrap("[Currency]")));

    public static final TextLocale RENT_PROMPT_PRICE = LangEntry.builder("ChestShop.Rent.Prompt.Price").text(GRAY.wrap(
        "Enter " + GREEN.wrap("[Rent Price]")));

    public static final MessageLocale RENT_RENT_SUCCESS = LangEntry.builder("ChestShop.Rent.Rent.Success").titleMessage(
        SOFT_GREEN.wrap(BOLD.wrap("Shop Rented!")),
        GRAY.wrap("You rented this shop for " + SOFT_GREEN.wrap(GENERIC_TIME) + "."),
        Sound.BLOCK_IRON_DOOR_OPEN
    );

    public static final MessageLocale RENT_EXTEND_SUCCESS = LangEntry.builder("ChestShop.Rent.Extend.Success")
        .titleMessage(
            SOFT_GREEN.wrap(BOLD.wrap("Rent Extended!")),
            GRAY.wrap("You extended shop rent for " + SOFT_GREEN.wrap(GENERIC_TIME) + "."),
            Sound.BLOCK_IRON_DOOR_OPEN
        );

    public static final MessageLocale RENT_CANCEL_BY_RENTER = LangEntry.builder("ChestShop.Rent.Cancel.ByRenter")
        .titleMessage(
            SOFT_GREEN.wrap(BOLD.wrap("Rent Cancelled!")),
            GRAY.wrap("You're no longer renting the " + SOFT_GREEN.wrap(SHOP_NAME) + " shop."),
            Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR
        );

    public static final MessageLocale RENT_CANCEL_BY_OWNER = LangEntry.builder("ChestShop.Rent.Cancel.ByOwner")
        .titleMessage(
            SOFT_GREEN.wrap(BOLD.wrap("Rent Cancelled!")),
            GRAY.wrap(SOFT_GREEN.wrap(CHEST_SHOP_RENTER_NAME) + " is no longer renting the " + SOFT_GREEN.wrap(
                SHOP_NAME) + " shop."),
            Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR
        );

    public static final MessageLocale BANK_ERROR_INVALID_CURRENCY = LangEntry.builder(
        "ChestShop.Shop.Bank.Error.InvalidCurrency").titleMessage(
            SOFT_RED.wrap(BOLD.wrap("Operation Failed!")),
            GRAY.wrap("Incorrect currency!")
        );

    public static final MessageLocale BANK_ERROR_NOT_EXIST = LangEntry.builder("ChestShop.Shop.Bank.Error.NotExist")
        .chatMessage(
            GRAY.wrap("Bank does not exist.")
        );

    public static final MessageLocale BANK_DEPOSIT_SUCCESS = LangEntry.builder("ChestShop.Shop.Bank.Deposit.Success")
        .chatMessage(
            GRAY.wrap("You deposit " + SOFT_GREEN.wrap(GENERIC_AMOUNT) + " to shop bank!")
        );

    public static final MessageLocale BANK_DEPOSIT_ERROR_NOT_ENOUGH = LangEntry.builder(
        "ChestShop.Shop.Bank.Deposit.Error.NotEnough").chatMessage(
            GRAY.wrap("You don't have enough funds!")
        );

    public static final MessageLocale BANK_WITHDRAW_SUCCESS = LangEntry.builder("ChestShop.Shop.Bank.Withdraw.Success")
        .chatMessage(
            GRAY.wrap("You withdraw " + SOFT_GREEN.wrap(GENERIC_AMOUNT) + " from shop bank!")
        );

    public static final MessageLocale BANK_WITHDRAW_ERROR_NOT_ENOUGH = LangEntry.builder(
        "ChestShop.Shop.Bank.Withdraw.NotEnough").chatMessage(
            GRAY.wrap("There is not enough funds in bank!")
        );

    public static final MessageLocale STORAGE_DEPOSIT_SUCCESS = LangEntry.builder(
        "ChestShop.Shop.InfiniteStorage.Deposit.Success").chatMessage(
            GRAY.wrap("You deposit " + SOFT_GREEN.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " to shop storage!")
        );

    public static final MessageLocale STORAGE_DEPOSIT_ERROR_NOT_ENOUGH = LangEntry.builder(
        "ChestShop.Shop.InfiniteStorage.Deposit.Error.NotEnough").chatMessage(
            GRAY.wrap("You don't have enough items!")
        );

    public static final MessageLocale STORAGE_WITHDRAW_SUCCESS = LangEntry.builder(
        "ChestShop.Shop.InfiniteStorage.Withdraw.Success").chatMessage(
            GRAY.wrap("You withdraw " + SOFT_GREEN.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) +
                " from shop storage!")
        );

    public static final MessageLocale STORAGE_WITHDRAW_ERROR_NOT_ENOUGH = LangEntry.builder(
        "ChestShop.Shop.InfiniteStorage.Withdraw.NotEnough").chatMessage(
            GRAY.wrap("There is not enough items in storage!")
        );

    public static final MessageLocale ERROR_COMMAND_INVALID_SHOP_BLOCK_ARGUMENT = LangEntry.builder(
        "Error.Command.Argument.InvalidShopBlock").chatMessage(
            GRAY.wrap(SOFT_RED.wrap(GENERIC_INPUT) + " is not a valid shop block!"));
}
