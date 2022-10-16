package su.nightexpress.nexshop;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public class Perms {

    private static final String PREFIX = "excellentshop.";
    private static final PermissionDefault OP = PermissionDefault.OP;
    private static final PermissionDefault TRUE = PermissionDefault.TRUE;

    @Deprecated public static final  String ADMIN  = PREFIX + "admin";
    @Deprecated public static final  String USER   = PREFIX + "user";

    public static final Permission PLUGIN         = new Permission(PREFIX + "*", OP);
    public static final Permission PLUGIN_COMMAND = new Permission(PREFIX + "command", OP);

    public static final Permission COMMAND_CURRENCY = new Permission(PREFIX + "command.currency", "Access to 'currency' sub-command without children commands.", OP);
    public static final Permission COMMAND_CURRENCY_GIVE = new Permission(PREFIX + "command.currency.give", "Access to 'currency give' sub-command.", OP);
    public static final Permission COMMAND_CURRENCY_TAKE = new Permission(PREFIX + "command.currency.take", "Access to 'currency take' sub-command.", OP);
    public static final Permission COMMAND_CURRENCY_CREATE = new Permission(PREFIX + "command.currency.create", "Access to 'currency create' sub-command.", OP);
    public static final Permission COMMAND_RELOAD         = new Permission(PREFIX + "command.reload", "Access to the reload command.", OP);

    public static final Permission VIRTUAL         = new Permission(PREFIX + "virtual", OP);
    public static final Permission VIRTUAL_COMMAND = new Permission(PREFIX + "virtual.command", OP);

    public static final Permission AUCTION         = new Permission(PREFIX + "auction", "Access to all the Auction functions.", OP);
    public static final Permission AUCTION_COMMAND = new Permission(PREFIX + "auction.command", "Access to all the Auction commands.", OP);
    public static final Permission AUCTION_BYPASS  = new Permission(PREFIX + "auction.bypass", "Bypass all the Auction restrictions.", OP);
    public static final Permission AUCTION_CURRENCY = new Permission(PREFIX + "auction.currency", "Allows to use all enabled currencies on Auction.", OP);

    public static final Permission AUCTION_COMMAND_EXPIRED = new Permission(PREFIX + "auction.command.expired", "Access to '/auction expired' command", TRUE);
    public static final Permission AUCTION_COMMAND_EXPIRED_OTHERS = new Permission(PREFIX + "auction.command.expired.others", "Access to '/auction expired' of other players.", OP);
    public static final Permission AUCTION_COMMAND_SELLING = new Permission(PREFIX + "auction.command.selling", "Access to '/auction selling' command", TRUE);
    public static final Permission AUCTION_COMMAND_SELLING_OTHERS = new Permission(PREFIX + "auction.command.selling.others", "Access to '/auction selling' command of other players.", OP);
    public static final Permission AUCTION_COMMAND_UNCLAIMED = new Permission(PREFIX + "auction.command.unclaimed", "Access to '/auction unclaimed' command", TRUE);
    public static final Permission AUCTION_COMMAND_UNCLAIMED_OTHERS = new Permission(PREFIX + "auction.command.unclaimed.others", "Access to '/auction unclaimed' command of other players.", OP);
    public static final Permission AUCTION_COMMAND_HISTORY = new Permission(PREFIX + "auction.command.history", "Access to '/auction history' command", TRUE);
    public static final Permission AUCTION_COMMAND_HISTORY_OTHERS = new Permission(PREFIX + "auction.command.history.others", "Access to '/auction history' command of other players.", OP);
    public static final Permission AUCTION_COMMAND_OPEN = new Permission(PREFIX + "auction.command.open", "Access to '/auction [open]' command.", TRUE);
    public static final Permission AUCTION_COMMAND_SELL = new Permission(PREFIX + "auction.command.sell", "Access to '/auction sell' command.", TRUE);

    public static final Permission AUCTION_BYPASS_LISTING_TAX    = new Permission(PREFIX + "auction.bypass.listing.tax", "Bypass listing taxes.", OP);
    public static final Permission AUCTION_BYPASS_LISTING_PRICE  = new Permission(PREFIX + "auction.bypass.listing.price", "Bypass listing price limits.", OP);
    public static final Permission AUCTION_BYPASS_DISABLED_WORLDS = new Permission(PREFIX + "auction.bypass.disabled_worlds", "Bypass world restrictions to use Auction.", OP);
    public static final Permission AUCTION_BYPASS_DISABLED_GAMEMODES = new Permission(PREFIX + "auction.bypass.disabled_gamemodes", "Bypass GameMode restrictions to add items on Auction.", OP);

    public static final Permission AUCTION_LISTING_REMOVE_OTHERS = new Permission(PREFIX + "auction.listing.remove.others", "Allows to remove other player's listings.", OP);

    static {
        addChildren(PLUGIN, PLUGIN_COMMAND);
        addChildren(PLUGIN, VIRTUAL);
        addChildren(PLUGIN, AUCTION);

        addChildren(PLUGIN_COMMAND, COMMAND_CURRENCY);
        addChildren(PLUGIN_COMMAND, COMMAND_RELOAD);

        addChildren(VIRTUAL, VIRTUAL_COMMAND);

        addChildren(AUCTION, AUCTION_COMMAND);
        addChildren(AUCTION, AUCTION_BYPASS);
        addChildren(AUCTION, AUCTION_LISTING_REMOVE_OTHERS);
        addChildren(AUCTION_BYPASS, AUCTION_BYPASS_LISTING_PRICE);
        addChildren(AUCTION_BYPASS, AUCTION_BYPASS_LISTING_TAX);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_OPEN);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_SELL);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_EXPIRED);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_EXPIRED_OTHERS);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_HISTORY);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_HISTORY_OTHERS);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_SELLING);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_SELLING_OTHERS);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_UNCLAIMED);
        addChildren(AUCTION_COMMAND, AUCTION_COMMAND_UNCLAIMED_OTHERS);
    }

    private static void addChildren(@NotNull Permission parent, @NotNull Permission children) {
        children.addParent(parent, true);
    }

    public static final Permission VIRTUAL_COMMAND_EDITOR = new Permission(PREFIX + "editor", "Allows to use the ingame editor.", PermissionDefault.OP);
    public static final String VIRTUAL_CMD_OPEN = PREFIX + "virtual.cmd.open";
    public static final String VIRTUAL_CMD_OPEN_OTHERS = PREFIX + "virtual.cmd.open.others";
    public static final String VIRTUAL_MAINMENU = PREFIX + "virtual.mainmenu";
    public static final String VIRTUAL_SHOP     = PREFIX + "virtual.shop.";

    public static final String CHEST_CMD_LIST                      = PREFIX + "chestshop.cmd.list";
    public static final String CHEST_CMD_SEARCH                    = PREFIX + "chestshop.cmd.search";
    public static final String CHEST_TELEPORT                      = PREFIX + "chestshop.teleport";
    public static final String CHEST_TELEPORT_OTHERS               = PREFIX + "chestshop.teleport.others";
    public static final String CHEST_CREATE                        = PREFIX + "chestshop.create";
    public static final String CHEST_REMOVE                        = PREFIX + "chestshop.remove";
    public static final String CHEST_REMOVE_OTHERS                 = PREFIX + "chestshop.remove.others";
    public static final String CHEST_TYPE                          = PREFIX + "chestshop.type.";
    public static final String CHEST_EDITOR_TYPE                   = PREFIX + "chestshop.editor.type";
    public static final String CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE = PREFIX + "chestshop.editor.product.price.negative";
    //public static final  String CHEST_EDITOR_PRODUCT_COMMANDS       = PREFIX + "chestshop.editor.product.commands";
    public static final String CHEST_EDITOR_PRODUCT_CURRENCY       = PREFIX + "chestshop.editor.product.currency";
    public static final String CHEST_EDITOR_PRODUCT_PRICE_RND      = PREFIX + "chestshop.editor.product.price.randomizer";
}
