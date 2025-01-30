package su.nightexpress.nexshop.config;

import org.bukkit.Sound;
import su.nightexpress.economybridge.Placeholders;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.language.entry.LangItem;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import java.time.DayOfWeek;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.*;

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
        LIGHT_GRAY.enclose("Given " + LIGHT_YELLOW.enclose(GENERIC_AMOUNT + " " + Placeholders.CURRENCY_NAME) + " to " + LIGHT_YELLOW.enclose(PLAYER_NAME) + "."));

    public static final LangString COMMAND_CURRENCY_TAKE_DESC = LangString.of("Command.Currency.Take.Desc", "Take currency from a player.");

    public static final LangText COMMAND_CURRENCY_TAKE_DONE = LangText.of("Command.Currency.Take.Done",
        LIGHT_GRAY.enclose("Took " + LIGHT_YELLOW.enclose(GENERIC_AMOUNT + " " + Placeholders.CURRENCY_NAME) + " from " + LIGHT_YELLOW.enclose(PLAYER_NAME) + "."));

    public static final LangString COMMAND_CURRENCY_CREATE_DESC = LangString.of("Command.Currency.Create.Desc", "Create an item currency.");

    public static final LangText COMMAND_CURRENCY_CREATE_DONE_NEW = LangText.of("Command.Currency.Create.Done.New",
        LIGHT_GRAY.enclose("Created a new currency " + LIGHT_GREEN.enclose(GENERIC_NAME) + " as " + LIGHT_GREEN.enclose(GENERIC_ITEM) + "."));

    public static final LangText COMMAND_CURRENCY_CREATE_ERROR_EXIST = LangText.of("Command.Currency.Create.Error.Exist",
        LIGHT_GRAY.enclose("Currency " + LIGHT_RED.enclose(Placeholders.CURRENCY_ID) + " already exists and is not an item currency."));

    public static final LangText COMMAND_CURRENCY_ERROR_NO_ITEM = LangText.of("Command.Currency.Error.NoItem",
        LIGHT_RED.enclose("You must hold an item to do that!"));



    public static final LangText MODULE_COMMAND_RELOAD = LangText.of("Module.Command.Reloaded",
        LIGHT_GRAY.enclose(LIGHT_GREEN.enclose(GENERIC_NAME) + " reloaded!"));

    public static final LangString MODULE_COMMAND_RELOAD_DESC = LangString.of("Module.Command.Reload.Desc", "Reload the module.");



    public static final LangText ERROR_INVALID_CURRENCY = LangText.of("Error.Currency.Invalid",
        LIGHT_RED.enclose("Invalid currency!"));



    public static final LangText SHOP_PRODUCT_ERROR_INVALID_CART_UI = LangText.of("Shop.Product.Error.InvalidCartUI",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.enclose("Could not open purchase menu: Cart UI not found."));

    public static final LangText SHOP_PRODUCT_ERROR_UNBUYABLE = LangText.of("Shop.Product.Error.Unbuyable",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.enclose(BOLD.enclose("Sorry! ")) + LIGHT_GRAY.enclose("This product can't be purchased currently!"));

    public static final LangText SHOP_PRODUCT_ERROR_UNSELLABLE = LangText.of("Shop.Product.Error.Unsellable",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.enclose(BOLD.enclose("Sorry! ")) + LIGHT_GRAY.enclose("This product can't be sold currently!"));

    public static final LangText SHOP_PRODUCT_ERROR_OUT_OF_STOCK = LangText.of("Shop.Product.Error.OutOfStock",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.enclose(BOLD.enclose("Sorry! ")) + LIGHT_GRAY.enclose("This product is out of stock!"));

    public static final LangText SHOP_PRODUCT_ERROR_OUT_OF_SPACE = LangText.of("Shop.Product.Error.OutOfSpace",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.enclose(BOLD.enclose("Sorry! ")) + LIGHT_GRAY.enclose("The shop is out of space!"));

    public static final LangText SHOP_PRODUCT_ERROR_OUT_OF_FUNDS = LangText.of("Shop.Product.Error.OutOfFunds",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.enclose(BOLD.enclose("Sorry! ")) + LIGHT_GRAY.enclose("The shop is out of money!"));

    public static final LangText SHOP_PRODUCT_ERROR_FULL_STOCK = LangText.of("Shop.Product.Error.FullStock",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.enclose(BOLD.enclose("Sorry! ")) + LIGHT_GRAY.enclose("This product is full of stock!"));

    public static final LangText SHOP_PRODUCT_ERROR_FULL_INVENTORY = LangText.of("Shop.Product.Error.FullInventory",
        SOUND.enclose(Sound.ENTITY_VILLAGER_NO),
        LIGHT_RED.enclose(BOLD.enclose("Sorry! ")) + LIGHT_GRAY.enclose("You have to clean up your inventory before purchase!"));

    public static final LangString SHOP_CART_ENTER_AMOUNT = LangString.of("Shop.Cart.CustomAmount",
        //OUTPUT.enclose(20, 1200),
        //LIGHT_GREEN.enclose(BOLD.enclose("< Custom Amount >")),
        LIGHT_GRAY.enclose("Enter your amount...")
    );



    public static final LangString OTHER_PRICE_DISABLED = LangString.of("Other.PriceDisabled", "N/A");

    public static final LangString EDITOR_PRICE_FLOAT_NO_DAYS = LangString.of("Editor.Price.Float.NoDays",
        "No days set. Price won't refresh properly.");

    public static final LangString EDITOR_PRICE_FLOAT_NO_TIMES = LangString.of("Editor.Price.Float.NoTimes",
        "No times set. Price won't refresh properly.");

    public static final LangItem EDITOR_GENERIC_BROKEN_ITEM = LangItem.of("Editor.Generic.BrokenItem",
        LIGHT_RED.enclose(BOLD.enclose("< Invalid Item> ")),
        LIGHT_GRAY.enclose("Item tag/ID is broken"),
        LIGHT_GRAY.enclose("or invalid.")
    );

    public static final LangString EDITOR_GENERIC_ENTER_NAME = LangString.of("Editor.Generic.Enter.Name",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Name]")));

    public static final LangString EDITOR_GENERIC_ENTER_AMOUNT = LangString.of("Editor.Generic.Enter.Amount",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Amount]")));

    public static final LangString EDITOR_GENERIC_ENTER_VALUE = LangString.of("Editor.Generic.Enter.Value",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Value]")));

    public static final LangString EDITOR_GENERIC_ENTER_DAY = LangString.of("Editor.Generic.Enter.Day",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Day Name]")));

    public static final LangString EDITOR_GENERIC_ENTER_TIME = LangString.of("Editor.Generic.Enter.Time",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Time]") + " like " + LIGHT_GREEN.enclose("22:00")));

    public static final LangString EDITOR_GENERIC_ENTER_SECONDS = LangString.of("Editor.Generic.Enter.Seconds",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Seconds Amount]")));

    public static final LangString EDITOR_PRODUCT_ENTER_PRICE = LangString.of("Editor.Product.Enter.Price",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Price]")));

    public static final LangString EDITOR_PRODUCT_ENTER_UNI_PRICE = LangString.of("Editor.Product.Enter.UniPrice",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Min] [Max]")));

    public static final LangString EDITOR_PRODUCT_ENTER_CURRENCY = LangString.of("Editor.Product.Enter.Currency",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Currency Identifier]")));

    public static final LangText SHOP_PRODUCT_ERROR_TOO_EXPENSIVE = LangText.of("Shop.Product.Error.TooExpensive",
        OUTPUT.enclose(15, 60) + SOUND.enclose(Sound.BLOCK_ANVIL_PLACE),
        LIGHT_RED.enclose(BOLD.enclose("Too Expensive!")),
        LIGHT_GRAY.enclose("You need " + LIGHT_RED.enclose(GENERIC_PRICE) + "!")
    );

    public static final LangText SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS = LangText.of("Shop.Product.Error.NotEnoughItems",
        OUTPUT.enclose(15, 60) + SOUND.enclose(Sound.BLOCK_ANVIL_PLACE),
        LIGHT_RED.enclose(BOLD.enclose("Not Enough Items!")),
        LIGHT_GRAY.enclose("You need " + LIGHT_RED.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + "!")
    );
}
