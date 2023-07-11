package su.nightexpress.nexshop.shop.virtual.editor;

import su.nexmedia.engine.api.editor.EditorLocale;
import su.nightexpress.nexshop.Placeholders;

public class VirtualLocales extends su.nexmedia.engine.api.editor.EditorLocales {

    private static final String PREFIX_OLD = "VirtualEditorType.Editor.";
    private static final String PREFIX = "VirtualShop.Editor.";

    public static final EditorLocale SHOP_CREATE = builder(PREFIX_OLD + "SHOP_CREATE")
        .name("Create Shop")
        .text("Create a new Virtual Shop.").breakLine()
        .actionsHeader().action("Left-Click", "Create")
        .build();

    public static final EditorLocale SHOP_OBJECT = builder(PREFIX_OLD + "SHOP_OBJECT")
        .name(Placeholders.SHOP_NAME)
        .currentHeader().current("Pages", Placeholders.SHOP_VIRTUAL_PAGES).breakLine()
        .actionsHeader().action("Left-Click", "Edit").action("Shift-Right", "Delete" + RED + " (No Undo)")
        .build();

    public static final EditorLocale SHOP_NAME = builder(PREFIX_OLD + "SHOP_CHANGE_NAME")
        .name("Display Name")
        .currentHeader().current("Name", Placeholders.SHOP_NAME).breakLine()
        .text("Sets the shop display name.", "It's used in GUIs, messages, etc.").breakLine()
        .noteHeader().notes("This is" + ORANGE + " NOT" + GRAY + " shop GUI title!").breakLine()
        .actionsHeader().action("Left-Click", "Change")
        .build();

    public static final EditorLocale SHOP_DESCRIPTION = builder(PREFIX_OLD + "SHOP_CHANGE_DESCRIPTION")
        .name("Description")
        .currentHeader().text(Placeholders.SHOP_VIRTUAL_DESCRIPTION).breakLine()
        .text("Sets the shop description.", "It can be used in main shops menu.").breakLine()
        .actionsHeader().action("Left-Click", "Add Line").action("Right-Click", "Clear List")
        .build();

    public static final EditorLocale SHOP_PAGES = builder(PREFIX_OLD + "SHOP_CHANGE_PAGES")
        .name("Pages Amount")
        .currentHeader().current("Pages", Placeholders.SHOP_VIRTUAL_PAGES).breakLine()
        .text("Sets amount of pages for the shop.").breakLine()
        .noteHeader().notes("Don't forget to add page items in " + YELLOW + "View Editor" + GRAY + "!").breakLine()
        .actionsHeader().action("Left-Click", "+1 Page").action("Right-Click", "-1 Page")
        .build();

    public static final EditorLocale SHOP_ICON = builder(PREFIX + "Shop.Icon")
        .name("Icon")
        .text("Sets the shop icon to be used in GUIs.").breakLine()
        .noteHeader().notes("Instead of name & lore, " + ORANGE + "Display Name" + GRAY + " and " + ORANGE + "Description" + GRAY + " are used.").breakLine()
        .actionsHeader().action("Drag & Drop", "Replace").action("Right-Click", "Get a Copy")
        .build();

    public static final EditorLocale SHOP_PERMISSION = builder(PREFIX_OLD + "SHOP_CHANGE_PERMISSION")
        .name("Permission Requirement")
        .currentHeader().current("Enabled", Placeholders.SHOP_VIRTUAL_PERMISSION_REQUIRED)
        .current("Node", Placeholders.SHOP_VIRTUAL_PERMISSION_NODE).breakLine()
        .text("Sets whether players must have permission", "in order to access/use this shop.").breakLine()
        .actionsHeader().action("Left-Click", "Toggle")
        .build();

    public static final EditorLocale SHOP_TRADES = builder(PREFIX_OLD + "SHOP_CHANGE_TRANSACTIONS")
        .name("Transactions")
        .currentHeader().current("Buying Enabled", Placeholders.SHOP_BUY_ALLOWED)
        .current("Selling Enabled", Placeholders.SHOP_SELL_ALLOWED).breakLine()
        .text("Sets whether " + YELLOW + "buying" + GRAY + " and/or " + YELLOW + "selling" + GRAY + " are", "enabled in this shop.").breakLine()
        .actionsHeader().action("Left-Click", "Toggle Buying").action("Right-Click", "Toggle Selling")
        .build();

