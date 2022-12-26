package su.nightexpress.nexshop;

import org.bukkit.permissions.PermissionDefault;
import su.nexmedia.engine.api.server.JPermission;

public class Perms {

    private static final String PREFIX = "excellentshop.";
    public static final String PREFIX_AUCTION_CURRENCY = PREFIX + "auction.currency.";
    public static final String PREFIX_CHEST_PRICE = PREFIX + "chestshop.price.";
    public static final String PREFIX_CHEST_TYPE = PREFIX + "chestshop.type.";
    public static final String PREFIX_VIRTUAL_SHOP = PREFIX + "virtual.shop.";

    private static final PermissionDefault OP = PermissionDefault.OP;
    private static final PermissionDefault TRUE = PermissionDefault.TRUE;

    @Deprecated public static final  String ADMIN  = PREFIX + "admin";

    public static final JPermission PLUGIN         = new JPermission(PREFIX + "*", "Full plugin access.");
    public static final JPermission PLUGIN_COMMAND = new JPermission(PREFIX + "command", "Access to all the plugin commands.");

    public static final JPermission COMMAND_CURRENCY = new JPermission(PREFIX + "command.currency", "Access to 'currency' sub-command without children commands.", OP);
    public static final JPermission COMMAND_CURRENCY_GIVE = new JPermission(PREFIX + "command.currency.give", "Access to 'currency give' sub-command.", OP);
    public static final JPermission COMMAND_CURRENCY_TAKE = new JPermission(PREFIX + "command.currency.take", "Access to 'currency take' sub-command.", OP);
    public static final JPermission COMMAND_CURRENCY_CREATE = new JPermission(PREFIX + "command.currency.create", "Access to 'currency create' sub-command.", OP);
    public static final JPermission COMMAND_RELOAD         = new JPermission(PREFIX + "command.reload", "Access to the reload command.", OP);

    public static final JPermission CHEST_SHOP = new JPermission(PREFIX + "chestshop", "Full access to the Chest Shop module.");
    public static final JPermission CHEST_SHOP_COMMAND      = new JPermission(PREFIX + "chestshop.command", "Access to all the Chest Shop commands.");
    public static final JPermission CHEST_SHOP_COMMAND_LIST   = new JPermission(PREFIX + "chestshop.command.list");
    public static final JPermission CHEST_SHOP_COMMAND_SEARCH = new JPermission(PREFIX + "chestshop.command.search");
    public static final JPermission CHEST_SHOP_TELEPORT        = new JPermission(PREFIX + "chestshop.teleport");
    public static final JPermission CHEST_SHOP_TELEPORT_OTHERS = new JPermission(PREFIX + "chestshop.teleport.others");
    public static final JPermission CHEST_SHOP_CREATE   = new JPermission(PREFIX + "chestshop.create");
    public static final JPermission CHEST_SHOP_REMOVE        = new JPermission(PREFIX + "chestshop.remove");
    public static final JPermission CHEST_SHOP_REMOVE_OTHERS = new JPermission(PREFIX + "chestshop.remove.others");
    public static final JPermission CHEST_SHOP_TYPE  = new JPermission(PREFIX_CHEST_TYPE + Placeholders.MASK_ANY);
    public static final JPermission CHEST_SHOP_PRICE = new JPermission(PREFIX_CHEST_PRICE + Placeholders.MASK_ANY);

    public static final JPermission VIRTUAL         = new JPermission(PREFIX + "virtual", "Full access to the Virtual Shop module.");
    public static final JPermission VIRTUAL_COMMAND = new JPermission(PREFIX + "virtual.command", "Access to all the Virtual Shop commands.");
    public static final JPermission VIRTUAL_COMMAND_EDITOR  = new JPermission(PREFIX + "virtual.command.editor", "Access to '/virtualshop editor' command.", OP);
    public static final JPermission VIRTUAL_COMMAND_OPEN        = new JPermission(PREFIX + "virtual.command.open", "Access to '/virtualshop open' command.");
    public static final JPermission VIRTUAL_COMMAND_OPEN_OTHERS = new JPermission(PREFIX + "virtual.command.open.others", "Access to '/virtualshop open' command on other players.");
    public static final JPermission VIRTUAL_COMMAND_SELL_MENU = new JPermission(PREFIX + "virtual.command.sellmenu", "Access to the Sell Menu commands.");
    public static final JPermission VIRTUAL_MAIN_MENU           = new JPermission(PREFIX + "virtual.mainmenu", "Access to the Main Menu of Virtual Shop module.");
    public static final JPermission VIRTUAL_SHOP                = new JPermission(PREFIX_VIRTUAL_SHOP + Placeholders.MASK_ANY, "Access to all the Virtual Shops.");

    public static final JPermission AUCTION         = new JPermission(PREFIX + "auction", "Access to all the Auction functions.", OP);
    public static final JPermission AUCTION_COMMAND = new JPermission(PREFIX + "auction.command", "Access to all the Auction commands.", OP);
    public static final JPermission AUCTION_BYPASS  = new JPermission(PREFIX + "auction.bypass", "Bypass all the Auction restrictions.", OP);
    public static final JPermission AUCTION_CURRENCY = new JPermission(PREFIX_AUCTION_CURRENCY + Placeholders.MASK_ANY, "Allows to use all enabled currencies on Auction.", OP);

