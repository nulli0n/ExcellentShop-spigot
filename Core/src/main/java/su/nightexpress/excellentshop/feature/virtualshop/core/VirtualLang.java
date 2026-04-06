package su.nightexpress.excellentshop.feature.virtualshop.core;

import org.bukkit.Sound;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.feature.virtualshop.command.VirtualCommands;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.locale.message.MessageData;

import static su.nightexpress.excellentshop.ShopPlaceholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class VirtualLang implements LangContainer {

    private static final String PREFIX = "VirtualShop.";

    public static LangEntry.@NonNull Builder builder(@NonNull String key) {
        return LangEntry.builder(PREFIX + key);
    }

    public static IconLocale.@NonNull Builder iconBuilder(@NonNull String key) {
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


    public static final MessageLocale SHOP_ROTATION_NOTIFY = LangEntry.builder("VirtualShop.Shop.Rotation.Update").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        GRAY.wrap("New items just appeared in the " + SOFT_YELLOW.wrap(SHOP_NAME) + " shop!"),
        GRAY.wrap("Click " + RUN_COMMAND.with("/" + VirtualCommands.DEF_SHOP + " " + SHOP_ID).and(SHOW_TEXT.with(GRAY.wrap("Click to open shop!"))).wrap(SOFT_YELLOW.wrap(BOLD.wrap("HERE"))) + " to open the shop!"),
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


    public static final MessageLocale PRODUCT_PURCHASE_BUY_SINGLE = LangEntry.builder("VirtualShop.Product.Purchase.BuySingle").chatMessage(
        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
        GRAY.wrap("You bought " + WHITE.wrap(GENERIC_PRODUCTS) + " for " + GREEN.wrap(GENERIC_WORTH))
    );

    public static final MessageLocale PRODUCT_PURCHASE_BUY_MULTIPLE = LangEntry.builder("VirtualShop.Product.Purchase.BuyMultiple").chatMessage(
        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
        GRAY.wrap("You bought " + SHOW_TEXT.with(GENERIC_PRODUCTS).wrap(GREEN.and(UNDERLINED).wrap(GENERIC_SIZE + " items")) + " " + GRAY.wrap("(hover for info)") + " for " + GREEN.wrap(GENERIC_WORTH) + ".")
    );

    public static final MessageLocale PRODUCT_PURCHASE_SELL_SINGLE = LangEntry.builder("VirtualShop.Product.Purchase.SellSingle").chatMessage(
        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
        GRAY.wrap("You sold " + WHITE.wrap(GENERIC_PRODUCTS) + " for " + GREEN.wrap(GENERIC_WORTH))
    );

    public static final MessageLocale PRODUCT_PURCHASE_SELL_MULTIPLE = LangEntry.builder("VirtualShop.Product.Purchase.SellMultiple").chatMessage(
        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
        GRAY.wrap("You sold " + SHOW_TEXT.with(GENERIC_PRODUCTS).wrap(GREEN.and(UNDERLINED).wrap(GENERIC_SIZE + " items")) + " " + GRAY.wrap("(hover for info)") + " for " + GREEN.wrap(GENERIC_WORTH) + ".")
    );

    public static final MessageLocale ERROR_COMMAND_INVALID_SHOP_ARGUMENT = builder("Command.Syntax.InvalidShop").chatMessage(
        GRAY.wrap(SOFT_RED.wrap(GENERIC_INPUT) + " is not a valid shop!")
    );

    public static final MessageLocale ERROR_EDITOR_ROTATION_EXISTS = builder("Error.Editor.RotationExists").chatMessage(
        SOFT_RED.wrap("Rotation with that name already exists!")
    );

    public static final IconLocale UI_EDITOR_SHOP_ROTATION_SELECTED_SLOT = iconBuilder("UI.Editor.Rotation.Slots.SelectedSlot")
        .accentColor(SOFT_AQUA)
        .name("Selected Slot")
        .appendClick("Click to unselect")
        .build();

    public static final IconLocale UI_EDITOR_SHOP_ROTATION_OTHER_SLOT = iconBuilder("UI.Editor.Rotation.Slots.OtherSlot")
        .name("Other Rotation's Slot", RED)
        .appendInfo("This slot is used by", "other rotation.")
        .build();

    public static final IconLocale UI_EDITOR_SHOP_PRODUCTS_OBJECT = iconBuilder("UI.Editor.Products.Object")
        .name(PRODUCT_PREVIEW_NAME)
        .appendCurrent("Handler", PRODUCT_HANDLER)
        .appendCurrent("Price Type", PRODUCT_PRICE_TYPE)
        .appendCurrent("Buy Price", PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY))
        .appendCurrent("Sell Price", PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL))
        .br()
        .appendInfo("You can freely move this product", "between slots, pages, and shops!")
        .br()
        .appendClick("Left-Click to edit")
        .appendClick("Right-Click to pick")
        .build();

    public static final TextLocale EDITOR_TITLE_SHOP_LIST               = builder("Editor.Title.Shop.List").text(BLACK.wrap("Virtual Shop Editor"));
    public static final TextLocale EDITOR_TITLE_SHOP_SETTINGS           = builder("Editor.Title.Shop.Settings").text(BLACK.wrap("Shop Settings"));
    public static final TextLocale EDITOR_TITLE_PRODUCTS_ROTATING       = builder("Editor.Title.Products.Rotating").text(BLACK.wrap("Rotating Products"));
    public static final TextLocale EDITOR_TITLE_PRODUCT_OPTIONS         = builder("Editor.Title.Product.Settings").text(BLACK.wrap("Product Options"));
    public static final TextLocale EDITOR_TITLE_SHOP_ROTATIONS          = builder("Editor.Title.Shop.Rotations").text(BLACK.wrap("Shop Rotations"));
    public static final TextLocale EDITOR_TITLE_ROTATION_OPTIONS        = builder("Editor.Title.Shop.Rotation.Options").text(BLACK.wrap("Rotation Options"));
    public static final TextLocale EDITOR_TITLE_ROTATION_TIMES          = builder("Editor.Title.Shop.Rotation.Times").text(BLACK.wrap("Rotation Times"));
    public static final TextLocale EDITOR_TITLE_ROTATION_ITEMS          = builder("Editor.Title.Shop.Rotation.Items").text(BLACK.wrap("Rotation Items"));
    public static final TextLocale EDITOR_TITLE_ROTATION_ITEM_SELECTION = builder("Editor.Title.Shop.Rotation.ItemSelection").text(BLACK.wrap("Select Product..."));

    @Deprecated
    public static final TextLocale EDITOR_ENTER_ROTATION_ID = LangEntry.builder("VirtualShop.Editor.Enter.RotationId").text(GRAY.wrap("Enter " + GREEN.wrap("[Rotation Name]")));
    @Deprecated
    public static final TextLocale EDITOR_ENTER_WEIGHT      = LangEntry.builder("VirtualShop.Editor.Enter.Weight").text(GRAY.wrap("Enter " + GREEN.wrap("[Weight]")));


}