    public static final EditorLocale SHOP_ATTACHED_NPCS = builder(PREFIX_OLD + "SHOP_CHANGE_CITIZENS_ID")
        .name("Attached NPCs")
        .currentHeader().current("NPC IDs", Placeholders.SHOP_VIRTUAL_NPC_IDS).breakLine()
        .text("A list of NPC Ids attached to this shop.", "Those NPCs will open shop GUI on click.").breakLine()
        .warningHeader().warning("You must have " + RED + "Citizens " + GRAY + "installed!").breakLine()
        .actionsHeader().action("Left-Click", "Add ID").action("Right-Click", "Clear List")
        .build();

    public static final EditorLocale SHOP_VIEW_EDITOR = builder(PREFIX_OLD + "SHOP_CHANGE_VIEW_DESIGN")
        .name("View Editor")
        .currentHeader().current("Title", Placeholders.SHOP_VIRTUAL_VIEW_TITLE)
        .current("Size", Placeholders.SHOP_VIRTUAL_VIEW_SIZE).breakLine()
        .text("Sets " + YELLOW + "title" + GRAY + " & " + YELLOW + "size" + GRAY + " for the shop GUI.", "Here you can create custom")
        .text("GUI layout for this shop.", "Simply place items in editor and press " + YELLOW + "ESC" + GRAY + " to save.").breakLine()
        .actionsHeader().action("Left-Click", "Open Editor").action("Shift-Left", "Change Title")
        .action("Shift-Right", "Change Size")
        .build();

    public static final EditorLocale SHOP_DISCOUNTS = builder(PREFIX_OLD + "SHOP_CHANGE_DISCOUNTS")
        .name("Discounts")
        .text("Create and manage shop discounts here!").breakLine()
        .actionsHeader().action("Left-Click", "Navigate")
        .build();

    public static final EditorLocale SHOP_PRODUCTS = builder(PREFIX_OLD + "SHOP_CHANGE_PRODUCTS")
        .name("Products")
        .text("Create and manage shop products here!").breakLine()
        .actionsHeader().action("Left-Click", "Navigate")
        .build();

    public static final EditorLocale PRODUCT_OBJECT = builder(PREFIX + "Product.Object")
        .name(Placeholders.PRODUCT_PREVIEW_NAME)
        .text("You can take & put this product in any other", "slot. Or store it in your inventory to", "add it in other pages or even shops!")
        .text("All product settings will be " + GREEN + "saved" + GRAY + "!").breakLine()
        .text(YELLOW + "&lCurrent Price:")
        .current("Buy", Placeholders.PRODUCT_PRICE_BUY)
        .current("Sell", Placeholders.PRODUCT_PRICE_SELL).breakLine()
        .actionsHeader().action("Shift-Left", "Edit").action("Shift-Right", "Delete" + RED + " (No Undo)")
        .build();

    public static final EditorLocale PRODUCT_FREE_SLOT = builder(PREFIX + "Product.FreeSlot")
        .name(GREEN + "&lFree Slot")
        .actionsHeader().action("Drag & Drop", "Add Item Product").action("Right-Click", "Add Command Product")
        .build();

    public static final EditorLocale PRODUCT_RESERVED_SLOT = builder(PREFIX + "Product.ReservedSlot")
        .name(RED + "&lReserved Slot")
        .text("This slot is occupied by a shop product.")
        .build();

    public static final EditorLocale PRODUCT_PRICE_MANAGER = builder("VirtualShop.Editor.Product.PriceManager")
        .name("Price Manager")
        .currentHeader().current("Currency", Placeholders.PRODUCT_CURRENCY)
        .current("Buy", Placeholders.PRODUCT_PRICE_BUY).current("Sell", Placeholders.PRODUCT_PRICE_SELL).breakLine()
        .text("Sets the product currency and", "price settings.").breakLine()
        .actionsHeader().action("Left-Click", "Change Prices").action("Right-Click", "Change Currency")
        .action("[Q/Drop] Key", "Refresh Price")
        .build();

    public static final EditorLocale PRODUCT_ITEM = builder(PREFIX_OLD + "PRODUCT_CHANGE_ITEM")
        .name("Actual Item")
        .text("Sets the actual product item to sell/purchase.").breakLine()
        .actionsHeader().action("Drag & Drop", "Replace").action("Right-Click", "Get Copy")
        .build();

