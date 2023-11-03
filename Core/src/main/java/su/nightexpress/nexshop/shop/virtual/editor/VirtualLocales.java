package su.nightexpress.nexshop.shop.virtual.editor;

import su.nexmedia.engine.api.editor.EditorLocale;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.nexshop.shop.virtual.impl.shop.RotationType;
import su.nightexpress.nexshop.shop.virtual.util.Placeholders;

import static su.nexmedia.engine.utils.Colors2.*;

public class VirtualLocales extends su.nexmedia.engine.api.editor.EditorLocales {

    private static final String PREFIX = "VirtualShop.Editor.";

    public static final EditorLocale SHOP_CREATE = builder(PREFIX + "Shop.Create")
        .name("New Shop")
        .breakLine()
        .text("(" + WHITE + LMB + GRAY + " to create static shop)")
        .text("(" + WHITE + RMB + GRAY + " to create rotating shop)")
        .build();

    public static final EditorLocale SHOP_OBJECT = builder(PREFIX + "Shop.Object")
        .name(Placeholders.SHOP_NAME)
        .currentHeader()
        .current("Type", Placeholders.SHOP_TYPE)
        .current("Pages", Placeholders.SHOP_PAGES)
        .breakLine()
        .text("(" + WHITE + LMB + GRAY + " to edit)")
        .text("(" + WHITE + "Shift-Right" + GRAY + " to delete" + RED + " (no undo)")
        .build();

