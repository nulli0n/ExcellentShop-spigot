package su.nightexpress.nexshop.shop.virtual.config;

import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nightcore.language.entry.LangItem;
import su.nightexpress.nightcore.util.Plugins;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.api.shop.type.TradeType.*;
import static su.nightexpress.nexshop.shop.virtual.Placeholders.*;

public class VirtualLocales {

    private static final String PREFIX = "VirtualShop.Editor.";

    public static final LangItem SHOP_CREATE = LangItem.builder(PREFIX + "Shop.Create")
        .name("New Shop")
        .emptyLine()
        .leftClick("create Static Shop")
        .rightClick("create Rotating Shop")
        .build();

    public static final LangItem SHOP_OBJECT = LangItem.builder(PREFIX + "Shop.Object")
        .name(SHOP_NAME)
        .current("Type", SHOP_TYPE)
        .current("Pages", Placeholders.SHOP_PAGES)
        .emptyLine()
        .leftClick("edit")
        .shiftRight("delete " + RED.enclose("(no undo)"))
        .build();

    public static final LangItem SHOP_DISPLAY_NAME = LangItem.builder(PREFIX + "Shop.DisplayName")
        .name("Display Name")
        .current("Current", SHOP_NAME)
        .emptyLine()
        .leftClick("change")
        .build();

    public static final LangItem SHOP_DESCRIPTION = LangItem.builder(PREFIX + "Shop.Description")
        .name("Description")
        .text(Placeholders.SHOP_DESCRIPTION).emptyLine()
        .emptyLine()
        .leftClick("add line")
        .rightClick("remove all")
        .build();

    public static final LangItem SHOP_PAGES = LangItem.builder(PREFIX + "Shop.Pages")
        .name("Pages Amount")
        .current("Current", Placeholders.SHOP_PAGES)
        .emptyLine()
        .text("Amount of pages in the shop.")
        .emptyLine()
        .text("Make sure that shop layout config")
        .text("contains page buttons.")
        .emptyLine()
        .leftClick("+1 page")
        .rightClick("-1 page")
        .build();

    public static final LangItem SHOP_ICON = LangItem.builder(PREFIX + "Shop.Icon")
        .name("Icon")
        .dragAndDrop("replace")
        .rightClick("get a copy")
        .build();

    public static final LangItem SHOP_PERMISSION = LangItem.builder(PREFIX + "Shop.PermissionRequirement")
        .name("Permission Requirement")
        .current("Enabled", SHOP_PERMISSION_REQUIRED)
        .current("Node", SHOP_PERMISSION_NODE)
        .emptyLine()
        .text("Sets whether or not permission", "is required to use this shop.")
        .emptyLine()
        .leftClick("toggle")
        .build();

    public static final LangItem SHOP_TRADES = LangItem.builder(PREFIX + "Shop.Transactions")
        .name("Transactions")
        .current("Buying Enabled", SHOP_BUY_ALLOWED)
        .current("Selling Enabled", SHOP_SELL_ALLOWED)
        .emptyLine()
        .text("Global rules allowing / disallowing")
        .text("selling and buying in this shop.")
        .emptyLine()
        .leftClick("toggle buying")
        .rightClick("toggle selling")
        .build();

    public static final LangItem SHOP_ATTACHED_NPCS = LangItem.builder(PREFIX + "Shop.AttachedNPCs")
        .name("Attached NPCs")
        .text(SHOP_NPC_IDS)
        .emptyLine()
        .text("List of NPCs (id) attached to this shop", "to open it on interaction.")
        .text(LIGHT_RED.enclose("(Citizens required)"))
        .emptyLine()
        .leftClick("add NPC")
        .rightClick("remove all")
        .build();

    public static final LangItem SHOP_LAYOUT = LangItem.builder(PREFIX + "Shop.Layout")
        .name("Layout")
        .current("Current", Placeholders.SHOP_LAYOUT)
        .emptyLine()
        .text("Sets GUI layout used in this shop.")
        .emptyLine()
        .text("Create more layouts in " + LIGHT_YELLOW.enclose(VirtualShopModule.DIR_LAYOUTS))
        .emptyLine()
        .click("change")
        .build();

