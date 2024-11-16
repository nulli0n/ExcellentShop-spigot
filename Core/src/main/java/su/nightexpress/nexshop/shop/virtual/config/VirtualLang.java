package su.nightexpress.nexshop.shop.virtual.config;

import org.bukkit.Sound;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class VirtualLang extends Lang {

    public static final LangEnum<ShopType> SHOP_TYPES = LangEnum.of("VirtualShop.ShopType", ShopType.class);

    public static final LangString COMMAND_ARGUMENT_NAME_SHOP = LangString.of("VirtualShop.Command.Argument.Name.Shop", "shop");

    public static final LangString COMMAND_EDITOR_DESC        = LangString.of("VirtualShop.Command.Editor.Desc", "Open VirtualShop editor.");
    public static final LangString COMMAND_OPEN_DESC          = LangString.of("VirtualShop.Command.Open.Desc", "Opens specified shop.");
    public static final LangString COMMAND_MENU_DESC          = LangString.of("VirtualShop.Command.Menu.Desc", "Opens Main Menu.");
    public static final LangString COMMAND_SHOP_DESC          = LangString.of("VirtualShop.Command.Shop.Desc", "Open specified shop or main menu.");
    public static final LangString COMMAND_SELL_MENU_DESC     = LangString.of("VirtualShop.Command.SellMenu.Desc", "Open Sell GUI.");
    public static final LangString COMMAND_SELL_ALL_DESC      = LangString.of("VirtualShop.Command.SellAll.Desc", "Quickly sell all items in inventory.");
    public static final LangString COMMAND_SELL_HAND_DESC     = LangString.of("VirtualShop.Command.SellHand.Desc", "Quickly sell hand item.");
    public static final LangString COMMAND_SELL_HAND_ALL_DESC = LangString.of("VirtualShop.Command.SellHandAll.Desc", "Quickly sell similar hand items.");

    public static final LangText COMMAND_OPEN_DONE_OTHERS = LangText.of("VirtualShop.Command.Open.Done.Others",
        LIGHT_GRAY.enclose("Opened " + LIGHT_YELLOW.enclose(SHOP_NAME) + " shop for " + LIGHT_YELLOW.enclose(PLAYER_NAME) + ".")
    );

    public static final LangText COMMAND_MENU_DONE_OTHERS = LangText.of("VirtualShop.Command.Menu.Done.Others",
        LIGHT_GRAY.enclose("Opened shops menu for " + LIGHT_YELLOW.enclose(PLAYER_NAME) + ".")
    );

    public static final LangText COMMAND_SELL_MENU_DONE_OTHERS = LangText.of("VirtualShop.Command.SellMenu.Done.Others",
        LIGHT_GRAY.enclose("Opened sell menu for " + LIGHT_YELLOW.enclose(PLAYER_NAME) + ".")
    );

    public static final LangText COMMAND_SELL_ALL_DONE_OTHERS = LangText.of("VirtualShop.Command.SellAll.Done.Others",
        LIGHT_GRAY.enclose("Forced player " + LIGHT_YELLOW.enclose(PLAYER_NAME) + " to sell all items.")
    );

    public static final LangText COMMAND_SELL_HAND_DONE_OTHERS = LangText.of("VirtualShop.Command.SellHand.Done.Others",
        LIGHT_GRAY.enclose("Forced player " + LIGHT_YELLOW.enclose(PLAYER_NAME) + " to sell hand item.")
    );


    public static final LangText SHOP_ROTATION_NOTIFY = LangText.of("VirtualShop.Shop.Rotation.Notify",
        TAG_NO_PREFIX,
        " ",
        LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose(GENERIC_AMOUNT) + " new items just appeared in the " + LIGHT_YELLOW.enclose(SHOP_NAME) + " shop!"),
        LIGHT_GRAY.enclose("Click " +
            CLICK.encloseRun(
                HOVER.encloseHint(LIGHT_YELLOW.enclose(BOLD.enclose("HERE")), LIGHT_GRAY.enclose("Click to open shop!")),
                "/shop " + SHOP_ID
            )
            + " to open the shop!"),
        " "
    );

    public static final LangText SHOP_ERROR_BAD_WORLD = LangText.of("VirtualShop.Shop.Error.BadWorld",
        LIGHT_RED.enclose("You can't use shop in this world!"));

    public static final LangText SHOP_ERROR_BAD_GAMEMODE = LangText.of("VirtualShop.Shop.Error.BadGamemode",
        LIGHT_RED.enclose("You can't use shop in current gamemode!"));

    public static final LangText SHOP_ERROR_INVALID_LAYOUT = LangText.of("VirtualShop.Shop.Error.InvalidLayout",
        LIGHT_GRAY.enclose("Could not open shop " + LIGHT_RED.enclose(SHOP_NAME) + ": Invalid shop layout!"));





    public static final LangText PRODUCT_PURCHASE_SELL = LangText.of("VirtualShop.Product.Purchase.Sell",
        OUTPUT.enclose(15, 60) + SOUND.enclose(Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
        LIGHT_GREEN.enclose(BOLD.enclose("Successful Sale!")),
        LIGHT_GRAY.enclose("You sold " + LIGHT_GREEN.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + LIGHT_GREEN.enclose(GENERIC_PRICE))
    );

    public static final LangText PRODUCT_PURCHASE_BUY = LangText.of("VirtualShop.Product.Purchase.Buy",
        OUTPUT.enclose(15, 60) + SOUND.enclose(Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
        LIGHT_GREEN.enclose(BOLD.enclose("Successful Purchase!")),
        LIGHT_GRAY.enclose("You bought " + LIGHT_GREEN.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_ITEM) + " for " + LIGHT_GREEN.enclose(GENERIC_PRICE))
    );


    public static final LangText SELL_MENU_SALE_RESULT = LangText.of("VirtualShop.SellMenu.SaleResult",
        OUTPUT.enclose(15, 60) + SOUND.enclose(Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
        LIGHT_GREEN.enclose(BOLD.enclose("Items Sold!")),
        LIGHT_GRAY.enclose("+" + GENERIC_TOTAL)
    );

    public static final LangText SELL_MENU_SALE_DETAILS = LangText.of("VirtualShop.SellMenu.SaleDetails",
        TAG_NO_PREFIX,
        " ",
        LIGHT_GREEN.enclose(BOLD.enclose("Sale Details:")),
        GENERIC_ENTRY,
        " ",
        LIGHT_GRAY.enclose("Total: " + LIGHT_GREEN.enclose(GENERIC_TOTAL)),
        " "
    );

    public static final LangString SELL_MENU_SALE_ENTRY = LangString.of("VirtualShop.SellMenu.SaleEntry",
        LIGHT_GRAY.enclose("x" + GENERIC_AMOUNT + " " + WHITE.enclose(GENERIC_ITEM) + ": " + LIGHT_GREEN.enclose(GENERIC_PRICE)));


    public static final LangString PRICE_AVG_DIFF_UP = LangString.of("VirtualShop.Price.AverageDynamics.Up",
        GREEN.enclose("↑ " + GENERIC_VALUE + "%")
    );

    public static final LangString PRICE_AVG_DIFF_DOWN = LangString.of("VirtualShop.Price.AverageDynamics.Down",
        RED.enclose("↓ " + GENERIC_VALUE + "%")
    );


    public static final LangText ERROR_COMMAND_INVALID_SHOP_ARGUMENT = LangText.of("VirtualShop.Shop.Error.Invalid",
        LIGHT_GRAY.enclose(LIGHT_RED.enclose(GENERIC_VALUE) + " is not a valid shop!")
    );


    public static final LangString EDITOR_TITLE_SHOP_LIST = LangString.of("Editor.Title.Shop.List",
        BLACK.enclose("Virtual Shop Editor")
    );

    public static final LangString EDITOR_TITLE_SHOP_SETTINGS = LangString.of("Editor.Title.Shop.Settings",
        BLACK.enclose("Shop Settings: " + SHOP_NAME)
    );


    public static final LangString EDITOR_PRODUCT_NO_RANK_REQUIREMENTS = LangString.of("VirtualShop.Editor.Product.NoRankRequirements",
        "No ranks required!");

    public static final LangString EDITOR_PRODUCT_NO_PERM_REQUIREMENTS = LangString.of("VirtualShop.Editor.Product.NoPermissionRequirements",
        "No permissions required!");

    public static final LangString EDITOR_SHOP_CREATE_ERROR_EXIST = LangString.of("VirtualShop.Editor.Create.Error.Exist",
        LIGHT_RED.enclose("Shop already exists!"));

    public static final LangString EDITOR_ENTER_SHOP_ID = LangString.of("VirtualShop.Editor.Enter.Id",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Shop Identifier]")));

    public static final LangString EDITOR_ENTER_DESCRIPTION = LangString.of("VirtualShop.Editor.Enter.Description",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Description]")));

    public static final LangString EDITOR_ENTER_NPC_ID = LangString.of("VirtualShop.Editor.Enter.NpcId",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[NPC ID]")));

    public static final LangString EDITOR_ENTER_TITLE = LangString.of("VirtualShop.Editor.Enter.Title",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Title]")));

    public static final LangString EDITOR_ENTER_COMMAND = LangString.of("VirtualShop.Editor.Enter.Command",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Command]")));

    public static final LangString EDITOR_ENTER_SLOTS = LangString.of("VirtualShop.Editor.Enter.Slots",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Slots] -> [1,2,5,etc]")));

    public static final LangString EDITOR_ENTER_RANK = LangString.of("VirtualShop.Editor.Enter.Rank",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Rank Name]")));

    public static final LangString EDITOR_ENTER_PERMISSION = LangString.of("VirtualShop.Editor.Enter.Permission",
        LIGHT_GRAY.enclose("Enter " + LIGHT_GREEN.enclose("[Permission Node]")));
}
