package su.nightexpress.nexshop.shop.virtual.config;

import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nightcore.language.entry.LangItem;
import su.nightexpress.nightcore.language.entry.LangUIButton;
import su.nightexpress.nightcore.util.Plugins;

import static su.nightexpress.nightcore.language.entry.LangItem.builder;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.api.shop.type.TradeType.*;
import static su.nightexpress.nexshop.Placeholders.*;

public class VirtualLocales {

    private static final String PREFIX = "VirtualShop.Editor.";

    public static final LangItem SHOP_CREATE = LangItem.builder(PREFIX + "Shop.Create")
        .name("New Shop")
        .emptyLine()
        .click("create")
        .build();

    public static final LangItem SHOP_DELETE = LangItem.builder(PREFIX + "Shop.Delete")
        .name("Delete Shop")
        .text("Permanently deletes the shop", "with all settings and items.")
        .emptyLine()
        .click("delete")
        .build();

    public static final LangItem SHOP_OBJECT = LangItem.builder(PREFIX + "Shop.Object")
        .name(SHOP_NAME)
        .current("Items", SHOP_PRODUCTS)
        .current("Pages", VIRTUAL_SHOP_PAGES)
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem SHOP_EDIT_NAME = LangItem.builder(PREFIX + "Shop.DisplayName")
        .name("Display Name")
        .current("Current", SHOP_NAME)
        .emptyLine()
        .click("change")
        .build();

    public static final LangItem SHOP_EDIT_DESCRIPTION = LangItem.builder(PREFIX + "Shop.Description")
        .name("Description")
        .textRaw(VIRTUAL_SHOP_DESCRIPTION)
        .emptyLine()
        .leftClick("add line")
        .rightClick("remove all")
        .build();

    public static final LangItem SHOP_EDIT_ICON = LangItem.builder(PREFIX + "Shop.Icon")
        .name("Icon")
        .dragAndDrop("replace")
        .rightClick("get a copy")
        .build();

    public static final LangItem SHOP_EDIT_MENU_SLOT = LangItem.builder(PREFIX + "Shop.MenuSlot")
        .name("Menu Slot")
        .current("Current", VIRTUAL_SHOP_MENU_SLOT)
        .emptyLine()
        .text("Sets shop position for the " + LIGHT_YELLOW.wrap("/shop") + " GUI.")
        .emptyLine()
        .leftClick("change")
        .rightClick("disable")
        .build();

    public static final LangItem SHOP_EDIT_PERMISSION = LangItem.builder(PREFIX + "Shop.PermissionRequirement")
        .name("Permission Requirement")
        .current("Enabled", VIRTUAL_SHOP_PERMISSION_REQUIRED)
        .current("Node", VIRTUAL_SHOP_PERMISSION_NODE)
        .emptyLine()
        .text("Controls whether permission is required", "to use this shop.")
        .emptyLine()
        .click("toggle")
        .build();

    public static final LangItem SHOP_EDIT_PAGES = LangItem.builder(PREFIX + "Shop.Pages")
        .name("Pages Amount")
        .current("Current", VIRTUAL_SHOP_PAGES)
        .emptyLine()
        .text("Amount of pages in the shop.")
        .emptyLine()
        .text("Make sure that shop layout config")
        .text("contains page buttons.")
        .emptyLine()
        .leftClick("+1 page")
        .rightClick("-1 page")
        .build();

    public static final LangItem SHOP_EDIT_BUYING = LangItem.builder(PREFIX + "Shop.Buying")
        .name("Buying")
        .current("State", SHOP_BUYING_ALLOWED)
        .emptyLine()
        .text("Controls whether players can", "buy items in this shop.")
        .emptyLine()
        .click("toggle")
        .build();

    public static final LangItem SHOP_EDIT_SELLING = LangItem.builder(PREFIX + "Shop.Selling")
        .name("Selling")
        .current("State", SHOP_SELLING_ALLOWED)
        .emptyLine()
        .text("Controls whether players can", "sell items to this shop.")
        .emptyLine()
        .click("toggle")
        .build();

    public static final LangUIButton SHOP_EDIT_ALIASES = LangUIButton.builder(PREFIX + "Shop.Aliases", "Command Aliases")
        .current(VIRTUAL_SHOP_ALIASES)
        .description("Provides direct shop access with", "listed command aliases.", "", LIGHT_RED.wrap("Requires module reload."))
        .leftClick("add alias")
        .rightClick("remove all")
        .build();

