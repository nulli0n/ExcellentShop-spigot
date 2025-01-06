package su.nightexpress.nexshop.auction.config;

import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Sound;
import su.nightexpress.economybridge.Placeholders;
import su.nightexpress.nexshop.auction.SortType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.*;

public class AuctionLang extends Lang {

    public static final LangEnum<SortType> SORT_TYPE = LangEnum.of("Auction.SortType", SortType.class);

    public static final LangString COMMAND_ARGUMENT_NAME_PRICE = LangString.of("Auction.Command.Argument.Name.Price", "price");

    public static final LangString COMMAND_OPEN_DESC      = LangString.of("Auction.Command.Open.Desc", "Open auction.");
    public static final LangString COMMAND_SELL_DESC      = LangString.of("Auction.Command.Sell.Desc", "Add item on auction.");
    public static final LangString COMMAND_EXPIRED_DESC   = LangString.of("Auction.Command.Expired.Desc", "List of expired listings.");
    public static final LangString COMMAND_HISTORY_DESC   = LangString.of("Auction.Command.History.Desc", "Your sales history.");
    public static final LangString COMMAND_SELLING_DESC   = LangString.of("Auction.Command.Selling.Desc", "List of your current listings.");
    public static final LangString COMMAND_UNCLAIMED_DESC = LangString.of("Auction.Command.Unclaimed.Desc", "List of unclaimed rewards for your listings.");

    public static final LangText COMMAND_SELL_ERROR_NO_ITEM = LangText.of("Auction.Command.Sell.Error.NoItem",
        LIGHT_RED.enclose("You must hold an item to do that!"));


    public static final LangText LISTING_ADD_SUCCESS_INFO = LangText.of("Auction.Listing.Add.Success.Info",
        TAG_NO_PREFIX + SOUND.enclose(Sound.BLOCK_NOTE_BLOCK_BELL),
        " ",
        LIGHT_GREEN.enclose(BOLD.enclose("✔ Success!")),
        " ",
        LIGHT_GRAY.enclose("You added " + LIGHT_GREEN.enclose("x" + LISTING_ITEM_AMOUNT + " " + LISTING_ITEM_NAME) + " on auction for " + LIGHT_GREEN.enclose(LISTING_PRICE) + "!"),
        LIGHT_GRAY.enclose("Tax amount: " + LIGHT_RED.enclose(GENERIC_TAX)),
        " "
    );

    public static final LangText LISTING_ADD_SUCCESS_ANNOUNCE = LangText.of("Auction.Listing.Add.Success.Broadcast",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Auction:")),
        LIGHT_GRAY.enclose("Player " + LIGHT_YELLOW.enclose(PLAYER_DISPLAY_NAME) + " added " + LIGHT_YELLOW.enclose("x" + LISTING_ITEM_AMOUNT) + " " +
            HOVER.enclose(LIGHT_YELLOW.enclose(LISTING_ITEM_NAME), HoverEvent.Action.SHOW_ITEM, LISTING_ITEM_VALUE) +
            " on the auction for " + LIGHT_YELLOW.enclose(LISTING_PRICE) + "."),
        " "
    );

    public static final LangText LISTING_ADD_ERROR_BAD_ITEM = LangText.of("Auction.Listing.Add.Error.BadItem",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘ " + GENERIC_ITEM) + " can not be added on the auction!")
    );

