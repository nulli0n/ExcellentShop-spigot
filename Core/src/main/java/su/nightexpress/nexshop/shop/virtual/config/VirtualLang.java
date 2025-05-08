package su.nightexpress.nexshop.shop.virtual.config;

import org.bukkit.Sound;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class VirtualLang extends Lang {

    public static final LangString COMMAND_ARGUMENT_NAME_SHOP = LangString.of("VirtualShop.Command.Argument.Name.Shop", "shop");

    public static final LangString COMMAND_EDITOR_DESC        = LangString.of("VirtualShop.Command.Editor.Desc", "Open VirtualShop editor.");
    public static final LangString COMMAND_ROTATE_DESC        = LangString.of("VirtualShop.Command.Rotate.Desc", "Force rotate a shop.");
    public static final LangString COMMAND_OPEN_DESC          = LangString.of("VirtualShop.Command.Open.Desc", "Opens specified shop.");
    public static final LangString COMMAND_MENU_DESC          = LangString.of("VirtualShop.Command.Menu.Desc", "Opens Main Menu.");
    public static final LangString COMMAND_SHOP_DESC          = LangString.of("VirtualShop.Command.Shop.Desc", "Open specified shop or main menu.");
    public static final LangString COMMAND_SHOP_ALIAS_DESC    = LangString.of("VirtualShop.Command.ShopAlias.Desc", "Open " + SHOP_NAME + " shop.");
    public static final LangString COMMAND_SELL_MENU_DESC     = LangString.of("VirtualShop.Command.SellMenu.Desc", "Open Sell GUI.");
    public static final LangString COMMAND_SELL_ALL_DESC      = LangString.of("VirtualShop.Command.SellAll.Desc", "Quickly sell all items in inventory.");
    public static final LangString COMMAND_SELL_HAND_DESC     = LangString.of("VirtualShop.Command.SellHand.Desc", "Quickly sell hand item.");
    public static final LangString COMMAND_SELL_HAND_ALL_DESC = LangString.of("VirtualShop.Command.SellHandAll.Desc", "Quickly sell similar hand items.");

    public static final LangText COMMAND_OPEN_DONE_OTHERS = LangText.of("VirtualShop.Command.Open.Done.Others",
        LIGHT_GRAY.wrap("Opened " + LIGHT_YELLOW.wrap(SHOP_NAME) + " shop for " + LIGHT_YELLOW.wrap(PLAYER_NAME) + ".")
    );

    public static final LangText COMMAND_MENU_DONE_OTHERS = LangText.of("VirtualShop.Command.Menu.Done.Others",
        LIGHT_GRAY.wrap("Opened shops menu for " + LIGHT_YELLOW.wrap(PLAYER_NAME) + ".")
    );

    public static final LangText COMMAND_SELL_MENU_DONE_OTHERS = LangText.of("VirtualShop.Command.SellMenu.Done.Others",
        LIGHT_GRAY.wrap("Opened sell menu for " + LIGHT_YELLOW.wrap(PLAYER_NAME) + ".")
    );

    public static final LangText COMMAND_SELL_ALL_DONE_OTHERS = LangText.of("VirtualShop.Command.SellAll.Done.Others",
        LIGHT_GRAY.wrap("Forced player " + LIGHT_YELLOW.wrap(PLAYER_NAME) + " to sell all items.")
    );

    public static final LangText COMMAND_SELL_HAND_DONE_OTHERS = LangText.of("VirtualShop.Command.SellHand.Done.Others",
        LIGHT_GRAY.wrap("Forced player " + LIGHT_YELLOW.wrap(PLAYER_NAME) + " to sell hand item.")
    );

    public static final LangText COMMAND_ROTATE_DONE = LangText.of("VirtualShop.Command.Rotate.Done",
        LIGHT_GRAY.wrap("Force rotated " + LIGHT_YELLOW.wrap(SHOP_NAME) + " shop")
    );


    public static final LangText SHOP_ROTATION_NOTIFY = LangText.of("VirtualShop.Shop.Rotation.Update",
        TAG_NO_PREFIX,
        " ",
        LIGHT_GRAY.wrap("New items just appeared in the " + LIGHT_YELLOW.wrap(SHOP_NAME) + " shop!"),
        LIGHT_GRAY.wrap("Click " +
            CLICK.wrapRunCommand(
                HOVER.wrapShowText(LIGHT_YELLOW.wrap(BOLD.wrap("HERE")), LIGHT_GRAY.wrap("Click to open shop!")),
                "/shop " + SHOP_ID
            )
            + " to open the shop!"), // TODO Default command to const
        " "
    );

    public static final LangText SHOP_ERROR_BAD_WORLD = LangText.of("VirtualShop.Shop.Error.BadWorld",
        LIGHT_RED.wrap("You can't use shop in this world!"));

    public static final LangText SHOP_ERROR_BAD_GAMEMODE = LangText.of("VirtualShop.Shop.Error.BadGamemode",
        LIGHT_RED.wrap("You can't use shop in current gamemode!"));

    public static final LangText SHOP_ERROR_INVALID_LAYOUT = LangText.of("VirtualShop.Shop.Error.InvalidLayout",
        LIGHT_GRAY.wrap("Could not open shop " + LIGHT_RED.wrap(SHOP_NAME) + ": Invalid shop layout!"));

    public static final LangText SHOP_CREATE_ERROR_EXIST = LangText.of("VirtualShop.Shop.Create.Error.Exist",
        LIGHT_RED.wrap("Shop with such name already exists!"));

    public static final LangText SHOP_CREATE_ERROR_BAD_NAME = LangText.of("VirtualShop.Shop.Create.Error.BadName",
        LIGHT_RED.wrap("Only latin letters and numbers are allowed!"));




    public static final LangText PRODUCT_PURCHASE_SELL = LangText.of("VirtualShop.Product.Purchase.Sell",
        OUTPUT.wrap(15, 60) + SOUND.wrap(Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
        LIGHT_GREEN.wrap(BOLD.wrap("Successful Sale!")),
        LIGHT_GRAY.wrap("You sold " + LIGHT_GREEN.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + LIGHT_GREEN.wrap(GENERIC_PRICE))
    );

    public static final LangText PRODUCT_PURCHASE_BUY = LangText.of("VirtualShop.Product.Purchase.Buy",
        OUTPUT.wrap(15, 60) + SOUND.wrap(Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
        LIGHT_GREEN.wrap(BOLD.wrap("Successful Purchase!")),
        LIGHT_GRAY.wrap("You bought " + LIGHT_GREEN.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + LIGHT_GREEN.wrap(GENERIC_PRICE))
    );


    public static final LangText SELL_MENU_NOTHING_RESULT = LangText.of("VirtualShop.SellMenu.NothingResult",
        OUTPUT.wrap(15, 60) + SOUND.wrap(Sound.BLOCK_ANVIL_PLACE),
        LIGHT_RED.wrap(BOLD.wrap("Nothing to Sell!")),
        LIGHT_GRAY.wrap("You don't have items to sell.")
    );

    public static final LangText SELL_MENU_NOTHING_DETAILS = LangText.of("VirtualShop.SellMenu.NothingDetails",
        TAG_NO_PREFIX,
        " ",
        LIGHT_RED.wrap(BOLD.wrap("Sale Details:")),
        LIGHT_GRAY.wrap("There are no items in your inventory that can be sold."),
        " ",
        LIGHT_GRAY.wrap("If you have stacked Shulker Boxes or Chests with items inside,"),
        LIGHT_GRAY.wrap("split them by single stacks to sell."),
        " "
    );
    
    public static final LangText SELL_MENU_SALE_RESULT = LangText.of("VirtualShop.SellMenu.SaleResult",
        OUTPUT.wrap(15, 60) + SOUND.wrap(Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
        LIGHT_GREEN.wrap(BOLD.wrap("Items Sold!")),
        LIGHT_GRAY.wrap("+" + GENERIC_TOTAL)
    );

    public static final LangText SELL_MENU_SALE_DETAILS = LangText.of("VirtualShop.SellMenu.SaleDetails",
        TAG_NO_PREFIX,
        " ",
        LIGHT_GREEN.wrap(BOLD.wrap("Sale Details:")),
        GENERIC_ENTRY,
        " ",
        LIGHT_GRAY.wrap("Total: " + LIGHT_GREEN.wrap(GENERIC_TOTAL)),
        " "
    );

    public static final LangString SELL_MENU_SALE_ENTRY = LangString.of("VirtualShop.SellMenu.SaleEntry",
        LIGHT_GRAY.wrap("x" + GENERIC_AMOUNT + " " + WHITE.wrap(GENERIC_ITEM) + ": " + LIGHT_GREEN.wrap(GENERIC_PRICE) + " " + DARK_GRAY.wrap("(" + WHITE.wrap(SHOP_NAME) + ")")));


    public static final LangString PRICE_AVG_DIFF_UP = LangString.of("VirtualShop.Price.AverageDynamics.Up",
        GREEN.wrap("↑ " + GENERIC_VALUE + "%")
    );

    public static final LangString PRICE_AVG_DIFF_DOWN = LangString.of("VirtualShop.Price.AverageDynamics.Down",
        RED.wrap("↓ " + GENERIC_VALUE + "%")
    );


    public static final LangText ERROR_COMMAND_INVALID_SHOP_ARGUMENT = LangText.of("VirtualShop.Shop.Error.Invalid",
        LIGHT_GRAY.wrap(LIGHT_RED.wrap(GENERIC_VALUE) + " is not a valid shop!")
    );

    public static final LangText ERROR_EDITOR_ROTATION_EXISTS = LangText.of("VirtualShop.Error.Editor.RotationExists",
        LIGHT_RED.wrap("Rotation with that name already exists!")
    );


    public static final LangString EDITOR_TITLE_SHOP_LIST = LangString.of("Editor.Title.Shop.List",
        BLACK.wrap("Virtual Shop Editor"));

    public static final LangString EDITOR_TITLE_SHOP_SETTINGS = LangString.of("Editor.Title.Shop.Settings",
        BLACK.wrap("Shop Settings"));

    public static final LangString EDITOR_TITLE_SHOP_LAYOUTS = LangString.of("Editor.Title.Shop.Layouts",
        BLACK.wrap("Shop Layouts"));

    public static final LangString EDITOR_TITLE_PRODUCT_CREATION = LangString.of("Editor.Title.Product.Creation",
        BLACK.wrap("Product Creation"));

    public static final LangString EDITOR_TITLE_PRODUCTS_NORMAL = LangString.of("Editor.Title.Products.Normal",
        BLACK.wrap("Shop Products (Normal)"));

    public static final LangString EDITOR_TITLE_PRODUCTS_ROTATING = LangString.of("Editor.Title.Products.Rotating",
        BLACK.wrap("Shop Products (Rotating)"));

    public static final LangString EDITOR_TITLE_PRODUCT_OPTIONS = LangString.of("Editor.Title.Product.Settings",
        BLACK.wrap("Product Options"));

    public static final LangString EDITOR_TITLE_PRODUCT_STOCKS = LangString.of("Editor.Title.Product.Stocks",
        BLACK.wrap("Product Stock Options"));

    public static final LangString EDITOR_TITLE_PRODUCT_PRICE = LangString.of("Editor.Title.Product.Price",
        BLACK.wrap("Product Price Options"));

    public static final LangString EDITOR_TITLE_SHOP_ROTATIONS = LangString.of("Editor.Title.Shop.Rotations",
        BLACK.wrap("Shop Rotations"));

    public static final LangString EDITOR_TITLE_ROTATION_OPTIONS = LangString.of("Editor.Title.Shop.Rotation.Options",
        BLACK.wrap("Rotation Options"));

    public static final LangString EDITOR_TITLE_ROTATION_TIMES = LangString.of("Editor.Title.Shop.Rotation.Times",
        BLACK.wrap("Rotation Times"));

    public static final LangString EDITOR_TITLE_ROTATION_ITEMS = LangString.of("Editor.Title.Shop.Rotation.Items",
        BLACK.wrap("Rotation Items"));

    public static final LangString EDITOR_TITLE_ROTATION_ITEM_SELECTION = LangString.of("Editor.Title.Shop.Rotation.ItemSelection",
        BLACK.wrap("Select Product..."));

    public static final LangString EDITOR_TITLE_ROTATION_SLOT_SELECTION = LangString.of("Editor.Title.Shop.Rotation.SlotSelection",
        BLACK.wrap("Select Slot(s)..."));


    public static final LangString EDITOR_PRODUCT_NO_RANK_REQUIREMENTS = LangString.of("VirtualShop.Editor.Product.NoRankRequirements",
        "No rank(s) required!");

    public static final LangString EDITOR_PRODUCT_NO_PERM_REQUIREMENTS = LangString.of("VirtualShop.Editor.Product.NoPermissionRequirements",
        "No permission(s) required!");

    public static final LangString EDITOR_PRODUCT_NO_FORBIDDEN_PERMS = LangString.of("VirtualShop.Editor.Product.NoForbiddenPerms",
        "No permission(s) forbidden!");


    public static final LangString EDITOR_ENTER_SHOP_ID = LangString.of("VirtualShop.Editor.Enter.Id",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Shop Identifier]")));

    public static final LangString EDITOR_ENTER_DESCRIPTION = LangString.of("VirtualShop.Editor.Enter.Description",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Description]")));

    public static final LangString EDITOR_ENTER_TITLE = LangString.of("VirtualShop.Editor.Enter.Title",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Title]")));

    public static final LangString EDITOR_ENTER_COMMAND = LangString.of("VirtualShop.Editor.Enter.Command",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Command]")));

    public static final LangString EDITOR_ENTER_ALIAS = LangString.of("VirtualShop.Editor.Enter.Alias",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Command Alias]")));

    public static final LangString EDITOR_ENTER_SLOTS = LangString.of("VirtualShop.Editor.Enter.Slots",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Slots] -> [1,2,5,etc]")));

    public static final LangString EDITOR_ENTER_RANK = LangString.of("VirtualShop.Editor.Enter.Rank",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Rank Name]")));

    public static final LangString EDITOR_ENTER_PERMISSION = LangString.of("VirtualShop.Editor.Enter.Permission",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Permission Node]")));

    public static final LangString EDITOR_ENTER_ROTATION_ID = LangString.of("VirtualShop.Editor.Enter.RotationId",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Rotation Name]")));

    public static final LangString EDITOR_ENTER_WEIGHT = LangString.of("VirtualShop.Editor.Enter.Weight",
        LIGHT_GRAY.wrap("Enter " + LIGHT_GREEN.wrap("[Weight]")));
}