    public static final LangItem SHOP_EDIT_LAYOUTS = LangItem.builder(PREFIX + "Shop.Layouts.Info")
        .name("Layouts")
        .text("Sets GUI layout for this shop.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem SHOP_EDIT_LAYOUT_BY_DEFAULT = LangItem.builder(PREFIX + "Shop.Layouts.ByDefault")
        .name("Default Layout")
        .current("Current", VIRTUAL_SHOP_DEFAULT_LAYOUT)
        .emptyLine()
        .text("Sets default (all pages) GUI", "layout for this shop.")
        .emptyLine()
        .click("change")
        .build();

    public static final LangItem SHOP_EDIT_LAYOUT_BY_PAGE = LangItem.builder(PREFIX + "Shop.Layouts.ByPage")
        .name("Page #" + GENERIC_PAGE + " Layout")
        .current("Current", GENERIC_NAME)
        .emptyLine()
        .leftClick("change")
        .rightClick("reset")
        .build();

    public static final LangItem SHOP_EDIT_DISCOUNTS = LangItem.builder(PREFIX + "Shop.Discounts")
        .name("Discounts")
        .text("Create and manage shop discounts here.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem SHOP_EDIT_PRODUCTS_NORMAL = LangItem.builder(PREFIX + "Shop.Products")
        .name("Normal Products")
        .text("Regular, static shop items.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem SHOP_EDIT_PRODUCTS_ROTATING = LangItem.builder(PREFIX + "Shop.RotatingProducts")
        .name("Rotating Products")
        .text("Items that will appear during", "shop rotations only.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem SHOP_EDIT_ROTATIONS = LangItem.builder(PREFIX + "Shop.Rotations")
        .name("Rotations")
        .text("Add dynamics to your shop", "with product rotations.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem SHOP_RESET_PRICE_DATA = LangItem.builder(PREFIX + "Shop.ResetPriceData")
        .name("Reset Prices & Update")
        .text("Resets price datas of all items", "and refreshes their prices.")
        .emptyLine()
        .click("reset")
        .build();

    public static final LangItem SHOP_RESET_STOCK_DATA = LangItem.builder(PREFIX + "Shop.ResetStockData")
        .name("Reset Stocks & Update")
        .text("Resets stock datas of all items", "and refreshes their stocks.")
        .emptyLine()
        .click("reset")
        .build();

    public static final LangItem SHOP_RESET_ROTATION_DATA = LangItem.builder(PREFIX + "Shop.ResetRotationData")
        .name("Reset Rotations & Update")
        .text("Resets rotation datas", "and performs new rotations.")
        .emptyLine()
        .click("reset")
        .build();



    public static final LangItem ROTATION_OBJECT = LangItem.builder(PREFIX + "Rotation.Object")
        .name(ROTATION_ID)
        .current("Slots Used", ROTATION_SLOTS_AMOUNT)
        .current("Items Amount", ROTATION_ITEMS_AMOUNT)
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem ROTATION_CREATE = LangItem.builder(PREFIX + "Rotation.Create")
        .name("New Rotation")
        .click("create")
        .build();

    public static final LangItem ROTATION_DELETE = LangItem.builder(PREFIX + "Rotation.Delete")
        .name("Delete Rotation")
        .text("Deletes rotation with all", "settings and data.")
        .emptyLine()
        .click("delete")
        .build();

    public static final LangItem ROTATION_RESET = LangItem.builder(PREFIX + "Rotation.Reset")
        .name("Reset & Update")
        .text("Resets rotation's data and", "performs a fresh rotation.")
        .emptyLine()
        .click("reset")
        .build();

    public static final LangItem ROTATION_EDIT_ICON = LangItem.builder(PREFIX + "Rotation.Icon")
        .name("Icon")
        .text("Sets rotation icon", "so you can distinguish it", "from others :)")
        .emptyLine()
        .dragAndDrop("replace")
        .rightClick("get a copy")
        .build();

    public static final LangItem ROTATION_EDIT_TYPE = LangItem.builder(PREFIX + "Rotation.Type")
        .name("Rotation Type")
        .current("Current", ROTATION_TYPE)
        .emptyLine()
        .text(LIGHT_YELLOW.wrap(BOLD.wrap("Interval:")))
        .text("Performs rotations every X seconds.")
        .emptyLine()
        .text(LIGHT_YELLOW.wrap(BOLD.wrap("Fixed:")))
        .text("Performs rotations at given times.")
        .emptyLine()
        .click("toggle")
        .build();

    public static final LangItem ROTATION_EDIT_INTERVAL = LangItem.builder(PREFIX + "Rotation.Interval")
        .name("Rotation Interval")
        .current("Current", ROTATION_INTERVAL)
        .emptyLine()
        .text("Sets rotation interval (in seconds).")
        .emptyLine()
        .click("change")
        .build();

    public static final LangItem ROTATION_EDIT_TIMES = LangItem.builder(PREFIX + "Rotation.Times")
        .name("Rotation Times")
        .text("Set rotation times.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem ROTATION_EDIT_SLOTS = LangItem.builder(PREFIX + "Rotation.Slots")
        .name("Used Slots")
        .text("This rotation currently", "uses " + LIGHT_YELLOW.wrap(ROTATION_SLOTS_AMOUNT) + " slot(s).")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem ROTATION_EDIT_PRODUCTS = LangItem.builder(PREFIX + "Rotation.Items")
        .name("Products")
        .text("This rotation currently", "contains " + LIGHT_YELLOW.wrap(ROTATION_ITEMS_AMOUNT) + " item(s).")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem ROTATION_DAY_TIME_OBJECT = LangItem.builder(PREFIX + "Rotation.DayTimes.Object")
        .name(GENERIC_NAME)
        .textRaw(GENERIC_TIME)
        .emptyLine()
        .leftClick("add")
        .rightClick("remove all")
        .build();

    public static final LangItem ROTATION_FREE_SLOT = LangItem.builder(PREFIX + "Rotation.FreeSlot")
        .name(GREEN.wrap(BOLD.wrap("Free Slot")))
        .text("This slot can be used", "for rotations.")
        .emptyLine()
        .click("select")
        .build();

    public static final LangItem ROTATION_SELECTED_SLOT = LangItem.builder(PREFIX + "Rotation.SelectedSlot")
        .name(CYAN.wrap(BOLD.wrap("Selected Slot")))
        .text("This slot is used", "for rotations.")
        .emptyLine()
        .click("unselect")
        .build();

    public static final LangItem ROTATION_OTHER_SLOT = LangItem.builder(PREFIX + "Rotation.OtherSlot")
        .name(RED.wrap(BOLD.wrap("Other Rotation's Slot")))
        .text("This slot is used by", "other rotation.")
        .build();

    public static final LangItem ROTATION_ITEM_OBJECT = LangItem.builder(PREFIX + "Rotation.Item.Object")
        .name(PRODUCT_PREVIEW_NAME + RESET.getBracketsName() + GRAY.wrap(" (ID: " + WHITE.wrap(PRODUCT_ID) + ")"))
        .current("Weight", GENERIC_WEIGHT)
        .emptyLine()
        .leftClick("set weight")
        .dropKey("remove")
        .build();

    public static final LangItem ROTATION_ITEM_CREATE = LangItem.builder(PREFIX + "Rotation.Item.Create")
        .name("New Item")
        .click("select")
        .build();



    public static final LangItem PRODUCT_OBJECT = LangItem.builder(PREFIX + "Product.Object.Static2")
        .name(PRODUCT_PREVIEW_NAME)
        .current("Handler", PRODUCT_HANDLER)
        .current("Currency", PRODUCT_CURRENCY)
        .current("Price Type", PRODUCT_PRICE_TYPE)
        .current("Buy", PRODUCT_PRICE.apply(BUY))
        .current("Sell", PRODUCT_PRICE.apply(SELL))
        .emptyLine()
        .text("You can freely move this product", "between slots, pages, and shops!")
        .emptyLine()
        .leftClick("edit")
        .rightClick("pick")
        .build();

    public static final LangItem PRODUCT_DELETE = LangItem.builder(PREFIX + "Product.Delete")
        .name("Delete Product")
        .text("Permanently deletes the product", "with all settings and data.")
        .emptyLine()
        .click("delete")
        .build();

    public static final LangItem PRODUCT_ROTATING_OBJECT = LangItem.builder(PREFIX + "Product.Rotating.Object")
        .name(PRODUCT_PREVIEW_NAME)
        .current("Handler", PRODUCT_HANDLER)
        .current("Currency", PRODUCT_CURRENCY)
        .current("Price Type", PRODUCT_PRICE_TYPE)
        .current("Buy", PRODUCT_PRICE.apply(BUY))
        .current("Sell", PRODUCT_PRICE.apply(SELL))
        .emptyLine()
        .click("edit")
        .build();

    public static final LangItem PRODUCT_ROTATING_CREATE = LangItem.builder(PREFIX + "Product.Rotating.Create")
        .name("New Product")
        .text("Creates a new product", "to use in rotation(s).")
        .emptyLine()
        .click("create")
        .build();

    public static final LangItem PRODUCT_FREE_SLOT = LangItem.builder(PREFIX + "Product.FreeSlot")
        .name(GREEN.wrap(BOLD.wrap("Free Slot")))
        .emptyLine()
        .text(LIGHT_CYAN.wrap(BOLD.wrap("Quick Creation:")))
        .text("Simply drag'n'drop item here!")
        .emptyLine()
        .text(LIGHT_PURPLE.wrap(BOLD.wrap("Manual Creation:")))
        .text("Click with empty cursor to", "open creation wizard!")
        .build();

    public static final LangItem PRODUCT_ROTATION_SLOT = LangItem.builder(PREFIX + "Product.RotationSlot")
        .name(RED.wrap(BOLD.wrap("Rotation Slot")))
        .text("Slot reserved for", "rotating products.")
        .build();

    public static final LangItem PRODUCT_RESERVED_SLOT = LangItem.builder(PREFIX + "Product.ReservedSlot")
        .name(RED.wrap(BOLD.wrap("Reserved Slot")))
        .text("This slot is occupied by a shop product.")
        .build();

    public static final LangItem PRODUCT_CREATION_INFO = builder(PREFIX + "Product.Creation.Info")
        .name("Creation Wizard")
        .emptyLine()
        .text(LIGHT_YELLOW.wrap(BOLD.wrap("Step #1")))
        .text("Click an item in your inventory")
        .text("to select it as a base for new product.")
        .emptyLine()
        .text(LIGHT_YELLOW.wrap(BOLD.wrap("Step #2")))
        .text("Select and click product type button")
        .text("to create a new product.")
        .build();

    public static final LangItem PRODUCT_CREATION_ITEM = builder(PREFIX + "Product.Creation.Item")
        .name("Item Product")
        .text("Gives item(s) directly to player's", "inventory when purchased.")
        .emptyLine()
        .text(GREEN.wrap("✔") + " Custom Items")
        .text(GREEN.wrap("✔") + " NBT Support")
        .text(GREEN.wrap("✔") + " Placeholders")
        .text(GREEN.wrap("✔") + " Sellable")
        .emptyLine()
        .click("create")
        .build();

    public static final LangItem PRODUCT_CREATION_COMMAND = builder(PREFIX + "Product.Creation.Command")
        .name("Command Product")
        .text("Runs specified command(s) with", "placeholders when purchased.")
        .emptyLine()
        .text(GREEN.wrap("✔") + " Custom Name")
        .text(GREEN.wrap("✔") + " Custom Lore")
        .text(GREEN.wrap("✔") + " Placeholders")
        .text(GREEN.wrap("✔") + " Unlimited Commands")
        .text(RED.wrap("✘") + " Unsellable")
        .emptyLine()
        .click("create")
        .build();

    // ===================================
    // Product Editor Locales
    // ===================================

    public static final LangItem PRODUCT_EDIT_ITEM = LangItem.builder(PREFIX + "Product.Item")
        .name("Item")
        .text("Defines product's item.")
        .emptyLine()
        .dragAndDrop("replace")
        .rightClick("get a copy")
        .build();

    public static final LangItem PRODUCT_EDIT_ICON = LangItem.builder(PREFIX + "Product.Preview")
        .name("Icon")
        .text("Defines product's icon.")
        .emptyLine()
        .dragAndDrop("replace")
        .rightClick("get a copy")
        .build();

    public static final LangItem PRODUCT_EDIT_NBT_MATCH = LangItem.builder(PREFIX + "Product.RespectItemMeta")
        .name("Match NBT")
        .current("Enabled", PRODUCT_RESPECT_META)
        .emptyLine()
        .text("Controls whether whole NBT", "must match to sell this product.")
        .emptyLine()
        .text("Otherwise any item with", "the same type is accepted.")
        .emptyLine()
        .click("toggle")
        .build();

    public static final LangItem PRODUCT_EDIT_PRICE = LangItem.builder(PREFIX + "Product.PriceManager")
        .name("Price Manager")
        .current("Type", PRODUCT_PRICE_TYPE)
        .current("Currency", PRODUCT_CURRENCY)
        .current("Buy", PRODUCT_PRICE.apply(BUY))
        .current("Sell", PRODUCT_PRICE.apply(SELL))
        .emptyLine()
        .text("Sets product currency and price.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem PRODUCT_EDIT_RANKS_REQUIRED = LangItem.builder(PREFIX + "Product.RanksRequired")
        .name("Required Ranks")
        .textRaw(PRODUCT_ALLOWED_RANKS)
        .emptyLine()
        .text("Only players with any of listed ranks", "will have access to this product.")
        .emptyLine()
        .leftClick("add rank")
        .rightClick("remove all & disable")
        .build();

    public static final LangUIButton PRODUCT_EDIT_REQUIRED_PERMISSIONS = LangUIButton.builder(PREFIX + "Product.RequiredPerms", "Required Permissions")
        .description(PRODUCT_REQUIRED_PERMISSIONS, "", "Only players with " + GREEN.wrap("any") + " of listed permissions", "will have access to this product.")
        .leftClick("add permission")
        .rightClick("remove all & disable")
        .build();

    public static final LangUIButton PRODUCT_EDIT_FORBIDDEN_PERMISSIONS = LangUIButton.builder(PREFIX + "Product.ForbiddenPerms", "Forbidden Permissions")
        .description(PRODUCT_FORBIDDEN_PERMISSIONS, "", "Only players with " + RED.wrap("none") + " of listed permissions", "will have access to this product.")
        .leftClick("add permission")
        .rightClick("remove all & disable")
        .build();

    public static final LangItem PRODUCT_EDIT_COMMANDS = LangItem.builder(PREFIX + "Product.Commands")
        .name("Commands")
        .textRaw(PRODUCT_COMMANDS)
        .emptyLine()
        .text("Runs listed command when", "player purchases this item.")
        .emptyLine()
        .current("Use " + LIGHT_YELLOW.wrap(PLAYER_NAME) + " for player name.")
        .current(LIGHT_YELLOW.wrap(Plugins.PLACEHOLDER_API) + " supported.")
        .emptyLine()
        .leftClick("add command")
        .rightClick("remove all")
        .build();

    public static final LangItem PRODUCT_EDIT_STOCK = LangItem.builder(PREFIX + "Product.Stock.Category")
        .name("Global & Player Stock")
        .text("Controls product limits on", LIGHT_YELLOW.wrap("per server") + " and " + LIGHT_YELLOW.wrap("per player"), "basis.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem PRODUCT_EDIT_STOCK_RESET = LangItem.builder(PREFIX + "Product.Stocks.ResetData")
        .name("Reset & Update")
        .text("Resets stocks and limits datas", "and refreshes their values.")
        .emptyLine()
        .click("reset")
        .build();

    // ===================================
    // Global Stock Locales
    // ===================================

    public static final LangItem PRODUCT_EDIT_STOCK_BUY = LangItem.builder(PREFIX + "Product.Stock.Global.BuyInitial")
        .name("Buy Stock")
        .current("Current", PRODUCT_STOCK_AMOUNT_INITIAL.apply(BUY))
        .emptyLine()
        .text("Sets the global buy stock.")
        .emptyLine()
        .leftClick("change")
        .rightClick("set unlimited")
        .build();

    public static final LangItem PRODUCT_EDIT_STOCK_SELL = LangItem.builder(PREFIX + "Product.Stock.Global.SellInitial")
        .name("Sell Stock")
        .current("Current", PRODUCT_STOCK_AMOUNT_INITIAL.apply(SELL))
        .emptyLine()
        .text("Sets the global sell stock.")
        .emptyLine()
        .leftClick("change")
        .rightClick("set unlimited")
        .build();

    public static final LangItem PRODUCT_EDIT_STOCK_RESET_TIME = LangItem.builder(PREFIX + "Product.Stocks.StockResetTime")
        .name("Restock Time")
        .current("Current", PRODUCT_STOCKS_RESET_TIME)
        .emptyLine()
        .text("Controls how soon stocks will", "reset back to default values.")
        .emptyLine()
        .leftClick("change")
        .rightClick("disable")
        .build();

    // ===================================
    // Player Stock Locales
    // ===================================

    public static final LangItem PRODUCT_EDIT_LIMIT_BUY = LangItem.builder(PREFIX + "Product.Stock.Player.BuyInitial")
        .name("Buy Limit")
        .current("Current", PRODUCT_LIMIT_AMOUNT_INITIAL.apply(BUY))
        .emptyLine()
        .text("Sets an individual buy limit", "for a player.")
        .emptyLine()
        .leftClick("change")
        .rightClick("set unlimited")
        .build();

    public static final LangItem PRODUCT_EDIT_LIMIT_SELL = LangItem.builder(PREFIX + "Product.Stock.Player.SellInitial")
        .name("Sell Limit")
        .current("Current", PRODUCT_LIMIT_AMOUNT_INITIAL.apply(SELL))
        .emptyLine()
        .text("Sets an individual sell limit", "for a player.")
        .emptyLine()
        .leftClick("change")
        .rightClick("set unlimited")
        .build();

    public static final LangItem PRODUCT_EDIT_LIMIT_RESET_TIME = LangItem.builder(PREFIX + "Product.Stocks.LimitResetTime")
        .name("Reset Time")
        .current("Current", PRODUCT_LIMITS_RESET_TIME)
        .emptyLine()
        .text("Controls how soon limits will", "reset back to default values.")
        .emptyLine()
        .leftClick("change")
        .rightClick("disable")
        .build();

    public static final LangItem PRODUCT_EDIT_DISCOUNT = LangItem.builder(PREFIX + "Product.Price.DiscountAllowed")
        .name("Discount Allowed")
        .current("Enabled", PRODUCT_DISCOUNT_ALLOWED)
        .emptyLine()
        .text("Sets whether product", "can be affected by shop's discounts.")
        .emptyLine()
        .leftClick("toggle")
        .build();

    // ===================================
    // Price Editor Locales
    // ===================================


    // ===================================
    // Discount Locales
    // ===================================

    public static final LangItem DISCOUNT_CREATE = LangItem.builder(PREFIX + "Discount.Create")
        .name("New Discount")
        .build();

    public static final LangItem DISCOUNT_OBJECT = LangItem.builder(PREFIX + "Discount.Object")
        .name("Discount")
        .current("Amount", Placeholders.DISCOUNT_CONFIG_AMOUNT)
        .current("Days", Placeholders.DISCOUNT_CONFIG_DAYS)
        .current("Times", Placeholders.DISCOUNT_CONFIG_TIMES)
        .emptyLine()
        .leftClick("edit")
        .shiftRight("delete " + LIGHT_RED.wrap("(no undo)"))
        .build();

    public static final LangItem DISCOUNT_AMOUNT = LangItem.builder(PREFIX + "Discount.Amount")
        .name("Amount")
        .current("Amount", Placeholders.DISCOUNT_CONFIG_AMOUNT + "%")
        .emptyLine()
        .text("Sets the discount amount (in percent).")
        .emptyLine()
        .leftClick("change")
        .build();

    public static final LangItem DISCOUNT_DURATION = LangItem.builder(PREFIX + "Discount.Duration")
        .name("Duration")
        .current("Duration", Placeholders.DISCOUNT_CONFIG_DURATION)
        .emptyLine()
        .text("Sets how long (in seconds) this", "discount will be active.")
        .emptyLine()
        .leftClick("change")
        .build();

    public static final LangItem DISCOUNT_DAYS = LangItem.builder( PREFIX + "Discount.Days")
        .name("Active Days")
        .current("Days", Placeholders.DISCOUNT_CONFIG_DAYS)
        .emptyLine()
        .text("A list of days, when this discount will have effect.")
        .emptyLine()
        .text("At least one day and time are required!")
        .emptyLine()
        .leftClick("add day")
        .rightClick("remove all")
        .build();

    public static final LangItem DISCOUNT_TIMES = LangItem.builder(PREFIX + "Discount.Times")
        .name("Active Times")
        .current("Days", Placeholders.DISCOUNT_CONFIG_TIMES)
        .emptyLine()
        .text("A list of times, when this discount will be activated.")
        .emptyLine()
        .text("At least one day and time are required!")
        .emptyLine()
        .leftClick("add time")
        .rightClick("remove all")
        .build();
}