    public static final JPermission AUCTION_COMMAND_EXPIRED = new JPermission(PREFIX + "auction.command.expired", "Access to '/auction expired' command", TRUE);
    public static final JPermission AUCTION_COMMAND_EXPIRED_OTHERS = new JPermission(PREFIX + "auction.command.expired.others", "Access to '/auction expired' of other players.", OP);
    public static final JPermission AUCTION_COMMAND_SELLING = new JPermission(PREFIX + "auction.command.selling", "Access to '/auction selling' command", TRUE);
    public static final JPermission AUCTION_COMMAND_SELLING_OTHERS = new JPermission(PREFIX + "auction.command.selling.others", "Access to '/auction selling' command of other players.", OP);
    public static final JPermission AUCTION_COMMAND_UNCLAIMED = new JPermission(PREFIX + "auction.command.unclaimed", "Access to '/auction unclaimed' command", TRUE);
    public static final JPermission AUCTION_COMMAND_UNCLAIMED_OTHERS = new JPermission(PREFIX + "auction.command.unclaimed.others", "Access to '/auction unclaimed' command of other players.", OP);
    public static final JPermission AUCTION_COMMAND_HISTORY = new JPermission(PREFIX + "auction.command.history", "Access to '/auction history' command", TRUE);
    public static final JPermission AUCTION_COMMAND_HISTORY_OTHERS = new JPermission(PREFIX + "auction.command.history.others", "Access to '/auction history' command of other players.", OP);
    public static final JPermission AUCTION_COMMAND_OPEN = new JPermission(PREFIX + "auction.command.open", "Access to '/auction [open]' command.", TRUE);
    public static final JPermission AUCTION_COMMAND_SELL = new JPermission(PREFIX + "auction.command.sell", "Access to '/auction sell' command.", TRUE);

    public static final JPermission AUCTION_BYPASS_LISTING_TAX    = new JPermission(PREFIX + "auction.bypass.listing.tax", "Bypass listing taxes.", OP);
    public static final JPermission AUCTION_BYPASS_LISTING_PRICE  = new JPermission(PREFIX + "auction.bypass.listing.price", "Bypass listing price limits.", OP);
    public static final JPermission AUCTION_BYPASS_DISABLED_WORLDS = new JPermission(PREFIX + "auction.bypass.disabled_worlds", "Bypass world restrictions to use Auction.", OP);
    public static final JPermission AUCTION_BYPASS_DISABLED_GAMEMODES = new JPermission(PREFIX + "auction.bypass.disabled_gamemodes", "Bypass GameMode restrictions to add items on Auction.", OP);

    public static final JPermission AUCTION_LISTING_REMOVE_OTHERS = new JPermission(PREFIX + "auction.listing.remove.others", "Allows to remove other player's listings.", OP);

    static {
        PLUGIN.addChildren(PLUGIN_COMMAND, CHEST_SHOP, VIRTUAL, AUCTION);
        PLUGIN_COMMAND.addChildren(COMMAND_CURRENCY, COMMAND_RELOAD);
        COMMAND_CURRENCY.addChildren(COMMAND_CURRENCY_CREATE, COMMAND_CURRENCY_GIVE, COMMAND_CURRENCY_TAKE);

        CHEST_SHOP.addChildren(CHEST_SHOP_COMMAND, CHEST_SHOP_CREATE, CHEST_SHOP_PRICE,
            CHEST_SHOP_REMOVE, CHEST_SHOP_REMOVE_OTHERS,
            CHEST_SHOP_TELEPORT, CHEST_SHOP_TELEPORT_OTHERS, CHEST_SHOP_TYPE);
        CHEST_SHOP_COMMAND.addChildren(CHEST_SHOP_COMMAND_LIST, CHEST_SHOP_COMMAND_SEARCH);

        VIRTUAL.addChildren(VIRTUAL_COMMAND, VIRTUAL_MAIN_MENU, VIRTUAL_SHOP);
        VIRTUAL_COMMAND.addChildren(VIRTUAL_COMMAND_EDITOR, VIRTUAL_COMMAND_OPEN, VIRTUAL_COMMAND_OPEN_OTHERS);

        AUCTION.addChildren(AUCTION_COMMAND, AUCTION_BYPASS, AUCTION_CURRENCY, AUCTION_LISTING_REMOVE_OTHERS);
        AUCTION_BYPASS.addChildren(AUCTION_BYPASS_LISTING_PRICE, AUCTION_BYPASS_LISTING_TAX,
            AUCTION_BYPASS_DISABLED_GAMEMODES, AUCTION_BYPASS_DISABLED_WORLDS);
        AUCTION_COMMAND.addChildren(AUCTION_COMMAND_OPEN, AUCTION_COMMAND_SELL,
            AUCTION_COMMAND_EXPIRED, AUCTION_COMMAND_EXPIRED_OTHERS,
            AUCTION_COMMAND_HISTORY, AUCTION_COMMAND_HISTORY_OTHERS,
            AUCTION_COMMAND_SELLING, AUCTION_COMMAND_SELLING_OTHERS,
            AUCTION_COMMAND_UNCLAIMED, AUCTION_COMMAND_UNCLAIMED_OTHERS);
    }
}
