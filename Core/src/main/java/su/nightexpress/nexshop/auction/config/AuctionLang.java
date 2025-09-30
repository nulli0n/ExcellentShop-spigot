package su.nightexpress.nexshop.auction.config;

import org.bukkit.Sound;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.auction.SortType;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.EnumLocale;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.locale.message.MessageData;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class AuctionLang implements LangContainer {

    public static final EnumLocale<SortType> SORT_TYPE = LangEntry.builder("Auction.SortType").enumeration(SortType.class);

    public static final TextLocale COMMAND_ARGUMENT_NAME_PRICE = LangEntry.builder("Auction.Command.Argument.Name.Price").text("price");

    public static final TextLocale COMMAND_OPEN_DESC      = LangEntry.builder("Auction.Command.Open.Desc").text("Open auction.");
    public static final TextLocale COMMAND_SELL_DESC      = LangEntry.builder("Auction.Command.Sell.Desc").text("Add item on auction.");
    public static final TextLocale COMMAND_EXPIRED_DESC   = LangEntry.builder("Auction.Command.Expired.Desc").text("List of expired listings.");
    public static final TextLocale COMMAND_HISTORY_DESC   = LangEntry.builder("Auction.Command.History.Desc").text("Your sales history.");
    public static final TextLocale COMMAND_SELLING_DESC   = LangEntry.builder("Auction.Command.Selling.Desc").text("List of your current listings.");
    public static final TextLocale COMMAND_UNCLAIMED_DESC = LangEntry.builder("Auction.Command.Unclaimed.Desc").text("List of unclaimed rewards for your listings.");

    public static final MessageLocale COMMAND_SELL_ERROR_NO_ITEM = LangEntry.builder("Auction.Command.Sell.Error.NoItem").chatMessage(
        SOFT_RED.wrap("You must hold an item to do that!"));


    public static final MessageLocale LISTING_ADD_SUCCESS_INFO = LangEntry.builder("Auction.Listing.Add.Success.Info").chatMessage(
        Sound.BLOCK_NOTE_BLOCK_BELL,
        " ",
        SOFT_GREEN.wrap(BOLD.wrap("✔ Success!")),
        " ",
        GRAY.wrap("You added " + SOFT_GREEN.wrap("x" + LISTING_ITEM_AMOUNT + " " + LISTING_ITEM_NAME) + " on auction for " + SOFT_GREEN.wrap(LISTING_PRICE) + "!"),
        GRAY.wrap("Tax amount: " + SOFT_RED.wrap(GENERIC_TAX)),
        " "
    );

    public static final MessageLocale LISTING_ADD_SUCCESS_ANNOUNCE = LangEntry.builder("Auction.Listing.Add.Success.Broadcast").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        SOFT_YELLOW.wrap(BOLD.wrap("Auction:")),
        GRAY.wrap("Player " + SOFT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + " added " + SOFT_YELLOW.wrap("x" + LISTING_ITEM_AMOUNT) + " " +
            SHOW_ITEM.with(LISTING_ITEM_VALUE).wrap(SOFT_YELLOW.wrap(LISTING_ITEM_NAME)) +
            " on the auction for " + SOFT_YELLOW.wrap(LISTING_PRICE) + "."),
        " "
    );

    public static final MessageLocale LISTING_ADD_ERROR_BAD_ITEM = LangEntry.builder("Auction.Listing.Add.Error.BadItem").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘ " + GENERIC_ITEM) + " can not be added on the auction!")
    );

    public static final MessageLocale LISTING_ADD_ERROR_LIMIT = LangEntry.builder("Auction.Listing.Add.Error.Limit").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘") + " You can't have more than " + SOFT_RED.wrap(GENERIC_AMOUNT) + " active items on the auction!")
    );

    public static final MessageLocale LISTING_ADD_ERROR_PRICE_TAX = LangEntry.builder("Auction.Listing.Add.Error.Price.Tax").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘") + " You can't afford the " + SOFT_RED.wrap(GENERIC_TAX) + " price tax: " + SOFT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final MessageLocale LISTING_ADD_ERROR_PRICE_CURRENCY_MIN = LangEntry.builder("Auction.Listing.Add.Error.Price.Currency.Min").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘") + " Minimal " + SOFT_RED.wrap(Placeholders.CURRENCY_NAME) + " price is " + SOFT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final MessageLocale LISTING_ADD_ERROR_PRICE_CURRENCY_MAX = LangEntry.builder("Auction.Listing.Add.Error.Price.Currency.Max").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘") + " Maximal " + SOFT_RED.wrap(Placeholders.CURRENCY_NAME) + " price is " + SOFT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final MessageLocale LISTING_ADD_ERROR_INVALID_PRICE = LangEntry.builder("Auction.Listing.Add.Error.Price.Negative").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘") + " Invalid price!")
    );

    public static final MessageLocale LISTING_ADD_ERROR_PRICE_MATERIAL_MIN = LangEntry.builder("Auction.Listing.Add.Error.Price.Material.Min").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘") + " Minimal " + SOFT_RED.wrap("x1 " + GENERIC_ITEM) + " price is " + SOFT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final MessageLocale LISTING_ADD_ERROR_PRICE_MATERIAL_MAX = LangEntry.builder("Auction.Listing.Add.Error.Price.Material.Max").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘") + " Maximal " + SOFT_RED.wrap("x1 " + GENERIC_ITEM) + " price is " + SOFT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final MessageLocale LISTING_ADD_ERROR_DISABLED_GAMEMODE = LangEntry.builder("Auction.Listing.Add.Error.DisabledGamemode").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap("✘") + " You can't add items in your current gamemode!")
    );

    public static final MessageLocale LISTING_BUY_SUCCESS_INFO = LangEntry.builder("Auction.Listing.Buy.Success.Info").titleMessage(
        SOFT_GREEN.wrap(BOLD.wrap("Successful Purchase!")),
        GRAY.wrap("You bought " + SOFT_GREEN.wrap("x" + LISTING_ITEM_AMOUNT + " " + LISTING_ITEM_NAME) + " from " + SOFT_GREEN.wrap(LISTING_SELLER) + " for " + SOFT_GREEN.wrap(LISTING_PRICE) + "!"),
        Sound.ENTITY_PLAYER_LEVELUP
    );

    public static final MessageLocale LISTING_BUY_ERROR_NOT_ENOUGH_FUNDS = LangEntry.builder("Auction.Listing.Buy.Error.NotEnoughFunds").titleMessage(
        SOFT_RED.wrap(BOLD.wrap("Not Enough Funds!")),
        GRAY.wrap("You need " + SOFT_RED.wrap(LISTING_PRICE) + ". You have " + SOFT_RED.wrap(GENERIC_BALANCE) + "."),
        Sound.BLOCK_ANVIL_PLACE
    );

    public static final MessageLocale LISTING_CLAIM_SUCCESS = LangEntry.builder("Auction.Notify.Listing.Claim").chatMessage(
        GRAY.wrap(SOFT_GREEN.wrap("✔") + " You claimed " + SOFT_GREEN.wrap(LISTING_PRICE) + " for " + SOFT_GREEN.wrap(LISTING_ITEM_NAME) + "!")
    );


    public static final IconLocale UI_BUY_CONFIRM = LangEntry.iconBuilder("Auction.UI.BuyConfirm.Listing")
        .rawName(LISTING_ITEM_NAME)
        .rawLore(
            LISTING_ITEM_LORE,
            EMPTY_IF_ABOVE,
            GRAY.wrap("Price: ") + SOFT_YELLOW.wrap(LISTING_PRICE),
            GRAY.wrap("Seller: ") + WHITE.wrap(LISTING_SELLER)
        )
        .build();


    public static final MessageLocale ERROR_DISABLED_WORLD = LangEntry.builder("Auction.Error.DisabledWorld").chatMessage(
        GRAY.wrap(SOFT_RED.wrap("✘") + " Auction is disabled in this world!")
    );

    public static final MessageLocale NOTIFY_UNCLAIMED_LISTINGS = LangEntry.builder("Auction.Notify.Listing.Unclaimed").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        SOFT_YELLOW.wrap(BOLD.wrap("Auction:")),
        GRAY.wrap("You have " + SOFT_YELLOW.wrap(GENERIC_AMOUNT) + " unclaimed listing's incomes!"),
        "",
        GRAY.wrap("Click " + RUN_COMMAND.with("/ah unclaimed").and(SHOW_TEXT.with(GRAY.wrap("Click to claim your incomes!"))).wrap(SOFT_YELLOW.wrap(BOLD.wrap("HERE"))) + " to claim now!"),
        " "
    );

    public static final MessageLocale NOTIFY_EXPIRED_LISTINGS = LangEntry.builder("Auction.Notify.Listing.Expired").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        SOFT_ORANGE.wrap(BOLD.wrap("Auction:")),
        GRAY.wrap("You have " + SOFT_ORANGE.wrap(GENERIC_AMOUNT) + " expired listings!"),
        "",
        GRAY.wrap("Click " + RUN_COMMAND.with("/ah expired").and(SHOW_TEXT.with(GRAY.wrap("Click to return your items."))).wrap(SOFT_ORANGE.wrap(BOLD.wrap("HERE"))) + " to return them."),
        " "
    );
}