    public static final EditorLocale PRODUCT_RESPECT_ITEM_META = builder("VirtualShop.Editor.Product.RespectItemMeta")
        .name("Respect Item Meta")
        .currentHeader().current("Enabled", Placeholders.PRODUCT_ITEM_META_ENABLED).breakLine()
        .text("Sets whether product should respect", "meta of the " + YELLOW + "Actual Item" + GRAY + ".")
        .text("This means, players will be able to sell", "similar (exact) items only.").breakLine()
        .noteHeader().notes("Enable this for custom items!").breakLine()
        .actionsHeader().action("Left-Click", "Toggle")
        .build();

    public static final EditorLocale PRODUCT_PREVIEW = builder(PREFIX_OLD + "PRODUCT_CHANGE_PREVIEW")
        .name("Preview Item")
        .text("Sets the preview item to display in Shop GUI.").breakLine()
        .noteHeader().notes("Use item with premade name, lore for best results.").breakLine()
        .actionsHeader().action("Drag & Drop", "Replace").action("Right-Click", "Get Copy")
        .build();

    public static final EditorLocale PRODUCT_COMMANDS = builder(PREFIX_OLD + "PRODUCT_CHANGE_COMMANDS")
        .name("Commands")
        .currentHeader().text(Placeholders.PRODUCT_VIRTUAL_COMMANDS).breakLine()
        .text("A list of commands to execute when", "player purchases this product.").breakLine()
        .noteHeader().notes("Use " + ORANGE + Placeholders.PLAYER_NAME + GRAY + " for player name.").breakLine()
        .actionsHeader().action("Left-Click", "Add Command").action("Right-Click", "Clear List")
        .build();

    public static final EditorLocale PRODUCT_DISCOUNT = builder(PREFIX_OLD + "PRODUCT_CHANGE_DISCOUNT")
        .name("Discount Allowed")
        .currentHeader().current("Enabled", Placeholders.PRODUCT_DISCOUNT_ALLOWED).breakLine()
        .text("Sets whether this product's price", "can be affected by shop's discounts.").breakLine()
        .actionsHeader().action("Left-Click", "Toggle")
        .build();

    public static final EditorLocale PRODUCT_GLOBAL_STOCK = builder(PREFIX_OLD + "PRODUCT_CHANGE_STOCK_GLOBAL")
        .name("Global Stock")
        .currentHeader()
        .current("Buy Initial", Placeholders.PRODUCT_STOCK_GLOBAL_BUY_AMOUNT_INITIAL)
        .current("Buy Auto-Restock", Placeholders.PRODUCT_STOCK_GLOBAL_BUY_RESTOCK_TIME)
        .current("Sell Initial", Placeholders.PRODUCT_STOCK_GLOBAL_SELL_AMOUNT_INITIAL)
        .current("Sell Auto-Restock", Placeholders.PRODUCT_STOCK_GLOBAL_SELL_RESTOCK_TIME).breakLine()
        .text("Sets how many of the product will be available", "for " + YELLOW + "all" + GRAY + " players at the same time.")
        .text("Amount of product left for", "sell/purchase will be changed on each transaction.").breakLine()
        .noteHeader().notes("-1 for initial = " + ORANGE + "unlimited", "-1 for restock = " + ORANGE + "never" + GRAY + " auto-restock.").breakLine()
        .actionsHeader()
        .action("Left-Click", "Change Buy Initial").action("Right-Click", "Change Buy Restock")
        .action("Shift-Left", "Change Sell Initial").action("Shift-Right", "Change Sell Restock")
        .action("[Q/Drop] Key", "Disable All")
        .build();

    public static final EditorLocale PRODUCT_PLAYER_STOCK = builder(PREFIX_OLD + "PRODUCT_CHANGE_STOCK_PLAYER")
        .name("Player Limits")
        .currentHeader()
        .current("Buy Limit", Placeholders.PRODUCT_STOCK_PLAYER_BUY_AMOUNT_INITIAL)
        .current("Buy Auto-Restock", Placeholders.PRODUCT_STOCK_PLAYER_BUY_RESTOCK_TIME)
        .current("Sell Limit", Placeholders.PRODUCT_STOCK_PLAYER_SELL_AMOUNT_INITIAL)
        .current("Sell Auto-Restock", Placeholders.PRODUCT_STOCK_PLAYER_SELL_RESTOCK_TIME).breakLine()
        .text("Sets how many of the product will be available", "for " + YELLOW + "each" + GRAY + " player individually.")
        .text("Amount of product left for", "sell/purchase will be changed on each transaction.").breakLine()
        .noteHeader().notes("-1 for limit = " + ORANGE + "unlimited", "-1 for restock = " + ORANGE + "never" + GRAY + " auto-restock.").breakLine()
        .actionsHeader()
        .action("Left-Click", "Change Buy Limit").action("Right-Click", "Change Buy Restock")
        .action("Shift-Left", "Change Sell Limit").action("Shift-Right", "Change Sell Restock")
        .action("[Q/Drop] Key", "Disable All")
        .build();