    public static final LangText LISTING_ADD_ERROR_LIMIT = LangText.of("Auction.Listing.Add.Error.Limit",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " You can't have more than " + LIGHT_RED.enclose(GENERIC_AMOUNT) + " active items on the auction!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_TAX = LangText.of("Auction.Listing.Add.Error.Price.Tax",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " You can't afford the " + LIGHT_RED.enclose(GENERIC_TAX) + " price tax: " + LIGHT_RED.enclose(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_CURRENCY_MIN = LangText.of("Auction.Listing.Add.Error.Price.Currency.Min",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " Minimal " + LIGHT_RED.enclose(Placeholders.CURRENCY_NAME) + " price is " + LIGHT_RED.enclose(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_CURRENCY_MAX = LangText.of("Auction.Listing.Add.Error.Price.Currency.Max",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " Maximal " + LIGHT_RED.enclose(Placeholders.CURRENCY_NAME) + " price is " + LIGHT_RED.enclose(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_INVALID_PRICE = LangText.of("Auction.Listing.Add.Error.Price.Negative",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " Invalid price!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_MATERIAL_MIN = LangText.of("Auction.Listing.Add.Error.Price.Material.Min",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " Minimal " + LIGHT_RED.enclose("x1 " + GENERIC_ITEM) + " price is " + LIGHT_RED.enclose(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_MATERIAL_MAX = LangText.of("Auction.Listing.Add.Error.Price.Material.Max",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " Maximal " + LIGHT_RED.enclose("x1 " + GENERIC_ITEM) + " price is " + LIGHT_RED.enclose(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_DISABLED_GAMEMODE = LangText.of("Auction.Listing.Add.Error.DisabledGamemode",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " You can't add items in your current gamemode!")
    );

    public static final LangText LISTING_BUY_SUCCESS_INFO = LangText.of("Auction.Listing.Buy.Success.Info",
        OUTPUT.enclose(20, 60) + SOUND.enclose(Sound.ENTITY_PLAYER_LEVELUP),
        LIGHT_GREEN.enclose(BOLD.enclose("Successful Purchase!")),
        LIGHT_GRAY.enclose("You bought " + LIGHT_GREEN.enclose("x" + LISTING_ITEM_AMOUNT + " " + LISTING_ITEM_NAME) + " from " + LIGHT_GREEN.enclose(LISTING_SELLER) + " for " + LIGHT_GREEN.enclose(LISTING_PRICE) + "!")
    );

    public static final LangText LISTING_BUY_ERROR_NOT_ENOUGH_FUNDS = LangText.of("Auction.Listing.Buy.Error.NotEnoughFunds",
        OUTPUT.enclose(20, 60) + SOUND.enclose(Sound.BLOCK_ANVIL_PLACE),
        LIGHT_RED.enclose(BOLD.enclose("Not Enough Funds!")),
        LIGHT_GRAY.enclose("You need " + LIGHT_RED.enclose(LISTING_PRICE) + ". You have " + LIGHT_RED.enclose(GENERIC_BALANCE) + ".")
    );

    public static final LangText LISTING_CLAIM_SUCCESS = LangText.of("Auction.Notify.Listing.Claim",
        LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("✔") + " You claimed " + LIGHT_GREEN.enclose(LISTING_PRICE) + " for " + LIGHT_GREEN.enclose(LISTING_ITEM_NAME) + "!")
    );

    public static final LangText ERROR_DISABLED_WORLD = LangText.of("Auction.Error.DisabledWorld",
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " Auction is disabled in this world!")
    );

    public static final LangText NOTIFY_UNCLAIMED_LISTINGS = LangText.of("Auction.Notify.Listing.Unclaimed",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Auction:")),
        LIGHT_GRAY.enclose("You have " + LIGHT_YELLOW.enclose(GENERIC_AMOUNT) + " unclaimed listing's incomes!"),
        "",
        LIGHT_GRAY.enclose("Click " + CLICK.encloseRun(
            HOVER.encloseHint(LIGHT_YELLOW.enclose(BOLD.enclose("HERE")), LIGHT_GRAY.enclose("Click to claim your incomes!")), "/ah unclaimed"
        ) + " to claim now!"),
        " "
    );

    public static final LangText NOTIFY_EXPIRED_LISTINGS = LangText.of("Auction.Notify.Listing.Expired",
        TAG_NO_PREFIX,
        " ",
        LIGHT_ORANGE.enclose(BOLD.enclose("Auction:")),
        LIGHT_GRAY.enclose("You have " + LIGHT_ORANGE.enclose(GENERIC_AMOUNT) + " expired listings!"),
        "",
        LIGHT_GRAY.enclose("Click " + CLICK.encloseRun(
            HOVER.encloseHint(LIGHT_ORANGE.enclose(BOLD.enclose("HERE")), LIGHT_GRAY.enclose("Click to return your items.")), "/ah expired"
        ) + " to return them."),
        " "
    );
}
