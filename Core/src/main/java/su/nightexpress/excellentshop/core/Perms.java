package su.nightexpress.excellentshop.core;

import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class Perms {

    public static final String PREFIX          = "excellentshop.";
    public static final String PREFIX_COMMAND  = PREFIX + "command.";
    public static final String PREFIX_CURRENCY = PREFIX + "currency.";

    public static final UniPermission PLUGIN  = new UniPermission(PREFIX + ShopPlaceholders.WILDCARD);
    public static final UniPermission COMMAND = new UniPermission(PREFIX_COMMAND + ShopPlaceholders.WILDCARD);
    public static final UniPermission CURRENCY = new UniPermission(PREFIX_CURRENCY + ShopPlaceholders.WILDCARD);

    public static final UniPermission KEY_SELL_ALL = new UniPermission(PREFIX + "key.sellall");
    public static final UniPermission KEY_BUY_ALL = new UniPermission(PREFIX + "key.buyall");

    @Deprecated
    public static final UniPermission COMMAND_FLAGS = new UniPermission(PREFIX_COMMAND + "flags", "Allows to use flags in commands.");

    public static final UniPermission COMMAND_RELOAD          = new UniPermission(PREFIX_COMMAND + "reload");

    static {
        PLUGIN.addChildren(
            COMMAND,
            CURRENCY,
            COMMAND_FLAGS,
            KEY_SELL_ALL,
            KEY_BUY_ALL
        );

        COMMAND.addChildren(
            COMMAND_RELOAD
        );
    }
}
