package su.nightexpress.excellentshop.feature.virtualshop.core;

import su.nightexpress.nightcore.language.entry.LangItem;

import static su.nightexpress.excellentshop.api.product.TradeType.BUY;
import static su.nightexpress.excellentshop.api.product.TradeType.SELL;
import static su.nightexpress.excellentshop.ShopPlaceholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@Deprecated
public class VirtualLocales {

    private static final String PREFIX = "VirtualShop.Editor.";

    public static final LangItem SHOP_DELETE = LangItem.builder(PREFIX + "Shop.Delete")
        .name("Delete Shop")
        .text("Permanently deletes the shop", "with all settings and items.")
        .emptyLine()
        .click("delete")
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

    public static final LangItem SHOP_EDIT_LAYOUTS = LangItem.builder(PREFIX + "Shop.Layouts.Info")
        .name("Layouts")
        .text("Apply custom layout(s) across your shop.")
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

    public static final LangItem ROTATION_ADD_ITEM = LangItem.builder(PREFIX + "Rotation.AddItem")
        .name("Add Item")
        .click("add")
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

    public static final LangItem ROTATION_ITEM_OBJECT = LangItem.builder(PREFIX + "Rotation.Item.Object")
        .name(PRODUCT_PREVIEW_NAME + RESET.getBracketsName() + GRAY.wrap(" (ID: " + WHITE.wrap(PRODUCT_ID) + ")"))
        .current("Weight", GENERIC_WEIGHT)
        .emptyLine()
        .leftClick("set weight")
        .dropKey("remove")
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
}
