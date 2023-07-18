package su.nightexpress.nexshop.config;

import org.bukkit.Sound;
import su.nexmedia.engine.api.lang.LangColors;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;
import su.nightexpress.nexshop.Placeholders;


public class Lang extends EngineLang implements LangColors {

    public static final LangKey COMMAND_CURRENCY_DESC  = LangKey.of("Command.Currency.Desc", "Currency manager.");
    public static final LangKey COMMAND_CURRENCY_USAGE = LangKey.of("Command.Currency.Usage", "[help]");

    public static final LangKey COMMAND_CURRENCY_GIVE_DESC  = LangKey.of("Command.Currency.Give.Desc", "Give specified currency to a player.");
    public static final LangKey COMMAND_CURRENCY_GIVE_USAGE = LangKey.of("Command.Currency.Give.Usage", "<currency> <player> <amount>");
    public static final LangKey COMMAND_CURRENCY_GIVE_DONE  = LangKey.of("Command.Currency.Give.Done", "Given &ax%amount% %currency_name%&7 to &a%player_name%&7.");

    public static final LangKey COMMAND_CURRENCY_TAKE_DESC  = LangKey.of("Command.Currency.Take.Desc", "Take specified currency from a player.");
    public static final LangKey COMMAND_CURRENCY_TAKE_USAGE = LangKey.of("Command.Currency.Take.Usage", "<currency> <player> <amount>");
    public static final LangKey COMMAND_CURRENCY_TAKE_DONE  = LangKey.of("Command.Currency.Take.Done", "Took &ax%amount% %currency_name%&7 from &a%player_name%&7.");

    public static final LangKey COMMAND_CURRENCY_CREATE_DESC         = LangKey.of("Command.Currency.Create.Desc", "Create/replace a currency from/with the item in hand.");
    public static final LangKey COMMAND_CURRENCY_CREATE_USAGE        = LangKey.of("Command.Currency.Create.Usage", "<name>");
    public static final LangKey COMMAND_CURRENCY_CREATE_DONE_NEW     = LangKey.of("Command.Currency.Create.Done.New", "Created a new currency &a%currency_id%&7 as &a%item%&7.");
    public static final LangKey COMMAND_CURRENCY_CREATE_DONE_REPLACE = LangKey.of("Command.Currency.Create.Done.Replace", "Replaced item in the currency &a%currency_id%&7 with &a%item%&7.");
    public static final LangKey COMMAND_CURRENCY_CREATE_ERROR_EXIST  = LangKey.of("Command.Currency.Create.Error.Exist", "Currency &c%currency_id%&7 is already exist and is not an Item Currency.");
    public static final LangKey COMMAND_CURRENCY_ERROR_NO_ITEM = LangKey.of("Command.Currency.Error.NoItem", RED + "You must hold an item to do that!");

    public static final LangKey MODULE_COMMAND_RELOAD = LangKey.of("Module.Command.Reload", GREEN + Placeholders.GENERIC_NAME + GRAY + " reloaded!");

    public static final LangKey ERROR_CURRENCY_INVALID = LangKey.of("Error.Currency.Invalid", RED + "Invalid currency!");

    public static final LangKey SHOP_PRODUCT_ERROR_UNBUYABLE = LangKey.of("Shop.Product.Error.Unbuyable",
        "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" +
            RED + "&lSorry! " + GRAY + "This product can't be purchased at the moment!");

    public static final LangKey SHOP_PRODUCT_ERROR_UNSELLABLE = LangKey.of("Shop.Product.Error.Unsellable",
        "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" +
            RED + "&lSorry! " + GRAY + "This product can't be sold at the moment!");

    public static final LangKey SHOP_PRODUCT_ERROR_OUT_OF_STOCK = LangKey.of("Shop.Product.Error.OutOfStock",
        "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" +
            RED + "&lSorry! " + GRAY + "This product is out of stock!");

    public static final LangKey SHOP_PRODUCT_ERROR_OUT_OF_SPACE = LangKey.of("Shop.Product.Error.OutOfSpace",
        "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" +
            RED + "&lSorry! " + GRAY + "The shop is out of space!");

    public static final LangKey SHOP_PRODUCT_ERROR_OUT_OF_FUNDS = LangKey.of("Shop.Product.Error.OutOfFunds",
        "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" +
            RED + "&lSorry! " + GRAY + "The shop is out of money!");

    public static final LangKey SHOP_PRODUCT_ERROR_FULL_STOCK = LangKey.of("Shop.Product.Error.FullStock",
        "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" +
            RED + "&lSorry! " + GRAY + "This product is full of stock!");

    public static final LangKey SHOP_PRODUCT_ERROR_FULL_INVENTORY = LangKey.of("Shop.Product.Error.FullInventory",
        "<! sound:\"" + Sound.ENTITY_VILLAGER_NO.name() + "\" !>" +
            RED + "&lSorry! " + GRAY + "You have to clean up your inventory before purchase!");

    public static final LangKey EDITOR_GENERIC_ENTER_NAME     = LangKey.of("Editor.Generic.Enter.Name", GRAY + "Enter " + GREEN + "[Name]");
    public static final LangKey EDITOR_GENERIC_ENTER_AMOUNT   = LangKey.of("Editor.Generic.Enter.Amount", GRAY + "Enter " + GREEN + "[Amount]");
    public static final LangKey EDITOR_GENERIC_ENTER_DAY      = LangKey.of("Editor.Generic.Enter.Day", GRAY + "Enter " + GREEN + "[English Day Name]");
    public static final LangKey EDITOR_GENERIC_ENTER_TIME     = LangKey.of("Editor.Generic.Enter.Time", GRAY + "Enter " + GREEN + "[Hours:Minutes:Seconds]");
    public static final LangKey EDITOR_GENERIC_ENTER_SECONDS  = LangKey.of("Editor.Generic.Enter.Seconds", GRAY + "Enter " + GREEN + "[Seconds Amount]");
    public static final LangKey EDITOR_PRODUCT_ENTER_PRICE    = LangKey.of("Editor.Product.Enter.Price", GRAY + "Enter " + GREEN + "[Price]");
    public static final LangKey EDITOR_PRODUCT_ENTER_CURRENCY = LangKey.of("Editor.Product.Enter.Currency", GRAY + "Enter " + GREEN + "[Currency Identifier]");

    public static final LangKey SHOP_PRODUCT_ERROR_TOO_EXPENSIVE = LangKey.of("Shop.Product.Error.TooExpensive",
        "<! type:\"titles:15:60:15\" sound:\"" + Sound.BLOCK_ANVIL_PLACE.name() + "\" !>" +
            "\n" + RED + "&lToo Expensive! " +
            "\n" + GRAY + "You need: " + RED + Placeholders.GENERIC_PRICE + GRAY + "!");

    public static final LangKey SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS = LangKey.of("Shop.Product.Error.NotEnoughItems",
        "<! type:\"titles:15:60:15\" sound:\"" + Sound.BLOCK_ANVIL_PLACE.name() + "\" !>" +
            "\n" + RED + "&lNot Enough Items! " +
            "\n" + GRAY + "You need: " + RED + "x" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + GRAY + "!");
}
