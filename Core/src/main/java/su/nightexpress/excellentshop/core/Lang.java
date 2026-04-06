package su.nightexpress.excellentshop.core;

import org.bukkit.Sound;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.click.ProductClickAction;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.excellentshop.product.ContentType;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.EnumLocale;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;

import java.time.DayOfWeek;

import static su.nightexpress.excellentshop.ShopPlaceholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public final class Lang implements LangContainer {

    public static final EnumLocale<DayOfWeek>          DAYS                 = LangEntry.builder("Days").enumeration(DayOfWeek.class);
    public static final EnumLocale<TradeType>          TRADE_TYPES          = LangEntry.builder("TradeType").enumeration(TradeType.class);
    public static final EnumLocale<PriceType>          PRICE_TYPES          = LangEntry.builder("PriceType").enumeration(PriceType.class);
    public static final EnumLocale<ContentType>        CONTENT_TYPE         = LangEntry.builder("ProductType").enumeration(ContentType.class);
    public static final EnumLocale<ProductClickAction> PRODUCT_CLICK_ACTION = LangEntry.builder("ProductClickAction").enumeration(ProductClickAction.class);

    public static final MessageLocale SHOP_TRADE_PRODUCT_UNBUYABLE = LangEntry.builder("Shop.Product.Error.Unbuyable").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap(PRODUCT_PREVIEW_NAME) + " cannot be purchased."));

    public static final MessageLocale SHOP_TRADE_PRODUCT_UNSELLABLE = LangEntry.builder("Shop.Product.Error.Unsellable").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap(PRODUCT_PREVIEW_NAME) + " cannot be sold."));

    public static final MessageLocale SHOP_TRADE_PRODUCT_OUT_OF_STOCK = LangEntry.builder("Shop.Product.Error.OutOfStock").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap(PRODUCT_PREVIEW_NAME) + " is out of stock."));

    public static final MessageLocale SHOP_TRADE_PLAYER_OUT_OF_LIMIT = LangEntry.builder("Shop.Product.Error.OutOfLimit").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("You have reached the limit for " + SOFT_RED.wrap(PRODUCT_PREVIEW_NAME) + "."));

    public static final MessageLocale SHOP_TRADE_PRODUCT_OUT_OF_SPACE = LangEntry.builder("Shop.Product.Error.OutOfSpace").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(SOFT_RED.wrap(PRODUCT_PREVIEW_NAME) + " is at full capacity."));

    public static final MessageLocale SHOP_TRADE_SHOP_OUT_OF_FUNDS = LangEntry.builder("Shop.Product.Error.OutOfFunds").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("The shop is out of money!"));

    public static final MessageLocale SHOP_TRADE_PLAYER_FULL_INVENTORY = LangEntry.builder("Shop.Product.Error.FullInventory").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("Clean up your inventory to purchase " + SOFT_RED.wrap(GENERIC_PRODUCTS) + "."));

    public static final MessageLocale SHOP_TRADE_PLAYER_OUT_OF_MONEY = LangEntry.builder("Shop.Trade.Product.TooExpensive").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("You don't have " + SOFT_RED.wrap(GENERIC_PRICE) + " to purchase " + WHITE.wrap(GENERIC_PRODUCTS) + ".")
    );

    public static final MessageLocale SHOP_TRADE_PLAYER_NOT_ENOUGH_ITEMS = LangEntry.builder("Shop.Trade.Product.NotEnoughItems").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("You don't have " + SOFT_RED.wrap(GENERIC_PRODUCTS) + " to sell.")
    );

    public static final MessageLocale SHOP_TRADE_FEEDBACK_EMPTY = LangEntry.builder("Shop.Trade.Feedback.Empty").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("No items were proceeded.")
    );

    public static final MessageLocale SHOP_TRADE_FEEDBACK_UNEXPECTED_ERROR = LangEntry.builder("Shop.Trade.Feedback.UnexpectedError").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("An unexpected error occured during the transaction. Please try again.")
    );

    public static final MessageLocale SHOP_TRADE_FEEDBACK_LOOSE_ITEMS = LangEntry.builder("Shop.Trade.Feedback.FailedItems").chatMessage(
        GRAY.wrap("We were unable to proceed " + SHOW_TEXT.with(GENERIC_LOOSE).wrap(RED.and(UNDERLINED).wrap(GENERIC_LOOSE_SIZE + " items")) + " " + GRAY.wrap("(hover for info)") + " during last transaction."));

    public static final TextLocale SHOP_TRADE_PRODUCT_ENTRY_ONE = LangEntry.builder("Shop.Trade.Product.Entry.One").text(
        GENERIC_AMOUNT + " x " + PRODUCT_PREVIEW_NAME
    );

    public static final TextLocale SHOP_TRADE_PRODUCT_ENTRY_MANY = LangEntry.builder("Shop.Trade.Product.Entry.Many").text(
        GENERIC_AMOUNT + " x " + PRODUCT_PREVIEW_NAME + " " + GRAY.wrap("(" + WHITE.wrap(GENERIC_PRICE) + ")")
    );

    public static final TextLocale OTHER_N_A             = LangEntry.builder("Other.NA").text("N/A");
    public static final TextLocale OTHER_NO_RENT         = LangEntry.builder("Other.NoRent").text("< Not Rented >");
    public static final TextLocale OTHER_UNDEFINED       = LangEntry.builder("Other.Undefined").text(SOFT_GRAY.wrap("« " + UNDERLINED.wrap("Undefined") + " »"));
    public static final TextLocale OTHER_PRICE_DELIMITER = LangEntry.builder("Other.PriceDelimiter").text(", ");

    public static final IconLocale EDITOR_GENERIC_BROKEN_ITEM = LangEntry.iconBuilder("Editor.Generic.BrokenItem")
        .accentColor(SOFT_RED)
        .rawName(SOFT_RED.wrap("< Invalid Item> "))
        .rawLore(GRAY.wrap("Item tag/ID is broken or invalid."))
        .build();

    @Deprecated
    public static final TextLocale EDITOR_GENERIC_ENTER_NAME    = LangEntry.builder("Editor.Generic.Enter.Name").text(GRAY.wrap("Enter " + GREEN.wrap("[Name]")));
    @Deprecated
    public static final TextLocale EDITOR_GENERIC_ENTER_AMOUNT  = LangEntry.builder("Editor.Generic.Enter.Amount").text(GRAY.wrap("Enter " + GREEN.wrap("[Amount]")));
    @Deprecated
    public static final TextLocale EDITOR_GENERIC_ENTER_TIME    = LangEntry.builder("Editor.Generic.Enter.Time").text(GRAY.wrap("Enter " + GREEN.wrap("[Time]") + " like " + GREEN.wrap("22:00")));
    @Deprecated
    public static final TextLocale EDITOR_GENERIC_ENTER_SECONDS = LangEntry.builder("Editor.Generic.Enter.Seconds").text(GRAY.wrap("Enter " + GREEN.wrap("[Seconds Amount]")));
    @Deprecated
    public static final TextLocale EDITOR_PRODUCT_ENTER_PRICE   = LangEntry.builder("Editor.Product.Enter.Price").text(GRAY.wrap("Enter " + GREEN.wrap("[Price]")));
}
