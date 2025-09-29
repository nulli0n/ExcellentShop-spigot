package su.nightexpress.nexshop.shop.virtual.lang;

import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.locale.message.MessageData;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class VirtualLang implements LangContainer {

    private static final String PREFIX = "VirtualShop.";

    @NotNull
    public static LangEntry.Builder builder(@NotNull String key) {
        return LangEntry.builder(PREFIX + key);
    }

    @NotNull
    public static IconLocale.Builder iconBuilder(@NotNull String key) {
        return LangEntry.iconBuilder(PREFIX + key);
    }

    public static final TextLocale COMMAND_ARGUMENT_NAME_SHOP = LangEntry.builder("VirtualShop.Command.Argument.Name.Shop").text("shop");

    public static final TextLocale COMMAND_EDITOR_DESC        = LangEntry.builder("VirtualShop.Command.Editor.Desc").text("Open VirtualShop editor.");
    public static final TextLocale COMMAND_ROTATE_DESC        = LangEntry.builder("VirtualShop.Command.Rotate.Desc").text("Force rotate a shop.");
    public static final TextLocale COMMAND_OPEN_DESC          = LangEntry.builder("VirtualShop.Command.Open.Desc").text("Opens specified shop.");
    public static final TextLocale COMMAND_MENU_DESC          = LangEntry.builder("VirtualShop.Command.Menu.Desc").text("Opens Main Menu.");
    public static final TextLocale COMMAND_SHOP_DESC          = LangEntry.builder("VirtualShop.Command.Shop.Desc").text("Open specified shop or main menu.");
    public static final TextLocale COMMAND_SHOP_ALIAS_DESC    = LangEntry.builder("VirtualShop.Command.ShopAlias.Desc").text("Open " + SHOP_NAME + " shop.");
    public static final TextLocale COMMAND_SELL_MENU_DESC     = LangEntry.builder("VirtualShop.Command.SellMenu.Desc").text("Open Sell GUI.");
    public static final TextLocale COMMAND_SELL_ALL_DESC      = LangEntry.builder("VirtualShop.Command.SellAll.Desc").text("Quickly sell all items in inventory.");
    public static final TextLocale COMMAND_SELL_HAND_DESC     = LangEntry.builder("VirtualShop.Command.SellHand.Desc").text("Quickly sell hand item.");
    public static final TextLocale COMMAND_SELL_HAND_ALL_DESC = LangEntry.builder("VirtualShop.Command.SellHandAll.Desc").text("Quickly sell similar hand items.");

    public static final MessageLocale COMMAND_OPEN_DONE_OTHERS = LangEntry.builder("VirtualShop.Command.Open.Done.Others").chatMessage(
        GRAY.wrap("Opened " + SOFT_YELLOW.wrap(SHOP_NAME) + " shop for " + SOFT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final MessageLocale COMMAND_MENU_DONE_OTHERS = LangEntry.builder("VirtualShop.Command.Menu.Done.Others").chatMessage(
        GRAY.wrap("Opened shops menu for " + SOFT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final MessageLocale COMMAND_SELL_MENU_DONE_OTHERS = LangEntry.builder("VirtualShop.Command.SellMenu.Done.Others").chatMessage(
        GRAY.wrap("Opened sell menu for " + SOFT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final MessageLocale COMMAND_SELL_ALL_DONE_OTHERS = LangEntry.builder("VirtualShop.Command.SellAll.Done.Others").chatMessage(
        GRAY.wrap("Forced player " + SOFT_YELLOW.wrap(PLAYER_NAME) + " to sell all items."));

    public static final MessageLocale COMMAND_SELL_HAND_DONE_OTHERS = LangEntry.builder("VirtualShop.Command.SellHand.Done.Others").chatMessage(
        GRAY.wrap("Forced player " + SOFT_YELLOW.wrap(PLAYER_NAME) + " to sell hand item."));

    public static final MessageLocale COMMAND_ROTATE_DONE = LangEntry.builder("VirtualShop.Command.Rotate.Done").chatMessage(
        GRAY.wrap("Force rotated " + SOFT_YELLOW.wrap(SHOP_NAME) + " shop"));


    // TODO Default command to const
    public static final MessageLocale SHOP_ROTATION_NOTIFY = LangEntry.builder("VirtualShop.Shop.Rotation.Update").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        GRAY.wrap("New items just appeared in the " + SOFT_YELLOW.wrap(SHOP_NAME) + " shop!"),
        GRAY.wrap("Click " + RUN_COMMAND.with("/shop " + SHOP_ID).and(SHOW_TEXT.with(GRAY.wrap("Click to open shop!"))).wrap(SOFT_YELLOW.wrap(BOLD.wrap("HERE"))) + " to open the shop!"),
        " "
    );

    public static final MessageLocale SHOP_ERROR_BAD_WORLD = LangEntry.builder("VirtualShop.Shop.Error.BadWorld").chatMessage(
        SOFT_RED.wrap("You can't use shop in this world!"));

    public static final MessageLocale SHOP_ERROR_BAD_GAMEMODE = LangEntry.builder("VirtualShop.Shop.Error.BadGamemode").chatMessage(
        SOFT_RED.wrap("You can't use shop in current gamemode!"));

    public static final MessageLocale SHOP_ERROR_INVALID_LAYOUT = LangEntry.builder("VirtualShop.Shop.Error.InvalidLayout").chatMessage(
        GRAY.wrap("Could not open shop " + SOFT_RED.wrap(SHOP_NAME) + ": Invalid shop layout!"));

    public static final MessageLocale SHOP_CREATE_ERROR_EXIST = LangEntry.builder("VirtualShop.Shop.Create.Error.Exist").chatMessage(
        SOFT_RED.wrap("Shop with such name already exists!"));

    public static final MessageLocale SHOP_CREATE_ERROR_BAD_NAME = LangEntry.builder("VirtualShop.Shop.Create.Error.BadName").chatMessage(
        SOFT_RED.wrap("Only latin letters and numbers are allowed!"));




    public static final MessageLocale PRODUCT_PURCHASE_SELL = LangEntry.builder("VirtualShop.Product.Purchase.Sell").titleMessage(
        SOFT_GREEN.wrap(BOLD.wrap("Successful Sale!")),
        GRAY.wrap("You sold " + SOFT_GREEN.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + SOFT_GREEN.wrap(GENERIC_PRICE)),
        Sound.ENTITY_EXPERIENCE_ORB_PICKUP
    );

    public static final MessageLocale PRODUCT_PURCHASE_BUY = LangEntry.builder("VirtualShop.Product.Purchase.Buy").titleMessage(
        SOFT_GREEN.wrap(BOLD.wrap("Successful Purchase!")),
        GRAY.wrap("You bought " + SOFT_GREEN.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + SOFT_GREEN.wrap(GENERIC_PRICE)),
        Sound.ENTITY_EXPERIENCE_ORB_PICKUP
    );

    public static final MessageLocale SELL_MENU_NOTHING_RESULT = LangEntry.builder("VirtualShop.SellMenu.NothingResult").titleMessage(
        SOFT_RED.wrap(BOLD.wrap("Nothing to Sell!")),
        GRAY.wrap("You don't have items to sell."),
        Sound.BLOCK_ANVIL_PLACE
    );

    public static final MessageLocale SELL_MENU_NOTHING_DETAILS = LangEntry.builder("VirtualShop.SellMenu.NothingDetails").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        SOFT_RED.wrap(BOLD.wrap("Sale Details:")),
        GRAY.wrap("There are no items in your inventory that can be sold."),
        " ",
        GRAY.wrap("If you have stacked Shulker Boxes or Chests with items inside,"),
        GRAY.wrap("split them by single stacks to sell."),
        " "
    );
    
    public static final MessageLocale SELL_MENU_SALE_RESULT = LangEntry.builder("VirtualShop.SellMenu.SaleResult").titleMessage(
        SOFT_GREEN.wrap(BOLD.wrap("Items Sold!")),
        GRAY.wrap("+" + GENERIC_TOTAL),
        Sound.ENTITY_EXPERIENCE_ORB_PICKUP
    );

    public static final MessageLocale SELL_MENU_SALE_DETAILS = LangEntry.builder("VirtualShop.SellMenu.SaleDetails").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        SOFT_GREEN.wrap(BOLD.wrap("Sale Details:")),
        GENERIC_ENTRY,
        " ",
        GRAY.wrap("Total: " + SOFT_GREEN.wrap(GENERIC_TOTAL)),
        " "
    );

    public static final TextLocale SELL_MENU_SALE_ENTRY = LangEntry.builder("VirtualShop.SellMenu.SaleEntry").text(
        GRAY.wrap("x" + GENERIC_AMOUNT + " " + WHITE.wrap(GENERIC_ITEM) + ": " + SOFT_GREEN.wrap(GENERIC_PRICE) + " " + DARK_GRAY.wrap("(" + WHITE.wrap(SHOP_NAME) + ")")));

    public static final TextLocale PRICE_TRENDING_UP   = LangEntry.builder("VirtualShop.Price.AverageDynamics.Up").text(GREEN.wrap("↑ " + GENERIC_VALUE + "%"));
    public static final TextLocale PRICE_TRENDING_DOWN = LangEntry.builder("VirtualShop.Price.AverageDynamics.Down").text(RED.wrap("↓ " + GENERIC_VALUE + "%"));

    public static final MessageLocale ERROR_COMMAND_INVALID_SHOP_ARGUMENT = builder("Shop.Error.Invalid").chatMessage(
        GRAY.wrap(SOFT_RED.wrap(GENERIC_VALUE) + " is not a valid shop!")
    );

    public static final MessageLocale ERROR_EDITOR_ROTATION_EXISTS = builder("Error.Editor.RotationExists").chatMessage(
        SOFT_RED.wrap("Rotation with that name already exists!")
    );


    public static final ButtonLocale DIALOG_BUTTON_APPLY = builder("Dialog.Generic.Button.Apply").button(GREEN.wrap("✔") + " " + WHITE.wrap("Apply"));
    public static final ButtonLocale DIALOG_BUTTON_RESET = builder("Dialog.Generic.Button.Reset").button(RED.wrap("✘") + " " + WHITE.wrap("Reset"));
    public static final ButtonLocale DIALOG_BUTTON_BACK  = builder("Dialog.Generic.Button.Back").button(SOFT_YELLOW.wrap("←") + " " + WHITE.wrap("Back"));


    public static final TextLocale EDITOR_TITLE_SHOP_LIST               = LangEntry.builder("Editor.Title.Shop.List").text(BLACK.wrap("Virtual Shop Editor"));
    public static final TextLocale EDITOR_TITLE_SHOP_SETTINGS           = LangEntry.builder("Editor.Title.Shop.Settings").text(BLACK.wrap("Shop Settings"));
    public static final TextLocale EDITOR_TITLE_SHOP_LAYOUTS            = LangEntry.builder("Editor.Title.Shop.Layouts").text(BLACK.wrap("Shop Layouts"));
    public static final TextLocale EDITOR_TITLE_PRODUCT_CREATION        = LangEntry.builder("Editor.Title.Product.Creation").text(BLACK.wrap("Product Creation"));
    public static final TextLocale EDITOR_TITLE_PRODUCTS_NORMAL         = LangEntry.builder("Editor.Title.Products.Normal").text(BLACK.wrap("Shop Products (Normal)"));
    public static final TextLocale EDITOR_TITLE_PRODUCTS_ROTATING       = LangEntry.builder("Editor.Title.Products.Rotating").text(BLACK.wrap("Shop Products (Rotating)"));
    public static final TextLocale EDITOR_TITLE_PRODUCT_OPTIONS         = LangEntry.builder("Editor.Title.Product.Settings").text(BLACK.wrap("Product Options"));
    public static final TextLocale EDITOR_TITLE_PRODUCT_STOCKS          = LangEntry.builder("Editor.Title.Product.Stocks").text(BLACK.wrap("Product Stock Options"));
    public static final TextLocale EDITOR_TITLE_PRODUCT_PRICE           = LangEntry.builder("Editor.Title.Product.Price").text(BLACK.wrap("Product Price Options"));
    public static final TextLocale EDITOR_TITLE_SHOP_ROTATIONS          = LangEntry.builder("Editor.Title.Shop.Rotations").text(BLACK.wrap("Shop Rotations"));
    public static final TextLocale EDITOR_TITLE_ROTATION_OPTIONS        = LangEntry.builder("Editor.Title.Shop.Rotation.Options").text(BLACK.wrap("Rotation Options"));
    public static final TextLocale EDITOR_TITLE_ROTATION_TIMES          = LangEntry.builder("Editor.Title.Shop.Rotation.Times").text(BLACK.wrap("Rotation Times"));
    public static final TextLocale EDITOR_TITLE_ROTATION_ITEMS          = LangEntry.builder("Editor.Title.Shop.Rotation.Items").text(BLACK.wrap("Rotation Items"));
    public static final TextLocale EDITOR_TITLE_ROTATION_ITEM_SELECTION = LangEntry.builder("Editor.Title.Shop.Rotation.ItemSelection").text(BLACK.wrap("Select Product..."));
    public static final TextLocale EDITOR_TITLE_ROTATION_SLOT_SELECTION = LangEntry.builder("Editor.Title.Shop.Rotation.SlotSelection").text(BLACK.wrap("Select Slot(s)..."));

    public static final TextLocale EDITOR_PRODUCT_NO_RANK_REQUIREMENTS = LangEntry.builder("VirtualShop.Editor.Product.NoRankRequirements").text("No rank(s) required!");
    public static final TextLocale EDITOR_PRODUCT_NO_PERM_REQUIREMENTS = LangEntry.builder("VirtualShop.Editor.Product.NoPermissionRequirements").text("No permission(s) required!");
    public static final TextLocale EDITOR_PRODUCT_NO_FORBIDDEN_PERMS   = LangEntry.builder("VirtualShop.Editor.Product.NoForbiddenPerms").text("No permission(s) forbidden!");


    @Deprecated public static final TextLocale EDITOR_ENTER_SHOP_ID     = LangEntry.builder("VirtualShop.Editor.Enter.Id").text(GRAY.wrap("Enter " + GREEN.wrap("[Shop Identifier]")));
    @Deprecated public static final TextLocale EDITOR_ENTER_DESCRIPTION = LangEntry.builder("VirtualShop.Editor.Enter.Description").text(GRAY.wrap("Enter " + GREEN.wrap("[Description]")));
    @Deprecated public static final TextLocale EDITOR_ENTER_TITLE       = LangEntry.builder("VirtualShop.Editor.Enter.Title").text(GRAY.wrap("Enter " + GREEN.wrap("[Title]")));
    @Deprecated public static final TextLocale EDITOR_ENTER_COMMAND     = LangEntry.builder("VirtualShop.Editor.Enter.Command").text(GRAY.wrap("Enter " + GREEN.wrap("[Command]")));
    @Deprecated public static final TextLocale EDITOR_ENTER_ALIAS       = LangEntry.builder("VirtualShop.Editor.Enter.Alias").text(GRAY.wrap("Enter " + GREEN.wrap("[Command Alias]")));
    @Deprecated public static final TextLocale EDITOR_ENTER_SLOTS       = LangEntry.builder("VirtualShop.Editor.Enter.Slots").text(GRAY.wrap("Enter " + GREEN.wrap("[Slots] -> [1,2,5,etc]")));
    @Deprecated  public static final TextLocale EDITOR_ENTER_RANK        = LangEntry.builder("VirtualShop.Editor.Enter.Rank").text(GRAY.wrap("Enter " + GREEN.wrap("[Rank Name]")));
    @Deprecated  public static final TextLocale EDITOR_ENTER_PERMISSION  = LangEntry.builder("VirtualShop.Editor.Enter.Permission").text(GRAY.wrap("Enter " + GREEN.wrap("[Permission Node]")));
    @Deprecated public static final TextLocale EDITOR_ENTER_ROTATION_ID = LangEntry.builder("VirtualShop.Editor.Enter.RotationId").text(GRAY.wrap("Enter " + GREEN.wrap("[Rotation Name]")));
    @Deprecated public static final TextLocale EDITOR_ENTER_WEIGHT      = LangEntry.builder("VirtualShop.Editor.Enter.Weight").text(GRAY.wrap("Enter " + GREEN.wrap("[Weight]")));

    public static final IconLocale EDITOR_PRODUCT_COMMANDS = iconBuilder("Editor.Product.Commands")
        .name("Commands")
        .rawLore(PRODUCT_COMMANDS).br()
        .appendInfo("Commands to run when a player buys this item.").br()
        .appendClick("Click to edit")
        .build();
}
