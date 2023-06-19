package su.nightexpress.nexshop.config;

import org.bukkit.Sound;
import su.nexmedia.engine.api.lang.LangColors;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;
import su.nightexpress.nexshop.Placeholders;


public class Lang extends EngineLang implements LangColors {

    public static final LangKey COMMAND_CURRENCY_DESC  = LangKey.of("Command.Currency.Desc", "Manage plugin currencies.");
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

    public static final LangKey Module_Cmd_Reload = LangKey.of("Module.Cmd.Reload", "Module &a%module% &7reloaded!");

    public static final LangKey ERROR_CURRENCY_INVALID = LangKey.of("Error.Currency.Invalid", "&cInvalid currency!");

    public static final LangKey EDITOR_GENERIC_ENTER_NAME     = LangKey.of("Editor.Generic.Enter.Name", "&7Enter &a[Name]");
    public static final LangKey EDITOR_GENERIC_ENTER_AMOUNT   = LangKey.of("Editor.Generic.Enter.Amount", "&7Enter &a[Amount]");
    public static final LangKey EDITOR_GENERIC_ENTER_DAY      = LangKey.of("Editor.Generic.Enter.Day", "&7Enter &aDay &7in &aEnglish");
    public static final LangKey EDITOR_GENERIC_ENTER_TIME     = LangKey.of("Editor.Generic.Enter.Time", "&7Enter &aTime&7 like &a18:00:00");
    public static final LangKey EDITOR_GENERIC_ENTER_SECONDS  = LangKey.of("Editor.Generic.Enter.Seconds", "&7Enter &aseconds &7amount");
    public static final LangKey EDITOR_GENERIC_ERROR_CURRENCY = LangKey.of("Editor.Generic.Error.Currency", "&cInvalid currency!");
    public static final LangKey EDITOR_PRODUCT_ENTER_PRICE    = LangKey.of("Editor.Product.Enter.Price", "&7Enter new &aprice");
    public static final LangKey EDITOR_PRODUCT_ENTER_CURRENCY = LangKey.of("Editor.Product.Enter.Currency", "&7Enter &acurrency id");

    public static final LangKey SHOP_PRODUCT_ERROR_UNBUYABLE      = LangKey.of("Shop.Product.Error.Unbuyable", "&cYou can not buy this item!");
    public static final LangKey SHOP_PRODUCT_ERROR_UNSELLABLE     = LangKey.of("Shop.Product.Error.Unsellable", "&cYou can not sell this item!");
    public static final LangKey SHOP_PRODUCT_ERROR_OUT_OF_STOCK   = LangKey.of("Shop.Product.Error.OutOfStock", "&cThis product is out of stock!");
    public static final LangKey SHOP_PRODUCT_ERROR_OUT_OF_SPACE   = LangKey.of("Shop.Product.Error.OutOfSpace", "&cThis shop is out of space!");
    public static final LangKey SHOP_PRODUCT_ERROR_OUT_OF_FUNDS   = LangKey.of("Shop.Product.Error.OutOfFunds", "&cThis shop is out of money!");
    public static final LangKey SHOP_PRODUCT_ERROR_FULL_STOCK     = LangKey.of("Shop.Product.Error.FullStock", "&cThis product is full of stock!");
    public static final LangKey SHOP_PRODUCT_ERROR_FULL_INVENTORY = LangKey.of("Shop.Product.Error.FullInventory", "&cYou can't buy items while your inventory is full!");

    public static final LangKey SHOP_PRODUCT_ERROR_TOO_EXPENSIVE    = LangKey.of("Shop.Product.Error.TooExpensive",
        "<! type:\"titles:15:60:15\" sound:\"" + Sound.BLOCK_ANVIL_PLACE.name() + "\" !>" +
            "\n&c&lToo Expensive! " +
            "\n &7You need: &c" + Placeholders.GENERIC_PRICE + "&7!");

    public static final LangKey SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS = LangKey.of("Shop.Product.Error.NotEnoughItems",
        "<! type:\"titles:15:60:15\" sound:\"" + Sound.BLOCK_ANVIL_PLACE.name() + "\" !>" +
            "\n&c&lNot Enough Items! " +
            "\n &7You need: &cx" + Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_ITEM + "&7!");
}