    public static final EditorLocale PRODUCT_PRICE_TYPE = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_TYPE")
        .name("Price Type")
        .currentHeader().current("Current", Placeholders.PRODUCT_PRICE_TYPE).breakLine()
        .text("Sets the product price type.", "Different types have different settings.").breakLine()
        .warningHeader().warning("When changed, previous settings will be " + RED + "lost" + GRAY + "!").breakLine()
        .actionsHeader().action("Left-Click", "Toggle")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLAT_BUY = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_FLAT_BUY")
        .name("Buy Price")
        .currentHeader().current("Current", Placeholders.PRODUCT_PRICE_BUY).breakLine()
        .text("Sets the product buy price.").breakLine()
        .noteHeader().notes("Negative value makes product unbuyable.").breakLine()
        .actionsHeader().action("Left-Click", "Change").action("Right-Click", "Disable")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLAT_SELL = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_FLAT_SELL")
        .name("Sell Price")
        .currentHeader().current("Current", Placeholders.PRODUCT_PRICE_SELL).breakLine()
        .text("Sets the product sell price.").breakLine()
        .noteHeader().notes("Negative value makes product unsellable.").breakLine()
        .actionsHeader().action("Left-Click", "Change").action("Right-Click", "Disable")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLOAT_BUY = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_FLOAT_BUY")
        .name("Buy Price")
        .currentHeader()
        .current("Min", Placeholders.PRODUCT_PRICER_BUY_MIN).current("Max", Placeholders.PRODUCT_PRICER_BUY_MAX).breakLine()
        .text("Sets the bounds for product buy price.", "Final price will be within these values.").breakLine()
        .noteHeader().notes("Negative value makes product unbuyable.").breakLine()
        .actionsHeader().action("Left-Click", "Change Min").action("Right-Click", "Change Max")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLOAT_SELL = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_FLOAT_SELL")
        .name("Sell Price")
        .currentHeader()
        .current("Min", Placeholders.PRODUCT_PRICER_SELL_MIN).current("Max", Placeholders.PRODUCT_PRICER_SELL_MAX).breakLine()
        .text("Sets the bounds for product sell price.", "Final price will be within these values.").breakLine()
        .noteHeader().notes("Negative value makes product unsellable.").breakLine()
        .actionsHeader().action("Left-Click", "Change Min").action("Right-Click", "Change Max")
        .build();

    public static final EditorLocale PRODUCT_PRICE_FLOAT_REFRESH = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_FLOAT_REFRESH")
        .name("Refresh Settings")
        .currentHeader()
        .current("Days", Placeholders.PRODUCT_PRICER_FLOAT_REFRESH_DAYS)
        .current("Times", Placeholders.PRODUCT_PRICER_FLOAT_REFRESH_TIMES).breakLine()
        .text("Sets conditions, when the product price will be refreshed.", "Until that, the previous generated price will be used.").breakLine()
        .warningHeader().warning("You have to set at least one " + RED + "day" + GRAY + " and " + RED + "time" + GRAY + "!").breakLine()
        .actionsHeader().action("Lift-Click", "Add Day").action("Shift-Left", "Clear Days")
        .action("Right-Click", "Add Time").action("Shift-Right", "Clear Times")
        .build();

    public static final EditorLocale PRODUCT_PRICE_DYNAMIC_BUY = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_DYNAMIC_BUY")
        .name("Buy Price Bounds")
        .currentHeader()
        .current("Min", Placeholders.PRODUCT_PRICER_BUY_MIN).current("Max", Placeholders.PRODUCT_PRICER_BUY_MAX).breakLine()
        .text("Sets the bounds for product buy price.", "Final price will be within these values.").breakLine()
        .noteHeader().notes("Negative value makes product unbuyable.").breakLine()
        .actionsHeader().action("Left-Click", "Change Min").action("Right-Click", "Change Max")
        .build();

    public static final EditorLocale PRODUCT_PRICE_DYNAMIC_SELL = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_DYNAMIC_SELL")
        .name("Sell Price Bounds")
        .currentHeader()
        .current("Min", Placeholders.PRODUCT_PRICER_SELL_MIN).current("Max", Placeholders.PRODUCT_PRICER_SELL_MAX).breakLine()
        .text("Sets the bounds for product sell price.", "Final price will be within these values.").breakLine()
        .noteHeader().notes("Negative value makes product unsellable.").breakLine()
        .actionsHeader().action("Left-Click", "Change Min").action("Right-Click", "Change Max")
        .build();

