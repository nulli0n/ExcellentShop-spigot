package su.nightexpress.nexshop.auction.config;

import org.bukkit.Sound;
import su.nightexpress.economybridge.Placeholders;
import su.nightexpress.nexshop.auction.SortType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;
import su.nightexpress.nightcore.language.entry.LangUIButton;
import su.nightexpress.nightcore.util.bridge.wrapper.HoverEventType;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.OUTPUT;
import static su.nightexpress.nightcore.language.tag.MessageTags.SOUND;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

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
        LIGHT_RED.wrap("You must hold an item to do that!"));


    public static final LangText LISTING_ADD_SUCCESS_INFO = LangText.of("Auction.Listing.Add.Success.Info",
        TAG_NO_PREFIX + SOUND.wrap(Sound.BLOCK_NOTE_BLOCK_BELL),
        " ",
        LIGHT_GREEN.wrap(BOLD.wrap("✔ Success!")),
        " ",
        LIGHT_GRAY.wrap("You added " + LIGHT_GREEN.wrap("x" + LISTING_ITEM_AMOUNT + " " + LISTING_ITEM_NAME) + " on auction for " + LIGHT_GREEN.wrap(LISTING_PRICE) + "!"),
        LIGHT_GRAY.wrap("Tax amount: " + LIGHT_RED.wrap(GENERIC_TAX)),
        " "
    );

    public static final LangText LISTING_ADD_SUCCESS_ANNOUNCE = LangText.of("Auction.Listing.Add.Success.Broadcast",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.wrap(BOLD.wrap("Auction:")),
        LIGHT_GRAY.wrap("Player " + LIGHT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + " added " + LIGHT_YELLOW.wrap("x" + LISTING_ITEM_AMOUNT) + " " +
            HOVER.wrap(LIGHT_YELLOW.wrap(LISTING_ITEM_NAME), HoverEventType.SHOW_ITEM, LISTING_ITEM_VALUE) +
            " on the auction for " + LIGHT_YELLOW.wrap(LISTING_PRICE) + "."),
        " "
    );

    public static final LangText LISTING_ADD_ERROR_BAD_ITEM = LangText.of("Auction.Listing.Add.Error.BadItem",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘ " + GENERIC_ITEM) + " can not be added on the auction!")
    );

    public static final LangText LISTING_ADD_ERROR_LIMIT = LangText.of("Auction.Listing.Add.Error.Limit",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " You can't have more than " + LIGHT_RED.wrap(GENERIC_AMOUNT) + " active items on the auction!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_TAX = LangText.of("Auction.Listing.Add.Error.Price.Tax",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " You can't afford the " + LIGHT_RED.wrap(GENERIC_TAX) + " price tax: " + LIGHT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_CURRENCY_MIN = LangText.of("Auction.Listing.Add.Error.Price.Currency.Min",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " Minimal " + LIGHT_RED.wrap(Placeholders.CURRENCY_NAME) + " price is " + LIGHT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_CURRENCY_MAX = LangText.of("Auction.Listing.Add.Error.Price.Currency.Max",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " Maximal " + LIGHT_RED.wrap(Placeholders.CURRENCY_NAME) + " price is " + LIGHT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_INVALID_PRICE = LangText.of("Auction.Listing.Add.Error.Price.Negative",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " Invalid price!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_MATERIAL_MIN = LangText.of("Auction.Listing.Add.Error.Price.Material.Min",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " Minimal " + LIGHT_RED.wrap("x1 " + GENERIC_ITEM) + " price is " + LIGHT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_PRICE_MATERIAL_MAX = LangText.of("Auction.Listing.Add.Error.Price.Material.Max",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " Maximal " + LIGHT_RED.wrap("x1 " + GENERIC_ITEM) + " price is " + LIGHT_RED.wrap(GENERIC_AMOUNT) + "!")
    );

    public static final LangText LISTING_ADD_ERROR_DISABLED_GAMEMODE = LangText.of("Auction.Listing.Add.Error.DisabledGamemode",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " You can't add items in your current gamemode!")
    );

    public static final LangText LISTING_BUY_SUCCESS_INFO = LangText.of("Auction.Listing.Buy.Success.Info",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.ENTITY_PLAYER_LEVELUP),
        LIGHT_GREEN.wrap(BOLD.wrap("Successful Purchase!")),
        LIGHT_GRAY.wrap("You bought " + LIGHT_GREEN.wrap("x" + LISTING_ITEM_AMOUNT + " " + LISTING_ITEM_NAME) + " from " + LIGHT_GREEN.wrap(LISTING_SELLER) + " for " + LIGHT_GREEN.wrap(LISTING_PRICE) + "!")
    );

    public static final LangText LISTING_BUY_ERROR_NOT_ENOUGH_FUNDS = LangText.of("Auction.Listing.Buy.Error.NotEnoughFunds",
        OUTPUT.wrap(20, 60) + SOUND.wrap(Sound.BLOCK_ANVIL_PLACE),
        LIGHT_RED.wrap(BOLD.wrap("Not Enough Funds!")),
        LIGHT_GRAY.wrap("You need " + LIGHT_RED.wrap(LISTING_PRICE) + ". You have " + LIGHT_RED.wrap(GENERIC_BALANCE) + ".")
    );

    public static final LangText LISTING_CLAIM_SUCCESS = LangText.of("Auction.Notify.Listing.Claim",
        LIGHT_GRAY.wrap(LIGHT_GREEN.wrap("✔") + " You claimed " + LIGHT_GREEN.wrap(LISTING_PRICE) + " for " + LIGHT_GREEN.wrap(LISTING_ITEM_NAME) + "!")
    );


    public static final LangUIButton UI_BUY_CONFIRM = LangUIButton.builder("Auction.UI.BuyConfirm.Listing", LISTING_ITEM_NAME)
        .description(
            LISTING_ITEM_LORE,
            EMPTY_IF_ABOVE,
            LIGHT_GRAY.wrap("Price: ") + LIGHT_YELLOW.wrap(LISTING_PRICE),
            LIGHT_GRAY.wrap("Seller: ") + WHITE.wrap(LISTING_SELLER)
        )
        .formatted(false)
        .build();


    public static final LangText ERROR_DISABLED_WORLD = LangText.of("Auction.Error.DisabledWorld",
        LIGHT_GRAY.wrap(LIGHT_RED.wrap("✘") + " Auction is disabled in this world!")
    );

    public static final LangText NOTIFY_UNCLAIMED_LISTINGS = LangText.of("Auction.Notify.Listing.Unclaimed",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.wrap(BOLD.wrap("Auction:")),
        LIGHT_GRAY.wrap("You have " + LIGHT_YELLOW.wrap(GENERIC_AMOUNT) + " unclaimed listing's incomes!"),
        "",
        LIGHT_GRAY.wrap("Click " + CLICK.wrapRunCommand(
            HOVER.wrapShowText(LIGHT_YELLOW.wrap(BOLD.wrap("HERE")), LIGHT_GRAY.wrap("Click to claim your incomes!")), "/ah unclaimed"
        ) + " to claim now!"),
        " "
    );

    public static final LangText NOTIFY_EXPIRED_LISTINGS = LangText.of("Auction.Notify.Listing.Expired",
        TAG_NO_PREFIX,
        " ",
        LIGHT_ORANGE.wrap(BOLD.wrap("Auction:")),
        LIGHT_GRAY.wrap("You have " + LIGHT_ORANGE.wrap(GENERIC_AMOUNT) + " expired listings!"),
        "",
        LIGHT_GRAY.wrap("Click " + CLICK.wrapRunCommand(
            HOVER.wrapShowText(LIGHT_ORANGE.wrap(BOLD.wrap("HERE")), LIGHT_GRAY.wrap("Click to return your items.")), "/ah expired"
        ) + " to return them."),
        " "
    );
}
