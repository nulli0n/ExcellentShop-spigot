package su.nightexpress.nexshop.config;

import org.bukkit.Sound;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.language.entry.*;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.EnumLocale;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;

import java.time.DayOfWeek;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nexshop.api.shop.type.TradeType.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public final class Lang implements LangContainer {

    public static final EnumLocale<DayOfWeek>   DAYS          = LangEntry.builder("Days").enumeration(DayOfWeek.class);
    public static final EnumLocale<TradeType>   TRADE_TYPES   = LangEntry.builder("TradeType").enumeration(TradeType.class);
    public static final EnumLocale<PriceType>   PRICE_TYPES   = LangEntry.builder("PriceType").enumeration(PriceType.class);
    public static final EnumLocale<ContentType> PRODUCT_TYPES = LangEntry.builder("ProductType").enumeration(ContentType.class);

    public static final MessageLocale SHOP_PRODUCT_ERROR_INVALID_CART_UI = LangEntry.builder("Shop.Product.Error.InvalidCartUI").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        SOFT_RED.wrap("Could not open purchase menu: Cart UI not found."));

    public static final MessageLocale SHOP_PRODUCT_ERROR_UNBUYABLE = LangEntry.builder("Shop.Product.Error.Unbuyable").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        SOFT_RED.wrap(BOLD.wrap("Sorry! ")) + GRAY.wrap("This product can't be purchased currently!"));

    public static final MessageLocale SHOP_PRODUCT_ERROR_UNSELLABLE = LangEntry.builder("Shop.Product.Error.Unsellable").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        SOFT_RED.wrap(BOLD.wrap("Sorry! ")) + GRAY.wrap("This product can't be sold currently!"));

    public static final MessageLocale SHOP_PRODUCT_ERROR_OUT_OF_STOCK = LangEntry.builder("Shop.Product.Error.OutOfStock").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        SOFT_RED.wrap(BOLD.wrap("Sorry! ")) + GRAY.wrap("This product is out of stock!"));

    public static final MessageLocale SHOP_PRODUCT_ERROR_OUT_OF_SPACE = LangEntry.builder("Shop.Product.Error.OutOfSpace").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        SOFT_RED.wrap(BOLD.wrap("Sorry! ")) + GRAY.wrap("The shop is out of space!"));

    public static final MessageLocale SHOP_PRODUCT_ERROR_OUT_OF_FUNDS = LangEntry.builder("Shop.Product.Error.OutOfFunds").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        SOFT_RED.wrap(BOLD.wrap("Sorry! ")) + GRAY.wrap("The shop is out of money!"));

    public static final MessageLocale SHOP_PRODUCT_ERROR_FULL_STOCK = LangEntry.builder("Shop.Product.Error.FullStock").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        SOFT_RED.wrap(BOLD.wrap("Sorry! ")) + GRAY.wrap("This product is full of stock!"));

    public static final MessageLocale SHOP_PRODUCT_ERROR_FULL_INVENTORY = LangEntry.builder("Shop.Product.Error.FullInventory").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        SOFT_RED.wrap(BOLD.wrap("Sorry! ")) + GRAY.wrap("You have to clean up your inventory before purchase!"));

    public static final MessageLocale SHOP_PRODUCT_ERROR_TOO_EXPENSIVE = LangEntry.builder("Shop.Product.Error.TooExpensive").titleMessage(
        SOFT_RED.wrap(BOLD.wrap("Too Expensive!")),
        GRAY.wrap("You need " + SOFT_RED.wrap(GENERIC_PRICE) + "!"),
        Sound.BLOCK_ANVIL_PLACE
    );

    public static final MessageLocale SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS = LangEntry.builder("Shop.Product.Error.NotEnoughItems").titleMessage(
        SOFT_RED.wrap(BOLD.wrap("Not Enough Items!")),
        GRAY.wrap("You need " + SOFT_RED.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + "!"),
        Sound.BLOCK_ANVIL_PLACE
    );

    public static final TextLocale SHOP_CART_ENTER_AMOUNT = LangEntry.builder("Shop.Cart.CustomAmount").text(GRAY.wrap("Enter your amount..."));

    public static final TextLocale OTHER_PRICE_DISABLED = LangEntry.builder("Other.PriceDisabled").text("N/A");
    public static final TextLocale OTHER_NO_RENT = LangEntry.builder("Other.NoRent").text("< Not Rented >");
    public static final TextLocale OTHER_UNDEFINED = LangEntry.builder("Other.Undefined").text(SOFT_GRAY.wrap("« " + UNDERLINED.wrap("Undefined") + " »"));

    public static final TextLocale EDITOR_PRICE_FLOAT_NO_DAYS = LangEntry.builder("Editor.Price.Float.NoDays").text("No days set. Price won't refresh properly.");
    public static final TextLocale EDITOR_PRICE_FLOAT_NO_TIMES = LangEntry.builder("Editor.Price.Float.NoTimes").text("No times set. Price won't refresh properly.");

    public static final IconLocale EDITOR_GENERIC_BROKEN_ITEM = LangEntry.iconBuilder("Editor.Generic.BrokenItem")
        .accentColor(SOFT_RED)
        .rawName(SOFT_RED.wrap("< Invalid Item> "))
        .rawLore(GRAY.wrap("Item tag/ID is broken or invalid."))
        .build();

    @Deprecated public static final TextLocale EDITOR_GENERIC_ENTER_NAME = LangEntry.builder("Editor.Generic.Enter.Name").text(GRAY.wrap("Enter " + GREEN.wrap("[Name]")));
    @Deprecated public static final TextLocale EDITOR_GENERIC_ENTER_AMOUNT = LangEntry.builder("Editor.Generic.Enter.Amount").text(GRAY.wrap("Enter " + GREEN.wrap("[Amount]")));
    @Deprecated public static final TextLocale EDITOR_GENERIC_ENTER_VALUE = LangEntry.builder("Editor.Generic.Enter.Value").text(GRAY.wrap("Enter " + GREEN.wrap("[Value]")));
    @Deprecated public static final TextLocale EDITOR_GENERIC_ENTER_DAY = LangEntry.builder("Editor.Generic.Enter.Day").text(GRAY.wrap("Enter " + GREEN.wrap("[Day Name]")));
    @Deprecated public static final TextLocale EDITOR_GENERIC_ENTER_TIME = LangEntry.builder("Editor.Generic.Enter.Time").text(GRAY.wrap("Enter " + GREEN.wrap("[Time]") + " like " + GREEN.wrap("22:00")));
    @Deprecated public static final TextLocale EDITOR_GENERIC_ENTER_SECONDS = LangEntry.builder("Editor.Generic.Enter.Seconds").text(GRAY.wrap("Enter " + GREEN.wrap("[Seconds Amount]")));
    @Deprecated public static final TextLocale EDITOR_PRODUCT_ENTER_PRICE = LangEntry.builder("Editor.Product.Enter.Price").text(GRAY.wrap("Enter " + GREEN.wrap("[Price]")));
    @Deprecated public static final TextLocale EDITOR_PRODUCT_ENTER_UNI_PRICE = LangEntry.builder("Editor.Product.Enter.UniPrice").text(GRAY.wrap("Enter " + GREEN.wrap("[Min] [Max]")));
    @Deprecated  public static final TextLocale EDITOR_PRODUCT_ENTER_CURRENCY = LangEntry.builder("Editor.Product.Enter.Currency").text(GRAY.wrap("Enter " + GREEN.wrap("[Currency Identifier]")));




    public static final LangUIButton PRODUCT_PRICE_RESET = LangUIButton.builder("Editor.Product.Price.Reset", "Reset & Update")
        .current("Buy Price", PRODUCT_PRICE_FORMATTED.apply(BUY))
        .current("Sell Price", PRODUCT_PRICE_FORMATTED.apply(SELL))
        .description("Resets product's price data", "and refreshes its values.")
        .click("reset")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_TYPE = LangUIButton.builder("Editor.Product.Price.Type", "Price Type")
        .current(PRODUCT_PRICE_TYPE)
        .click("change")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_CURRENCY = LangUIButton.builder("Editor.Product.Price.Currency", "Currency")
        .current(PRODUCT_CURRENCY)
        .click("change")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_FLAT_BUY = LangUIButton.builder("Editor.Product.Price.Flat.Buy", "Buy Price")
        .current(PRODUCT_PRICE.apply(BUY))
        .leftClick("change")
        .dropKey("disable")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_FLAT_SELL = LangUIButton.builder("Editor.Product.Price.Flat.Sell", "Sell Price")
        .current(PRODUCT_PRICE.apply(SELL))
        .leftClick("change")
        .dropKey("disable")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_BOUNDS_BUY = LangUIButton.builder("Editor.Product.Price.Float.Buy", "Buy Price Bounds")
        .current("Min", PRICER_RANGED_BOUNDS_MIN.apply(BUY))
        .current("Max", PRICER_RANGED_BOUNDS_MAX.apply(BUY))
        .description("Sets product buy price bounds.", "Final price will be within these values.")
        .leftClick("change")
        .dropKey("disable")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_BOUNDS_SELL = LangUIButton.builder("Editor.Product.Price.Float.Sell", "Sell Price Bounds")
        .current("Min", PRICER_RANGED_BOUNDS_MIN.apply(SELL))
        .current("Max", PRICER_RANGED_BOUNDS_MAX.apply(SELL))
        .description("Sets product sell price bounds.", "Final price will be within these values.")
        .leftClick("change")
        .dropKey("disable")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_FLOAT_DECIMALS = LangUIButton.builder("Editor.Product.Price.Float.Decimals", "Cut Decimals")
        .current("Enabled", PRICER_FLOAT_ROUND_DECIMALS)
        .description("Controls whether final price", "should be integer.")
        .click("toggle")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_FLOAT_REFRESH_TYPE = LangUIButton.builder("Editor.Product.Price.Float.RefreshType", "Refresh Type")
        .current(PRICER_FLOAT_REFRESH_TYPE)
        .description(
            SOFT_YELLOW.wrap(BOLD.wrap("Interval:")),
            "Performs refresh every X seconds.",
            "",
            SOFT_YELLOW.wrap(BOLD.wrap("Fixed:")),
            "Performs refresh at given times."
        )
        .click("toggle")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_FLOAT_REFRESH_INTERVAL = LangUIButton.builder("Editor.Product.Price.Float.RefreshInterval", "Refresh Interval")
        .current(PRICER_FLOAT_REFRESH_INTERVAL)
        .description("Sets refresh interval (in seconds).")
        .click("change")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_FLOAT_REFRESH_DAYS = LangUIButton.builder("Editor.Product.Price.Float.RefreshDays", "Refresh Days")
        .description(PRICER_FLOAT_REFRESH_DAYS)
        .leftClick("add day")
        .rightClick("remove all")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_FLOAT_REFRESH_TIMES = LangUIButton.builder("Editor.Product.Price.Float.RefreshTimes", "Refresh Times")
        .description(PRICER_FLOAT_REFRESH_TIMES)
        .leftClick("add time")
        .rightClick("remove all")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_DYNAMIC_INITIAL = LangUIButton.builder("Editor.Product.Price.Dynamic.Initial", "Initial Values")
        .current("Buy", PRICER_DYNAMIC_INITIAL_BUY)
        .current("Sell", PRICER_DYNAMIC_INITIAL_SELL)
        .description("Sets initial (start) product price.")
        .leftClick("change buy")
        .rightClick("change sell")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_DYNAMIC_STEP = LangUIButton.builder("Editor.Product.Price.Dynamic.Step", "Price Step")
        .current("Buy", PRICER_DYNAMIC_STEP_BUY)
        .current("Sell", PRICER_DYNAMIC_STEP_SELL)
        .description("Adjusts prices by specified", "values on each purchase/sale.")
        .leftClick("change buy")
        .rightClick("change sell")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_PLAYERS_INITIAL = LangUIButton.builder("Editor.Product.Price.Players.Initial", "Initial Values")
        .current("Buy", PRICER_DYNAMIC_INITIAL_BUY)
        .current("Sell", PRICER_DYNAMIC_INITIAL_SELL)
        .description("Sets initial (start) product price.")
        .leftClick("change buy")
        .rightClick("change sell")
        .build();

    public static final LangUIButton PRODUCT_EDIT_PRICE_PLAYERS_ADJUST = LangUIButton.builder("Editor.Product.Price.Players.Adjust", "Adjust Amount")
        .current("Buy", PRICER_PLAYERS_ADJUST_AMOUNT_BUY)
        .current("Sell", PRICER_PLAYERS_ADJUST_AMOUNT_SELL)
        .description("Adjusts prices by specified", "values with a multiplier of", "online players amount.")
        .leftClick("change buy")
        .rightClick("change sell")
        .build();
}