    public static final EditorLocale PRODUCT_PRICE_DYNAMIC_INITIAL = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_DYNAMIC_INITIAL")
        .name("Initial Price")
        .currentHeader()
        .current("Buy", Placeholders.PRODUCT_PRICER_DYNAMIC_INITIAL_BUY)
        .current("Sell", Placeholders.PRODUCT_PRICER_DYNAMIC_INITIAL_SELL).breakLine()
        .text("Sets initial product prices.", "These values will be used as default/start ones.").breakLine()
        .actionsHeader().action("Left-Click", "Change Buy").action("Right-Click", "Change Sell")
        .build();

    public static final EditorLocale PRODUCT_PRICE_DYNAMIC_STEP = builder(PREFIX_OLD + "PRODUCT_CHANGE_PRICE_DYNAMIC_STEP")
        .name("Price Step")
        .currentHeader()
        .current("Buy", Placeholders.PRODUCT_PRICER_DYNAMIC_STEP_BUY)
        .current("Sell", Placeholders.PRODUCT_PRICER_DYNAMIC_STEP_SELL).breakLine()
        .text("Step defines how much the price will grow up/down", "on each sale/purchase.").breakLine()
        .noteHeader().notes("More purchases - higher price.", "More sales - lower price").breakLine()
        .actionsHeader().action("Left-Click", "Change Buy").action("Right-Click", "Change Sell")
        .build();

    public static final EditorLocale DISCOUNT_CREATE = builder(PREFIX_OLD + "DISCOUNT_CREATE")
        .name("Create Discount")
        .text("Creates a new discount.").breakLine()
        .actionsHeader().action("Left-Click", "Create")
        .build();

    public static final EditorLocale DISCOUNT_OBJECT = builder(PREFIX_OLD + "DISCOUNT_OBJECT")
        .name("Discount")
        .currentHeader().current("Amount", Placeholders.DISCOUNT_CONFIG_AMOUNT)
        .current("Days", Placeholders.DISCOUNT_CONFIG_DAYS).current("Times", Placeholders.DISCOUNT_CONFIG_TIMES).breakLine()
        .actionsHeader().action("Left-Click", "Edit")
        .action("Shift-Right", "Delete " + RED + "(No Undo)")
        .build();

    public static final EditorLocale DISCOUNT_AMOUNT = builder(PREFIX_OLD + "DISCOUNT_CHANGE_DISCOUNT")
        .name("Amount")
        .currentHeader().current("Amount", Placeholders.DISCOUNT_CONFIG_AMOUNT + "%").breakLine()
        .text("Sets the discount amount (in percent).").breakLine()
        .actionsHeader().action("Left-Click", "Change")
        .build();

    public static final EditorLocale DISCOUNT_DURATION = builder(PREFIX_OLD + "DISCOUNT_CHANGE_DURATION")
        .name("Duration")
        .currentHeader().current("Duration", Placeholders.DISCOUNT_CONFIG_DURATION).breakLine()
        .text("Sets how long (in seconds) this", "discount will be active.").breakLine()
        .actionsHeader().action("Left-Click", "Change")
        .build();

    public static final EditorLocale DISCOUNT_DAYS = builder( PREFIX_OLD + "DISCOUNT_CHANGE_DAY")
        .name("Active Days")
        .currentHeader().current("Days", Placeholders.DISCOUNT_CONFIG_DAYS).breakLine()
        .text("A list of days, when this discount will have effect.").breakLine()
        .noteHeader().notes("At least one " + ORANGE + "day" + GRAY + " and " + ORANGE + "time" + GRAY + " are required!").breakLine()
        .actionsHeader().action("Left-Click", "Add Day").action("Right-Click", "Clear List")
        .build();

    public static final EditorLocale DISCOUNT_TIMES = builder(PREFIX_OLD + "DISCOUNT_CHANGE_TIME")
        .name("Active Times")
        .currentHeader().current("Days", Placeholders.DISCOUNT_CONFIG_TIMES).breakLine()
        .text("A list of times, when this discount will be activated.").breakLine()
        .noteHeader().notes("At least one " + ORANGE + "day" + GRAY + " and " + ORANGE + "time" + GRAY + " are required!").breakLine()
        .actionsHeader().action("Left-Click", "Add Time").action("Right-Click", "Clear List")
        .build();
}