    public static final EditorLocale SHOP_NAME = builder(PREFIX + "Shop.DisplayName")
        .name("Display Name")
        .text("General shop name, which", "is " + RED + "not" + GRAY + " related to GUI title.").breakLine()
        .currentHeader()
        .current("Name", Placeholders.SHOP_NAME + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .build();

    public static final EditorLocale SHOP_DESCRIPTION = builder(PREFIX + "Shop.Description")
        .name("Description")
        .text("Describe what items this shop trades.").breakLine()
        .currentHeader()
        .text(Placeholders.SHOP_DESCRIPTION).breakLine()
        .text("(" + WHITE + LMB + GRAY + " to add line)")
        .text("(" + WHITE + RMB + GRAY + " to remove all)")
        .build();

    public static final EditorLocale SHOP_PAGES = builder(PREFIX + "Shop.Pages")
        .name("Pages Amount")
        .text("Amount of pages in the shop.").breakLine()
        .text(ORANGE + "[!]" + GRAY + " Make sure to add " + ORANGE + "page buttons")
        .text("in " + ORANGE + "View Editor" + GRAY + "!").breakLine()
        .currentHeader()
        .current("Pages", Placeholders.SHOP_PAGES + GRAY + " (" + WHITE + "LMB +1" + GRAY + " | " + WHITE + "RMB -1" + GRAY + ")")
        .build();

    public static final EditorLocale SHOP_ICON = builder(PREFIX + "Shop.Icon")
        .name("Icon")
        .text("Item used to display shop", "in various GUIs.").breakLine()
        .text("(" + WHITE + "Drag & Drop" + GRAY + " to replace)")
        .text("(" + WHITE + RMB + GRAY + " to get a copy)")
        .build();

    public static final EditorLocale SHOP_PERMISSION = builder(PREFIX + "Shop.PermissionRequirement")
        .name("Permission Requirement")
        .text("Sets whether or not permission", "is required to use this shop.").breakLine()
        .currentHeader()
        .current("Enabled", Placeholders.SHOP_PERMISSION_REQUIRED + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Node", Placeholders.SHOP_PERMISSION_NODE)
        .build();

    public static final EditorLocale SHOP_TRADES = builder(PREFIX + "Shop.Transactions")
        .name("Transactions")
        .text("Global rules allowing / disallowing")
        .text("selling and buying in this shop.")
        .breakLine()
        .currentHeader()
        .current("Buying Enabled", Placeholders.SHOP_BUY_ALLOWED + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Selling Enabled", Placeholders.SHOP_SELL_ALLOWED + GRAY + " (" + WHITE + RMB + GRAY + ")")
        .build();

    public static final EditorLocale SHOP_ATTACHED_NPCS = builder(PREFIX + "Shop.AttachedNPCs")
        .name("Attached NPCs")
        .text("List of NPCs (id) attached to this shop", "to open it on interaction.")
        .text(RED + "(Citizens plugin required)")
        .breakLine()
        .currentHeader().text(Placeholders.SHOP_NPC_IDS).breakLine()
        .text("(" + WHITE + LMB + GRAY + " to add)")
        .text("(" + WHITE + RMB + GRAY + " to remove all)")
        .build();

    public static final EditorLocale SHOP_VIEW_EDITOR = builder(PREFIX + "Shop.ViewEditor")
        .name("View Editor")
        .text("Here you can customize and decorate", "GUI layout for this shop.").breakLine()
        .currentHeader()
        .current("Layout", WHITE + LMB)
        .current("Title", Placeholders.SHOP_VIEW_TITLE + GRAY + " (" + WHITE + "Shift-Left" + GRAY + ")")
        .current("Size", Placeholders.SHOP_VIEW_SIZE + GRAY + " (" + WHITE + "Shift-Right" + GRAY + ")")
        .build();

    public static final EditorLocale SHOP_DISCOUNTS = builder(PREFIX + "Shop.Discounts")
        .name("Discounts")
        .text("Create and manage shop discounts here!")
        .build();

    public static final EditorLocale SHOP_PRODUCTS = builder(PREFIX + "Shop.Products")
        .name("Products")
        .text("Create and manage products here.").breakLine()
        .text("(" + WHITE + LMB + GRAY + " to navigate)")
        .text("(" + WHITE + DROP_KEY + GRAY + " to reset & update all prices)")
        .text("(" + WHITE + SWAP_KEY + GRAY + " to reset all stocks & limits)")
        .build();

    public static final EditorLocale SHOP_ROTATION_TYPE = builder(PREFIX + "Shop.Rotation.Type")
        .name("Rotation Type")
        .text(YELLOW + RotationType.INTERVAL.name() + GRAY + " = Every X minutes.")
        .text(YELLOW + RotationType.FIXED.name() + GRAY + " = Strictly at specified times.")
        .breakLine()
        .currentHeader()
        .current("Type", Placeholders.SHOP_ROTATION_TYPE + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .build();

    public static final EditorLocale SHOP_ROTATION_INTERVAL = builder(PREFIX + "Shop.Rotation.Interval")
        .name("Rotation Interval")
        .text("Sets how often (in seconds) shop", "products will be rotated.").breakLine()
        .currentHeader()
        .current("Interval", Placeholders.SHOP_ROTATION_INTERVAL + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to force rotate)")
        .build();

    public static final EditorLocale SHOP_ROTATION_TIMES = builder(PREFIX + "Shop.Rotation.Times")
        .name("Rotation Times")
        .text("Here you can define rotation", "times for each day of week.").breakLine()
        .text("(" + WHITE + LMB + GRAY + " to navigate)")
        .build();

    public static final EditorLocale SHOP_ROTATION_PRODUCTS = builder(PREFIX + "Shop.Rotation.Products")
        .name("Rotation Products")
        .text("Sets how many products", "will be used in rotations and", "slots where they will appear.").breakLine()
        .currentHeader()
        .current("Min", Placeholders.SHOP_ROTATION_MIN_PRODUCTS + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Max", Placeholders.SHOP_ROTATION_MAX_PRODUCTS + GRAY + " (" + WHITE + RMB + GRAY + ")")
        .current("Slots", Placeholders.SHOP_ROTATION_PRODUCT_SLOTS + GRAY + " (" + WHITE + DROP_KEY + GRAY + ")")
        .build();

    public static final EditorLocale PRODUCT_OBJECT = builder(PREFIX + "Product.Object.Static")
        .name(Placeholders.PRODUCT_PREVIEW_NAME)
        .text("You can freely move this product", "between slots, pages, and shops!").breakLine()
        .text(YELLOW + BOLD + "Info:")
        .current("Currency", Placeholders.PRODUCT_CURRENCY)
        .current("Buy", Placeholders.PRODUCT_PRICE_BUY)
        .current("Sell", Placeholders.PRODUCT_PRICE_SELL).breakLine()
        .text("(" + WHITE + "Shift-Left" + GRAY + " to edit)")
        .text("(" + WHITE + "Shift-Right" + GRAY + " to delete" + RED + " (no undo)")
        .build();

    public static final EditorLocale ROTATING_PRODUCT_OBJECT = builder(PREFIX + "Product.Object.Rotating")
        .name(Placeholders.PRODUCT_PREVIEW_NAME)
        .currentHeader()
        .current("Rotation Chance", Placeholders.PRODUCT_ROTATION_CHANCE + "%")
        .current("Currency", Placeholders.PRODUCT_CURRENCY)
        .current("Buy Price", Placeholders.PRODUCT_PRICE_BUY)
        .current("Sell Price", Placeholders.PRODUCT_PRICE_SELL).breakLine()
        .text("(" + WHITE + "Shift-Left" + GRAY + " to edit)")
        .text("(" + WHITE + "Shift-Right" + GRAY + " to delete" + RED + " (no undo)")
        .build();

    public static final EditorLocale PRODUCT_FREE_SLOT = builder(PREFIX + "Product.FreeSlot")
        .name(GREEN + BOLD + "Free Slot")
        .text("This slot can be filled with product!").breakLine()
        .text("(" + WHITE + "Drag & Drop" + GRAY + " to create item product)")
        .text("(" + WHITE + RMB + GRAY + " to create command product")
        .build();

    public static final EditorLocale PRODUCT_RESERVED_SLOT = builder(PREFIX + "Product.ReservedSlot")
        .name(RED + BOLD + "Reserved Slot")
        .text("This slot is occupied by a shop product.")
        .build();

    public static final EditorLocale PRODUCT_PRICE_MANAGER = builder(PREFIX + "Product.PriceManager")
        .name("Price Manager")
        .text("Sets product currency and price.").breakLine()
        .currentHeader()
        .current("Type", Placeholders.PRODUCT_PRICE_TYPE)
        .current("Currency", Placeholders.PRODUCT_CURRENCY + GRAY + " (" + WHITE + RMB + GRAY + ")")
        .current("Buy", Placeholders.PRODUCT_PRICE_BUY)
        .current("Sell", Placeholders.PRODUCT_PRICE_SELL)
        .breakLine()
        .text("(" + WHITE + LMB + GRAY + " to change settings)")
        .text("(" + WHITE + DROP_KEY + GRAY + " to refresh prices)")
        .build();

    public static final EditorLocale PRODUCT_ROTATION_CHANCE = builder(PREFIX + "Product.Rotation.Chance")
        .name("Rotation Chance")
        .text("A chance that this product", "will appear in next shop rotation.").breakLine()
        .currentHeader()
        .current("Chance", Placeholders.PRODUCT_ROTATION_CHANCE + "%" + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .build();

    public static final EditorLocale PRODUCT_ROTATION_DAY_TIMES = builder(PREFIX + "Product.Rotation.DayTimes")
        .name(Placeholders.GENERIC_NAME)
        .text(Placeholders.GENERIC_TIME).breakLine()
        .text("(" + WHITE + LMB + GRAY + " to add)")
        .text("(" + WHITE + RMB + GRAY + " to remove all)")
        .build();

    public static final EditorLocale PRODUCT_ITEM = builder(PREFIX + "Product.Item")
        .name("Actual Item")
        .text("This item will be " + GREEN + "given" + GRAY + " to players", "on buying and " + RED + "taken" + GRAY + " on selling.")
        .breakLine()
        .text("(" + WHITE + "Drag & Drop" + GRAY + " to replace)")
        .text("(" + WHITE + RMB + GRAY + " to get copy)")
        .build();

    public static final EditorLocale PRODUCT_RESPECT_ITEM_META = builder(PREFIX + "Product.RespectItemMeta")
        .name("Respect Item Meta")
        .text("When " + GREEN + "enabled" + GRAY + ", players can sell only", "exact items comparing to " + WHITE + "Actual Item" + GRAY + ".").breakLine()
        .text("When " + RED + "disabled" + GRAY + ", players can sell any", "item of the same type as " + WHITE + "Actual Item" + GRAY + ".").breakLine()
        .currentHeader()
        .current("Enabled", Placeholders.PRODUCT_ITEM_META_ENABLED + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .build();

    public static final EditorLocale PRODUCT_PREVIEW = builder(PREFIX + "Product.Preview")
        .name("Preview Item")
        .text("This item will be " + YELLOW + "displayed" + GRAY + " in shop GUIs", "instead of the " + WHITE + "Actual Item" + GRAY + ".")
        .breakLine()
        .text("(" + WHITE + "Drag & Drop" + GRAY + " to replace)")
        .text("(" + WHITE + RMB + GRAY + " to get copy)")
        .build();

    public static final EditorLocale PRODUCT_COMMANDS = builder(PREFIX + "Product.Commands")
        .name("Commands")
        .text("All commands listed below will", "be runned from " + WHITE + "console", "when player purchases this product.").breakLine()
        .currentHeader()
        .text(Placeholders.PRODUCT_COMMANDS)
        .breakLine()
        .text(YELLOW + BOLD + "Placeholders:")
        .current(Placeholders.PLAYER_NAME, "Player (buyer) name.")
        .current(EngineUtils.PLACEHOLDER_API, "All of them.")
        .breakLine()
        .text("(" + WHITE + LMB + GRAY + " to add command)")
        .text("(" + WHITE + RMB + GRAY + " to remove all)")
        .build();

    public static final EditorLocale PRODUCT_ALLOWED_RANKS = builder(PREFIX + "Product.Allowed_Ranks")
        .name("Allowed Ranks")
        .text("List of ranks (permission groups) which", "can access this product.")
        .text("(leave it empty to disable)")
        .breakLine()
        .currentHeader()
        .text(Placeholders.PRODUCT_ALLOWED_RANKS).breakLine()
        .text("(" + WHITE + LMB + GRAY + " to add rank)")
        .text("(" + WHITE + RMB + GRAY + " to remove all)")
        .build();

    public static final EditorLocale PRODUCT_DISCOUNT = builder(PREFIX + "Product.DiscountAllowed")
        .name("Discount Allowed")
        .text("Sets whether or not this product", "can be affected by shop's discounts.").breakLine()
        .currentHeader()
        .current("Enabled", Placeholders.PRODUCT_DISCOUNT_ALLOWED + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .build();

    public static final EditorLocale PRODUCT_GLOBAL_STOCK = builder(PREFIX + "Product.Stock.Global")
        .name("Global Stock")
        .text("Sets how many of the product will be available", "for " + WHITE + "all" + GRAY + " players at the same time.")
        .text("(amount changed on each transaction)")
        .breakLine()
        .text("Set initial to " + WHITE + "-1" + GRAY + " for unlimited.")
        .text("Set restock to " + WHITE + "-1" + GRAY + " to disable.")
        .breakLine()
        .currentHeader()
        .current("Buy Initial", Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_INITIAL + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Buy Restock", Placeholders.PRODUCT_STOCK_GLOBAL_BUY_RESTOCK_TIME + GRAY + " (" + WHITE + RMB + GRAY + ")")
        .current("Sell Initial", Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_INITIAL + GRAY + " (" + WHITE + "Shift-Left" + GRAY + ")")
        .current("Sell Restock", Placeholders.PRODUCT_STOCK_GLOBAL_SELL_RESTOCK_TIME + GRAY + " (" + WHITE + "Shift-Right" + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to disable all)")
        .build();

    public static final EditorLocale PRODUCT_PLAYER_STOCK = builder(PREFIX + "Product.Stock.Player")
        .name("Player Limits")
        .text("Sets how many of the product will be available", "for " + WHITE + "each" + GRAY + " player individually.")
        .text("(amount changed on each transaction)")
        .breakLine()
        .text("Set limit to " + WHITE + "-1" + GRAY + " for unlimited.")
        .text("Set restock to " + WHITE + "-1" + GRAY + " to disable.")
        .breakLine()
        .currentHeader()
        .current("Buy Limit", Placeholders.PRODUCT_STOCK_PLAYER_BUY_AMOUNT_INITIAL + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Buy Restock", Placeholders.PRODUCT_STOCK_PLAYER_BUY_RESTOCK_TIME + GRAY + " (" + WHITE + RMB + GRAY + ")")
        .current("Sell Limit", Placeholders.PRODUCT_STOCK_PLAYER_SELL_AMOUNT_INITIAL + GRAY + " (" + WHITE + "Shift-Left" + GRAY + ")")
        .current("Sell Restock", Placeholders.PRODUCT_STOCK_PLAYER_SELL_RESTOCK_TIME + GRAY + " (" + WHITE + "Shift-Right" + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to disable all)")
        .build();

    public static final EditorLocale PRODUCT_PRICE_TYPE = builder(PREFIX + "Product.Price.Type")
        .name("Price Type")
        .text("Sets product price type.", "Different types have different settings.").breakLine()
        .currentHeader()
        .current("Current", Placeholders.PRODUCT_PRICE_TYPE + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLAT_BUY = builder(PREFIX + "Product.Price.Flat.Buy")
        .name("Buy Price")
        .current("Current", Placeholders.PRODUCT_PRICE_BUY + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to disable)")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLAT_SELL = builder(PREFIX + "Product.Price.Flat.Sell")
        .name("Sell Price")
        .current("Current", Placeholders.PRODUCT_PRICE_SELL + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to disable)")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLOAT_BUY = builder(PREFIX + "Product.Price.Float.Buy")
        .name("Buy Price")
        .text("Sets product buy price bounds.", "Final price will be within these values.").breakLine()
        .currentHeader()
        .current("Min", Placeholders.PRODUCT_PRICER_BUY_MIN + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Max", Placeholders.PRODUCT_PRICER_BUY_MAX + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to disable)")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLOAT_SELL = builder(PREFIX + "Product.Price.Float.Sell")
        .name("Sell Price")
        .text("Sets product sell price bounds.", "Final price will be within these values.").breakLine()
        .currentHeader()
        .current("Min", Placeholders.PRODUCT_PRICER_SELL_MIN + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Max", Placeholders.PRODUCT_PRICER_SELL_MAX + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to disable)")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLOAT_REFRESH = builder(PREFIX + "Product.Price.Float.Refresh")
        .name("Refresh Settings")
        .text("Product price will be auto generated", "in specified days and times below.")
        .text("Until that, previously generated price will be used.").breakLine()
        .currentHeader()
        .current("Days", Placeholders.PRODUCT_PRICER_FLOAT_REFRESH_DAYS)
        .current("Times", Placeholders.PRODUCT_PRICER_FLOAT_REFRESH_TIMES)
        .breakLine()
        .text("(" + WHITE + LMB + GRAY + " to add day)")
        .text("(" + WHITE + RMB + GRAY + " to add time)")
        .text("(" + WHITE + "Shift-Left" + GRAY + " to clear days)")
        .text("(" + WHITE + "Shift-Right" + GRAY + " to clear times)")
        .build();

    public static final EditorLocale PRODUCT_PRICE_DYNAMIC_BUY = builder(PREFIX + "Product.Price.Dynamic.Buy")
        .name("Buy Price")
        .text("Sets product buy price bounds.", "Final price will be within these values.").breakLine()
        .currentHeader()
        .current("Min", Placeholders.PRODUCT_PRICER_BUY_MIN + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Max", Placeholders.PRODUCT_PRICER_BUY_MAX + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to disable)")
        .build();

    public static final EditorLocale PRODUCT_PRICE_DYNAMIC_SELL = builder(PREFIX + "Product.Price.Dynamic.Sell")
        .name("Sell Price")
        .text("Sets product sell price bounds.", "Final price will be within these values.").breakLine()
        .currentHeader()
        .current("Min", Placeholders.PRODUCT_PRICER_SELL_MIN + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Max", Placeholders.PRODUCT_PRICER_SELL_MAX + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .breakLine()
        .text("(" + WHITE + DROP_KEY + GRAY + " to disable)")
        .build();

    public static final EditorLocale PRODUCT_PRICE_DYNAMIC_INITIAL = builder(PREFIX + "Product.Price.Dynamic.Initial")
        .name("Initial Price")
        .text("Sets initial product price.", "These values will be used as default/start ones.").breakLine()
        .currentHeader()
        .current("Buy", Placeholders.PRODUCT_PRICER_DYNAMIC_INITIAL_BUY + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Sell", Placeholders.PRODUCT_PRICER_DYNAMIC_INITIAL_SELL + GRAY + " (" + WHITE + RMB + GRAY + ")")
        .build();

    public static final EditorLocale PRODUCT_PRICE_DYNAMIC_STEP = builder(PREFIX + "Product.Price.Dynamic.Step")
        .name("Price Step")
        .text("Step defines for how much price will", "grow up/down on each sale/purchase.").breakLine()
        .text("Purchases = Price Up, Sales = Price Down").breakLine()
        .currentHeader()
        .current("Buy", Placeholders.PRODUCT_PRICER_DYNAMIC_STEP_BUY + GRAY + " (" + WHITE + LMB + GRAY + ")")
        .current("Sell", Placeholders.PRODUCT_PRICER_DYNAMIC_STEP_SELL + GRAY + " (" + WHITE + RMB + GRAY + ")")
        .build();

    public static final EditorLocale DISCOUNT_CREATE = builder(PREFIX + "Discount.Create")
        .name("New Discount")
        .build();

    public static final EditorLocale DISCOUNT_OBJECT = builder(PREFIX + "Discount.Object")
        .name("Discount")
        .currentHeader()
        .current("Amount", Placeholders.DISCOUNT_CONFIG_AMOUNT)
        .current("Days", Placeholders.DISCOUNT_CONFIG_DAYS)
        .current("Times", Placeholders.DISCOUNT_CONFIG_TIMES).breakLine()
        .actionsHeader().action("Left-Click", "Edit")
        .action("Shift-Right", "Delete " + RED + "(No Undo)")
        .build();

    public static final EditorLocale DISCOUNT_AMOUNT = builder(PREFIX + "Discount.Amount")
        .name("Amount")
        .text("Sets the discount amount (in percent).").breakLine()
        .currentHeader().current("Amount", Placeholders.DISCOUNT_CONFIG_AMOUNT + "%").breakLine()
        .actionsHeader().action("Left-Click", "Change")
        .build();

    public static final EditorLocale DISCOUNT_DURATION = builder(PREFIX + "Discount.Duration")
        .name("Duration")
        .text("Sets how long (in seconds) this", "discount will be active.").breakLine()
        .currentHeader().current("Duration", Placeholders.DISCOUNT_CONFIG_DURATION).breakLine()
        .actionsHeader().action("Left-Click", "Change")
        .build();

    public static final EditorLocale DISCOUNT_DAYS = builder( PREFIX + "Discount.Days")
        .name("Active Days")
        .text("A list of days, when this discount will have effect.").breakLine()
        .currentHeader().current("Days", Placeholders.DISCOUNT_CONFIG_DAYS).breakLine()
        .noteHeader().notes("At least one " + ORANGE + "day" + GRAY + " and " + ORANGE + "time" + GRAY + " are required!").breakLine()
        .actionsHeader().action("Left-Click", "Add Day").action("Right-Click", "Clear List")
        .build();

    public static final EditorLocale DISCOUNT_TIMES = builder(PREFIX + "Discount.Times")
        .name("Active Times")
        .text("A list of times, when this discount will be activated.").breakLine()
        .currentHeader().current("Days", Placeholders.DISCOUNT_CONFIG_TIMES).breakLine()
        .noteHeader().notes("At least one " + ORANGE + "day" + GRAY + " and " + ORANGE + "time" + GRAY + " are required!").breakLine()
        .actionsHeader().action("Left-Click", "Add Time").action("Right-Click", "Clear List")
        .build();
}