    public static final LangItem SHOP_DISCOUNTS = LangItem.builder(PREFIX + "Shop.Discounts")
        .name("Discounts")
        .text("Create and manage shop discounts here.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem SHOP_PRODUCTS = LangItem.builder(PREFIX + "Shop.Products")
        .name("Products")
        .text("Create and manage products here.")
        .emptyLine()
        .leftClick("navigate")
        .dropKey("reset & update all prices")
        .swapKey("reset all stocks & limits")
        .build();

    public static final LangItem SHOP_ROTATION_TYPE = LangItem.builder(PREFIX + "Shop.Rotation.Type")
        .name("Rotation Type")
        .current("Current", Placeholders.SHOP_ROTATION_TYPE)
        .emptyLine()
        .text(LIGHT_YELLOW.enclose(BOLD.enclose("Types:")))
        .current(RotationType.INTERVAL.name(), "Every X minutes.")
        .current(RotationType.FIXED.name(), "Strictly at specified times.")
        .emptyLine()
        .click("change")
        .build();

    public static final LangItem SHOP_ROTATION_INTERVAL = LangItem.builder(PREFIX + "Shop.Rotation.Interval")
        .name("Rotation Interval")
        .current("Current", Placeholders.SHOP_ROTATION_INTERVAL)
        .emptyLine()
        .text("Sets how often shop products", "will be rotated (changed).")
        .emptyLine()
        .leftClick("change")
        .dropKey("force rotate")
        .build();

    public static final LangItem SHOP_ROTATION_TIMES = LangItem.builder(PREFIX + "Shop.Rotation.Times")
        .name("Rotation Times")
        .text("Here you can set rotation", "times for each day of a week.").emptyLine()
        .click("navigate")
        .build();

    public static final LangItem SHOP_ROTATION_PRODUCTS = LangItem.builder(PREFIX + "Shop.Rotation.Products")
        .name("Rotation Products")
        .current("Min", SHOP_ROTATION_MIN_PRODUCTS)
        .current("Max", SHOP_ROTATION_MAX_PRODUCTS)
        .current("Slots", SHOP_ROTATION_PRODUCT_SLOTS)
        .emptyLine()
        .text("Sets how many products", "will be used in rotations and", "slots where they will appear.")
        .emptyLine()
        .leftClick("change min")
        .rightClick("change max")
        .dropKey("change slots")
        .build();

    public static final LangItem PRODUCT_OBJECT = LangItem.builder(PREFIX + "Product.Object.Static2")
        .name(PRODUCT_PREVIEW_NAME)
        .current("Handler", PRODUCT_HANDLER)
        .current("Currency", PRODUCT_CURRENCY)
        .current("Price Type", Placeholders.PRODUCT_PRICE_TYPE)
        .current("Buy", PRODUCT_PRICE.apply(BUY))
        .current("Sell", PRODUCT_PRICE.apply(SELL))
        .emptyLine()
        .text("You can freely move this product", "between slots, pages, and shops!")
        .emptyLine()
        .leftClick("edit")
        .rightClick("pick")
        .dropKey("delete " + LIGHT_RED.enclose("(no undo)"))
        .build();

    public static final LangItem ROTATING_PRODUCT_OBJECT = LangItem.builder(PREFIX + "Product.Object.Rotating2")
        .name(PRODUCT_PREVIEW_NAME)
        .current("Rotation Chance", Placeholders.PRODUCT_ROTATION_CHANCE + "%")
        .current("Currency", PRODUCT_CURRENCY)
        .current("Buy Price", PRODUCT_PRICE.apply(BUY))
        .current("Sell Price", PRODUCT_PRICE.apply(SELL))
        .emptyLine()
        .leftClick("edit")
        .rightClick("pick")
        .dropKey("delete " + LIGHT_RED.enclose("(no undo)"))
        .build();

    public static final LangItem PRODUCT_FREE_SLOT = LangItem.builder(PREFIX + "Product.FreeSlot")
        .name(GREEN.enclose(BOLD.enclose("Free Slot")))
        .emptyLine()
        .text(GREEN.enclose(BOLD.enclose("ITEM PRODUCT:")))
        .text("Click with item on cursor", "to create " + GREEN.enclose("item") + " product.")
        .emptyLine()
        .text("Hold " + WHITE.enclose("Shift") + " to bypass", "custom item detection.")
        .emptyLine()
        .text(LIGHT_ORANGE.enclose(BOLD.enclose("COMMAND PRODUCT:")))
        .text("Click with empty cursor to", "create " + LIGHT_ORANGE.enclose("command") + " product.")
        .build();

    public static final LangItem PRODUCT_RESERVED_SLOT = LangItem.builder(PREFIX + "Product.ReservedSlot")
        .name(RED.enclose(BOLD.enclose("Reserved Slot")))
        .text("This slot is occupied by a shop product.")
        .build();

    // ===================================
    // Product Editor Locales
    // ===================================

    public static final LangItem PRODUCT_ITEM = LangItem.builder(PREFIX + "Product.Item")
        .name("Actual Item")
        .text("This is the item that:")
        .current("Players gets on " + GREEN.enclose("purchase") + ".")
        .current("Players must have to " + RED.enclose("sell") + ".")
        .emptyLine()
        .dragAndDrop("replace")
        .rightClick("get a copy")
        .emptyLine()
        .text("(Hold " + WHITE.enclose("Shift") + " to bypass item detection)")
        .build();

    public static final LangItem PRODUCT_PREVIEW = LangItem.builder(PREFIX + "Product.Preview")
        .name("Preview Item")
        .text("This item is used purely as", "visual product representation.")
        .emptyLine()
        .text("Feel free to " + LIGHT_YELLOW.enclose("rename") + " it, add " + LIGHT_YELLOW.enclose("lore") + " and " + LIGHT_YELLOW.enclose("enchants") + "!")
        .emptyLine()
        .dragAndDrop("replace")
        .rightClick("get a copy")
        .build();

    public static final LangItem PRODUCT_RESPECT_ITEM_META = LangItem.builder(PREFIX + "Product.RespectItemMeta")
        .name("Respect Item Meta")
        .current("Enabled", Placeholders.PRODUCT_ITEM_META_ENABLED)
        .emptyLine()
        .text("When " + GREEN.enclose("enabled") + ", players can sell only", "items with exact data as in " + WHITE.enclose("Actual Item") + ".")
        .emptyLine()
        .text("When " + RED.enclose("disabled") + ", players can sell any", "item of the same type as " + WHITE.enclose("Actual Item") + ".")
        .emptyLine()
        .leftClick("toggle")
        .build();

    public static final LangItem PRODUCT_PRICE_MANAGER = LangItem.builder(PREFIX + "Product.PriceManager")
        .name("Price Manager")
        .current("Type", Placeholders.PRODUCT_PRICE_TYPE)
        .current("Currency", PRODUCT_CURRENCY)
        .current("Buy", PRODUCT_PRICE.apply(BUY))
        .current("Sell", PRODUCT_PRICE.apply(SELL))
        .emptyLine()
        .text("Sets product currency and price.")
        .emptyLine()
        .leftClick("edit")
        .dropKey("refresh")
        .build();

    public static final LangItem PRODUCT_RANKS_REQUIRED = LangItem.builder(PREFIX + "Product.RanksRequired")
        .name("Required Ranks")
        .text(PRODUCT_ALLOWED_RANKS)
        .emptyLine()
        .text("Only players with listed ranks (groups)", "will have access to this product.")
        .emptyLine()
        .leftClick("add rank")
        .rightClick("remove all & disable")
        .build();

    public static final LangItem PRODUCT_PERMISIONS_REQUIRED = LangItem.builder(PREFIX + "Product.PermissionsRequired")
        .name("Required Permissions")
        .text(PRODUCT_REQUIRED_PERMISSIONS)
        .emptyLine()
        .text("Only players with listed permissions", "will have access to this product.")
        .emptyLine()
        .shiftLeft("add permission")
        .shiftRight("remove all & disable")
        .build();

    public static final LangItem PRODUCT_COMMANDS = LangItem.builder(PREFIX + "Product.Commands")
        .name("Commands")
        .text(Placeholders.PRODUCT_COMMANDS)
        .emptyLine()
        .text("Commands to run when product is purchased.")
        .emptyLine()
        .text(LIGHT_YELLOW.enclose(BOLD.enclose("Placeholders:")))
        .current(PLAYER_NAME, "Player (buyer) name.")
        .current(Plugins.PLACEHOLDER_API, "All of them.")
        .emptyLine()
        .leftClick("add command")
        .rightClick("remove all")
        .build();

    public static final LangItem PRODUCT_ROTATION_CHANCE = LangItem.builder(PREFIX + "Product.Rotation.Chance")
        .name("Weight")
        .current("Current", Placeholders.PRODUCT_ROTATION_CHANCE + "%")
        .emptyLine()
        .text("Greater the weight, greater the chance", "that this product will appear", "in shop rotations.")
        .emptyLine()
        .click("change")
        .build();

    public static final LangItem PRODUCT_ROTATION_DAY_TIMES = LangItem.builder(PREFIX + "Product.Rotation.DayTimes")
        .name(GENERIC_NAME)
        .text(GENERIC_TIME)
        .emptyLine()
        .leftClick("add")
        .rightClick("remove all")
        .build();

    // ===================================
    // Stock Editor Locales
    // ===================================

    public static final LangItem PRODUCT_STOCK = LangItem.builder(PREFIX + "Product.Stock.Category")
        .name("Global & Player Stock")
        .text("Here you can set how many of the product", "is available for sale and purchase", "globally and per player.")
        .emptyLine()
        .click("navigate")
        .build();

    public static final LangItem PRODUCT_STOCK_GLOBAL_INFO = LangItem.builder(PREFIX + "Product.Stock.Info.Global")
        .name("Global Stock")
        .text("Limits amount of the product available", "for sale/purchase globally for all players.")
        .emptyLine()
        .text(LIGHT_YELLOW.enclose(BOLD.enclose("Manual Restock:")))
        .current("By Purchase: " + GREEN.enclose("Sell Stock ↑") + " | " + RED.enclose("Buy Stock ↓"))
        .current("By Sale: " + RED.enclose("Sell Stock ↓") + " | " + GREEN.enclose("Buy Stock ↑"))
        .emptyLine()
        .rightClick("wipe global stock data")
        .build();

    public static final LangItem PRODUCT_STOCK_PLAYER_INFO = LangItem.builder(PREFIX + "Product.Stock.Info.Player")
        .name("Player Limits")
        .text("Limits amount of the product available", "for sale/purchase per a player.")
        .emptyLine()
        .text(LIGHT_YELLOW.enclose(BOLD.enclose("Manual Restock:")))
        .text("Manual restock for limits is " + RED.enclose("not possible") + ".")
        .text("Players have to wait restock time.")
        .emptyLine()
        .rightClick("wipe player limit data")
        .build();

    // ===================================
    // Global Stock Locales
    // ===================================

    public static final LangItem PRODUCT_STOCK_GLOBAL_BUY_INITIAL = LangItem.builder(PREFIX + "Product.Stock.Global.BuyInitial")
        .name("Initial Buy Amount")
        .current("Current", PRODUCT_STOCK_AMOUNT_INITIAL.apply(BUY))
        .emptyLine()
        .text("Sets initial product amount for purchases.")
        .emptyLine()
        .leftClick("change")
        .rightClick("set unlimited")
        .build();

    public static final LangItem PRODUCT_STOCK_GLOBAL_SELL_INITIAL = LangItem.builder(PREFIX + "Product.Stock.Global.SellInitial")
        .name("Initial Sell Amount")
        .current("Current", PRODUCT_STOCK_AMOUNT_INITIAL.apply(SELL))
        .emptyLine()
        .text("Sets initial product amount for sales.")
        .emptyLine()
        .leftClick("change")
        .rightClick("set unlimited")
        .build();

    public static final LangItem PRODUCT_STOCK_GLOBAL_RESTOCK_BUY = LangItem.builder(PREFIX + "Product.Stock.Global.BuyRestock")
        .name("Buy Restock Time")
        .current("Current", PRODUCT_STOCK_RESTOCK_TIME.apply(BUY))
        .emptyLine()
        .text("Sets how often product amount available", "for purchase will reset back", "to default (initial).")
        .emptyLine()
        .text("When disabled " + DARK_GRAY.enclose("(-1)") + ", auto-restock", "will never happen!")
        .emptyLine()
        .leftClick("change")
        .rightClick("disable")
        .build();

    public static final LangItem PRODUCT_STOCK_GLOBAL_RESTOCK_SELL = LangItem.builder(PREFIX + "Product.Stock.Global.SellRestock")
        .name("Sell Restock Time")
        .current("Current", PRODUCT_STOCK_RESTOCK_TIME.apply(SELL))
        .emptyLine()
        .text("Sets how often product amount available", "for sale will reset back", "to default (initial).")
        .emptyLine()
        .text("When disabled " + DARK_GRAY.enclose("(-1)") + ", auto-restock", "will never happen!")
        .emptyLine()
        .leftClick("change")
        .rightClick("disable")
        .build();

    // ===================================
    // Player Stock Locales
    // ===================================

    public static final LangItem PRODUCT_STOCK_PLAYER_BUY_INITIAL = LangItem.builder(PREFIX + "Product.Stock.Player.BuyInitial")
        .name("Initial Buy Amount")
        .current("Current", PRODUCT_LIMIT_AMOUNT_INITIAL.apply(BUY))
        .emptyLine()
        .text("Sets initial product amount for purchases.")
        .emptyLine()
        .leftClick("change")
        .rightClick("set unlimited")
        .build();

    public static final LangItem PRODUCT_STOCK_PLAYER_SELL_INITIAL = LangItem.builder(PREFIX + "Product.Stock.Player.SellInitial")
        .name("Initial Sell Amount")
        .current("Current", PRODUCT_LIMIT_AMOUNT_INITIAL.apply(SELL))
        .emptyLine()
        .text("Sets initial product amount for sales.")
        .emptyLine()
        .leftClick("change")
        .rightClick("set unlimited")
        .build();

    public static final LangItem PRODUCT_STOCK_PLAYER_RESTOCK_BUY = LangItem.builder(PREFIX + "Product.Stock.Player.BuyRestock")
        .name("Buy Restock Time")
        .current("Current", PRODUCT_LIMIT_RESTOCK_TIME.apply(BUY))
        .emptyLine()
        .text("Sets how often product amount available", "for purchase will reset back", "to default (initial).")
        .emptyLine()
        .text("When disabled " + DARK_GRAY.enclose("(-1)") + ", auto-restock", "will never happen!")
        .emptyLine()
        .leftClick("change")
        .rightClick("disable")
        .build();

    public static final LangItem PRODUCT_STOCK_PLAYER_RESTOCK_SELL = LangItem.builder(PREFIX + "Product.Stock.Player.SellRestock")
        .name("Sell Restock Time")
        .current("Current", PRODUCT_LIMIT_RESTOCK_TIME.apply(SELL))
        .emptyLine()
        .text("Sets how often product amount available", "for sale will reset back", "to default (initial).")
        .emptyLine()
        .text("When disabled " + DARK_GRAY.enclose("(-1)") + ", auto-restock", "will never happen!")
        .emptyLine()
        .leftClick("change")
        .rightClick("disable")
        .build();

    // ===================================
    // Price Editor Locales
    // ===================================

    public static final LangItem PRODUCT_PRICE_INFO = LangItem.builder(PREFIX + "Product.Price.Info")
        .name("Price Info")
        .current("Buy Current", PRODUCT_PRICE_FORMATTED.apply(BUY))
        .current("Sell Current", PRODUCT_PRICE_FORMATTED.apply(SELL))
        .emptyLine()
        .text(LIGHT_YELLOW.enclose(BOLD.enclose("Update:")))
        .text("Fetches and applies the price data", "from the database.")
        .text("If no data present or expired,", "creates a fresh one.")
        .emptyLine()
        .text("In most cases won't change anything", "until you wipe it out.")
        .emptyLine()
        .text(LIGHT_YELLOW.enclose(BOLD.enclose("Wipe:")))
        .text("Wipes out product price data", "from the database.")
        .emptyLine()
        .text("Use " + LIGHT_YELLOW.enclose("update") + " option to generate a new one.")
        .emptyLine()
        .leftClick("update")
        .rightClick("wipe data")
        .build();

    public static final LangItem PRODUCT_PRICE_TYPE = LangItem.builder(PREFIX + "Product.Price.Type")
        .name("Price Type")
        .current("Current", Placeholders.PRODUCT_PRICE_TYPE)
        .emptyLine()
        .text("Sets product price type.", "Different types have different settings.")
        .emptyLine()
        .click("change")
        .build();

    public static final LangItem PRODUCT_PRICE_CURRENCY = LangItem.builder(PREFIX + "Product.Price.Currency")
        .name("Currency")
        .current("Current", PRODUCT_CURRENCY).emptyLine()
        .text("Sets product currency.").emptyLine()
        .click("change")
        .build();

    public static final LangItem PRODUCT_DISCOUNT = LangItem.builder(PREFIX + "Product.Price.DiscountAllowed")
        .name("Discount Allowed")
        .current("Enabled", PRODUCT_DISCOUNT_ALLOWED)
        .emptyLine()
        .text("Sets whether or not this product", "can be affected by shop's discounts.")
        .emptyLine()
        .leftClick("toggle")
        .build();

    public static final LangItem PRODUCT_PRICE_FLAT_BUY = LangItem.builder(PREFIX + "Product.Price.Flat.Buy")
        .name("Buy Price")
        .current("Current", PRODUCT_PRICE.apply(BUY))
        .emptyLine()
        .leftClick("change")
        .dropKey("disable")
        .build();

    public static final LangItem PRODUCT_PRICE_FLAT_SELL = LangItem.builder(PREFIX + "Product.Price.Flat.Sell")
        .name("Sell Price")
        .current("Current", PRODUCT_PRICE.apply(SELL))
        .emptyLine()
        .leftClick("change")
        .dropKey("disable")
        .build();

    public static final LangItem PRODUCT_PRICE_FLOAT_BUY = LangItem.builder(PREFIX + "Product.Price.Float.Buy")
        .name("Buy Price Bounds")
        .current("Min", PRODUCT_PRICER_RANGE_MIN.apply(BUY))
        .current("Max", PRODUCT_PRICER_RANGE_MAX.apply(BUY))
        .emptyLine()
        .text("Sets product buy price bounds.", "Final price will be within these values.")
        .emptyLine()
        .leftClick("change")
        .dropKey("disable")
        .build();

    public static final LangItem PRODUCT_PRICE_FLOAT_SELL = LangItem.builder(PREFIX + "Product.Price.Float.Sell")
        .name("Sell Price Bounds")
        .current("Min", PRODUCT_PRICER_RANGE_MIN.apply(SELL))
        .current("Max", PRODUCT_PRICER_RANGE_MAX.apply(SELL))
        .emptyLine()
        .text("Sets product sell price bounds.", "Final price will be within these values.")
        .emptyLine()
        .leftClick("change")
        .dropKey("disable")
        .build();

    public static final LangItem PRODUCT_PRICE_FLOAT_DECIMALS = LangItem.builder(PREFIX + "Product.Price.Float.Decimals")
        .name("Cut Decimals")
        .current("Enabled", PRODUCT_PRICER_FLOAT_ROUND_DECIMALS).emptyLine()
        .text("Sets whether or not prices should", "generate as whole numbers.").emptyLine()
        .click("toggle")
        .build();

    public static final LangItem PRODUCT_PRICE_FLOAT_REFRESH_DAYS = LangItem.builder(PREFIX + "Product.Price.Float.RefreshDays")
        .name("Refresh Days")
        .text(PRODUCT_PRICER_FLOAT_REFRESH_DAYS)
        .emptyLine()
        .text("Sets days allowed for price generation.")
        .emptyLine()
        .leftClick("add day")
        .rightClick("remove all")
        .build();

    public static final LangItem PRODUCT_PRICE_FLOAT_REFRESH_TIMES = LangItem.builder(PREFIX + "Product.Price.Float.RefreshTimes")
        .name("Refresh Times")
        .text(PRODUCT_PRICER_FLOAT_REFRESH_TIMES)
        .emptyLine()
        .text("Sets times used for price generation.")
        .emptyLine()
        .leftClick("add time")
        .rightClick("remove all")
        .build();

    public static final LangItem PRODUCT_PRICE_DYNAMIC_INITIAL = LangItem.builder(PREFIX + "Product.Price.Dynamic.Initial")
        .name("Initial Price")
        .current("Buy", PRODUCT_PRICER_DYNAMIC_INITIAL_BUY)
        .current("Sell", PRODUCT_PRICER_DYNAMIC_INITIAL_SELL)
        .emptyLine()
        .text("Sets initial product price.", "These values will be used as default/start ones.")
        .emptyLine()
        .leftClick("change buy")
        .rightClick("change sell")
        .build();

    public static final LangItem PRODUCT_PRICE_DYNAMIC_STEP = LangItem.builder(PREFIX + "Product.Price.Dynamic.Step")
        .name("Price Step")
        .current("Buy", PRODUCT_PRICER_DYNAMIC_STEP_BUY)
        .current("Sell", PRODUCT_PRICER_DYNAMIC_STEP_SELL)
        .emptyLine()
        .text("Step defines for how much price will", "grow up/down on each sale/purchase.")
        .emptyLine()
        .text("Purchases = Price Up, Sales = Price Down")
        .emptyLine()
        .leftClick("change buy")
        .rightClick("change sell")
        .build();




    public static final LangItem PRODUCT_PRICE_PLAYERS_INITIAL = LangItem.builder(PREFIX + "Product.Price.Players.Initial")
        .name("Initial Price")
        .current("Buy", PRODUCT_PRICER_DYNAMIC_INITIAL_BUY)
        .current("Sell", PRODUCT_PRICER_DYNAMIC_INITIAL_SELL)
        .emptyLine()
        .text("Sets initial product price.", "These values will be used as default/start ones.")
        .emptyLine()
        .leftClick("change buy")
        .rightClick("change sell")
        .build();

    public static final LangItem PRODUCT_PRICE_PLAYERS_ADJUST = LangItem.builder(PREFIX + "Product.Price.Players.Adjust")
        .name("Price Adjust")
        .current("Buy", PRODUCT_PRICER_PLAYERS_ADJUST_AMOUNT_BUY)
        .current("Sell", PRODUCT_PRICER_PLAYERS_ADJUST_AMOUNT_SELL)
        .emptyLine()
        .text("Defines for how much price will", "be adjusted for each X online players.")
        .emptyLine()
        .leftClick("change buy")
        .rightClick("change sell")
        .build();

    public static final LangItem PRODUCT_PRICE_PLAYERS_STEP = LangItem.builder(PREFIX + "Product.Price.Players.Step")
        .name("Adjust Step")
        .current("Current", PRODUCT_PRICER_PLAYERS_ADJUST_STEP)
        .emptyLine()
        .text("Sets amount of online players", "to adjust the price for.")
        .emptyLine()
        .text(LIGHT_YELLOW.enclose(BOLD.enclose("Examples:")))
        .text(LIGHT_YELLOW.enclose("1") + " = for " + LIGHT_YELLOW.enclose("every player") + " online.")
        .text(LIGHT_YELLOW.enclose("5") + " = for " + LIGHT_YELLOW.enclose("every 5") + " players (5, 10, 15, etc.)")
        .emptyLine()
        .click("change")
        .build();

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
        .shiftRight("delete " + LIGHT_RED.enclose("(no undo)"))
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
