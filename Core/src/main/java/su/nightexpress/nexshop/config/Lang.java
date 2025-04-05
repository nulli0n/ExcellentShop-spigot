package su.nightexpress.nexshop.config;

import org.bukkit.Sound;
import su.nightexpress.economybridge.Placeholders;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.*;

import java.time.DayOfWeek;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nexshop.api.shop.type.TradeType.BUY;
import static su.nightexpress.nexshop.api.shop.type.TradeType.SELL;
import static su.nightexpress.nightcore.language.tag.MessageTags.OUTPUT;
import static su.nightexpress.nightcore.language.tag.MessageTags.SOUND;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class Lang extends CoreLang {

    public static final LangEnum<DayOfWeek>   DAYS          = LangEnum.of("Days", DayOfWeek.class);
    public static final LangEnum<TradeType>   TRADE_TYPES   = LangEnum.of("TradeType", TradeType.class);
    public static final LangEnum<PriceType>   PRICE_TYPES   = LangEnum.of("PriceType", PriceType.class);
    public static final LangEnum<ProductType> PRODUCT_TYPES = LangEnum.of("ProductType", ProductType.class);

    public static final LangString COMMAND_ARGUMENT_NAME_NAME     = LangString.of("Command.Argument.Name.Name", "name");
    public static final LangString COMMAND_ARGUMENT_NAME_CURRENCY = LangString.of("Command.Argument.Name.Currency", "currency");
    public static final LangString COMMAND_ARGUMENT_NAME_PRICE    = LangString.of("Command.Argument.Name.Price", "price");

    public static final LangString COMMAND_CURRENCY_DESC = LangString.of("Command.Currency.Desc", "Currency manager.");

    public static final LangString COMMAND_CURRENCY_GIVE_DESC = LangString.of("Command.Currency.Give.Desc", "Give currency to a player.");

    public static final LangText COMMAND_CURRENCY_GIVE_DONE = LangText.of("Command.Currency.Give.Done",
        LIGHT_GRAY.wrap("Given " + LIGHT_YELLOW.wrap(GENERIC_AMOUNT + " " + Placeholders.CURRENCY_NAME) + " to " + LIGHT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final LangString COMMAND_CURRENCY_TAKE_DESC = LangString.of("Command.Currency.Take.Desc", "Take currency from a player.");

    public static final LangText COMMAND_CURRENCY_TAKE_DONE = LangText.of("Command.Currency.Take.Done",
        LIGHT_GRAY.wrap("Took " + LIGHT_YELLOW.wrap(GENERIC_AMOUNT + " " + Placeholders.CURRENCY_NAME) + " from " + LIGHT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final LangString COMMAND_CURRENCY_CREATE_DESC = LangString.of("Command.Currency.Create.Desc", "Create an item currency.");

    public static final LangText COMMAND_CURRENCY_CREATE_DONE_NEW = LangText.of("Command.Currency.Create.Done.New",
        LIGHT_GRAY.wrap("Created a new currency " + LIGHT_GREEN.wrap(GENERIC_NAME) + " as " + LIGHT_GREEN.wrap(GENERIC_ITEM) + "."));

    public static final LangText COMMAND_CURRENCY_CREATE_ERROR_EXIST = LangText.of("Command.Currency.Create.Error.Exist",
        LIGHT_GRAY.wrap("Currency " + LIGHT_RED.wrap(Placeholders.CURRENCY_ID) + " already exists and is not an item currency."));

    public static final LangText COMMAND_CURRENCY_ERROR_NO_ITEM = LangText.of("Command.Currency.Error.NoItem",
        LIGHT_RED.wrap("You must hold an item to do that!"));


    public static final LangText MODULE_COMMAND_RELOAD = LangText.of("Module.Command.Reloaded",
        LIGHT_GRAY.wrap(LIGHT_GREEN.wrap(GENERIC_NAME) + " reloaded!"));

    public static final LangString MODULE_COMMAND_RELOAD_DESC = LangString.of("Module.Command.Reload.Desc", "Reload the module.");


    public static final LangText ERROR_INVALID_CURRENCY = LangText.of("Error.Currency.Invalid",
        LIGHT_RED.wrap("Invalid currency!"));


    public static final LangText SHOP_PRODUCT_ERROR_INVALID_CART_UI = LangText.of("Shop.Product.Error.InvalidCartUI",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap("Could not open purchase menu: Cart UI not found."));

    public static final LangText SHOP_PRODUCT_ERROR_UNBUYABLE = LangText.of("Shop.Product.Error.Unbuyable",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Sorry! ")) + LIGHT_GRAY.wrap("This product can't be purchased currently!"));

    public static final LangText SHOP_PRODUCT_ERROR_UNSELLABLE = LangText.of("Shop.Product.Error.Unsellable",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Sorry! ")) + LIGHT_GRAY.wrap("This product can't be sold currently!"));

    public static final LangText SHOP_PRODUCT_ERROR_OUT_OF_STOCK = LangText.of("Shop.Product.Error.OutOfStock",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Sorry! ")) + LIGHT_GRAY.wrap("This product is out of stock!"));

    public static final LangText SHOP_PRODUCT_ERROR_OUT_OF_SPACE = LangText.of("Shop.Product.Error.OutOfSpace",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Sorry! ")) + LIGHT_GRAY.wrap("The shop is out of space!"));

    public static final LangText SHOP_PRODUCT_ERROR_OUT_OF_FUNDS = LangText.of("Shop.Product.Error.OutOfFunds",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Sorry! ")) + LIGHT_GRAY.wrap("The shop is out of money!"));

    public static final LangText SHOP_PRODUCT_ERROR_FULL_STOCK = LangText.of("Shop.Product.Error.FullStock",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Sorry! ")) + LIGHT_GRAY.wrap("This product is full of stock!"));

    public static final LangText SHOP_PRODUCT_ERROR_FULL_INVENTORY = LangText.of("Shop.Product.Error.FullInventory",
        SOUND.wrap(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.wrap(BOLD.wrap("Sorry! ")) + LIGHT_GRAY.wrap("You have to clean up your inventory before purchase!"));

    public static final LangText SHOP_PRODUCT_ERROR_TOO_EXPENSIVE = LangText.of("Shop.Product.Error.TooExpensive",
        OUTPUT.wrap(15, 60) + SOUND.wrap(Sound.BLOCK_ANVIL_PLACE),
        LIGHT_RED.wrap(BOLD.wrap("Too Expensive!")),
        LIGHT_GRAY.wrap("You need " + LIGHT_RED.wrap(GENERIC_PRICE) + "!")
    );

    public static final LangText SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS = LangText.of("Shop.Product.Error.NotEnoughItems",
        OUTPUT.wrap(15, 60) + SOUND.wrap(Sound.BLOCK_ANVIL_PLACE),
        LIGHT_RED.wrap(BOLD.wrap("Not Enough Items!")),
        LIGHT_GRAY.wrap("You need " + LIGHT_RED.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + "!")
    );

    public static final LangString SHOP_CART_ENTER_AMOUNT = LangString.of("Shop.Cart.CustomAmount",
        //OUTPUT.wrap(20, 1200),
        //LIGHT_GREEN.wrap(BOLD.wrap("< Custom Amount >")),
        LIGHT_GRAY.wrap("Enter your amount...")
    );


    public static final LangString OTHER_PRICE_DISABLED = LangString.of("Other.PriceDisabled", "N/A");

    public static final LangString EDITOR_PRICE_FLOAT_NO_DAYS = LangString.of("Editor.Price.Float.NoDays",
        "No days set. Price won't refresh properly.");

    public static final LangString EDITOR_PRICE_FLOAT_NO_TIMES = LangString.of("Editor.Price.Float.NoTimes",
        "No times set. Price won't refresh properly.");

    public static final LangUIButton EDITOR_GENERIC_BROKEN_ITEM = LangUIButton.builder("Editor.Generic.BrokenItem", LIGHT_RED.wrap(BOLD.wrap("< Invalid Item> ")))
        .description(
            LIGHT_GRAY.wrap("Item tag/ID is broken"),
            LIGHT_GRAY.wrap("or invalid.")
        ).build();

    public static final LangString EDITOR_GENERIC_ENTER_NAME = LangString.of("Editor.Generic.Enter.Name",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Name]")));

    public static final LangString EDITOR_GENERIC_ENTER_AMOUNT = LangString.of("Editor.Generic.Enter.Amount",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Amount]")));

    public static final LangString EDITOR_GENERIC_ENTER_VALUE = LangString.of("Editor.Generic.Enter.Value",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Value]")));

    public static final LangString EDITOR_GENERIC_ENTER_DAY = LangString.of("Editor.Generic.Enter.Day",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Day Name]")));

    public static final LangString EDITOR_GENERIC_ENTER_TIME = LangString.of("Editor.Generic.Enter.Time",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Time]") + " like " + LIGHT_GREEN.wrap("22:00")));

    public static final LangString EDITOR_GENERIC_ENTER_SECONDS = LangString.of("Editor.Generic.Enter.Seconds",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Seconds Amount]")));

    public static final LangString EDITOR_PRODUCT_ENTER_PRICE = LangString.of("Editor.Product.Enter.Price",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Price]")));

    public static final LangString EDITOR_PRODUCT_ENTER_UNI_PRICE = LangString.of("Editor.Product.Enter.UniPrice",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Min] [Max]")));

    public static final LangString EDITOR_PRODUCT_ENTER_CURRENCY = LangString.of("Editor.Product.Enter.Currency",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Currency Identifier]")));



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
            LIGHT_YELLOW.wrap(BOLD.wrap("Interval:")),
            "Performs refresh every X seconds.",
            "",
            LIGHT_YELLOW.wrap(BOLD.wrap("Fixed:")),
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

    public static final LangUIButton PRODUCT_EDIT_PRICE_PLAYERS_STEP = LangUIButton.builder("Editor.Product.Price.Players.Step", "Adjust Step")
        .current(PRICER_PLAYERS_ADJUST_STEP)
        .description("Adjusts prices for", "every " + PRICER_PLAYERS_ADJUST_STEP + " player(s) online.")
        .click("change")
        .build();
}
