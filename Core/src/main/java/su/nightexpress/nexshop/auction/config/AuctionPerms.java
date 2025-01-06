package su.nightexpress.nexshop.auction.config;

import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class AuctionPerms {

    public static final String PREFIX          = Perms.PREFIX + "auction.";
    public static final String PREFIX_COMMAND  = PREFIX + "command.";
    public static final String PREFIX_BYPASS   = PREFIX + "bypass.";
    public static final String PREFIX_CURRENCY = PREFIX + "currency.";

    public static final UniPermission AUCTION  = new UniPermission(PREFIX + Placeholders.WILDCARD);
    public static final UniPermission COMMAND  = new UniPermission(PREFIX_COMMAND + Placeholders.WILDCARD);
    public static final UniPermission BYPASS   = new UniPermission(PREFIX_BYPASS + Placeholders.WILDCARD);
    public static final UniPermission CURRENCY = new UniPermission(PREFIX_CURRENCY + Placeholders.WILDCARD);

    public static final UniPermission LISTING_REMOVE_OTHERS = new UniPermission(PREFIX + "listing.remove.others");

    public static final UniPermission COMMAND_EXPIRED          = new UniPermission(PREFIX_COMMAND + "expired");
    public static final UniPermission COMMAND_EXPIRED_OTHERS   = new UniPermission(PREFIX_COMMAND + "expired.others");
    public static final UniPermission COMMAND_SELLING          = new UniPermission(PREFIX_COMMAND + "selling");
    public static final UniPermission COMMAND_SELLING_OTHERS   = new UniPermission(PREFIX_COMMAND + "selling.others");
    public static final UniPermission COMMAND_UNCLAIMED        = new UniPermission(PREFIX_COMMAND + "unclaimed");
    public static final UniPermission COMMAND_UNCLAIMED_OTHERS = new UniPermission(PREFIX_COMMAND + "unclaimed.others");
    public static final UniPermission COMMAND_HISTORY          = new UniPermission(PREFIX_COMMAND + "history");
    public static final UniPermission COMMAND_HISTORY_OTHERS   = new UniPermission(PREFIX_COMMAND + "history.others");
    public static final UniPermission COMMAND_OPEN             = new UniPermission(PREFIX_COMMAND + "open");
    public static final UniPermission COMMAND_OPEN_OTHERS      = new UniPermission(PREFIX_COMMAND + "open.others");
    public static final UniPermission COMMAND_SELL             = new UniPermission(PREFIX_COMMAND + "sell");

    public static final UniPermission BYPASS_LISTING_TAX        = new UniPermission(PREFIX_BYPASS + "listing.tax");
    public static final UniPermission BYPASS_LISTING_PRICE      = new UniPermission(PREFIX_BYPASS + "listing.price");
    public static final UniPermission BYPASS_DISABLED_WORLDS    = new UniPermission(PREFIX_BYPASS + "disabled_worlds");
    public static final UniPermission BYPASS_DISABLED_GAMEMODES = new UniPermission(PREFIX_BYPASS + "disabled_gamemodes");

    static {
        Perms.PLUGIN.addChildren(AUCTION);

        AUCTION.addChildren(
            COMMAND,
            BYPASS,
            CURRENCY,
            LISTING_REMOVE_OTHERS
        );

        BYPASS.addChildren(
            BYPASS_LISTING_PRICE,
            BYPASS_LISTING_TAX,
            BYPASS_DISABLED_GAMEMODES,
            BYPASS_DISABLED_WORLDS
        );

        COMMAND.addChildren(
            COMMAND_OPEN,
            COMMAND_SELL,
            COMMAND_EXPIRED, COMMAND_EXPIRED_OTHERS,
            COMMAND_HISTORY, COMMAND_HISTORY_OTHERS,
            COMMAND_SELLING, COMMAND_SELLING_OTHERS,
            COMMAND_UNCLAIMED, COMMAND_UNCLAIMED_OTHERS
        );
    }
}
