package su.nightexpress.nexshop.config;

import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class Perms {

    public static final String PREFIX         = "excellentshop.";
    public static final String PREFIX_COMMAND = PREFIX + "command.";

    public static final UniPermission PLUGIN  = new UniPermission(PREFIX + Placeholders.WILDCARD);
    public static final UniPermission COMMAND = new UniPermission(PREFIX_COMMAND + Placeholders.WILDCARD);

    public static final UniPermission KEY_SELL_ALL = new UniPermission(PREFIX + "key.sellall");

    @Deprecated
    public static final UniPermission COMMAND_FLAGS = new UniPermission(PREFIX_COMMAND + "flags", "Allows to use flags in commands.");

    public static final UniPermission COMMAND_CURRENCY        = new UniPermission(PREFIX_COMMAND + "currency");
    public static final UniPermission COMMAND_CURRENCY_GIVE   = new UniPermission(PREFIX_COMMAND + "currency.give");
    public static final UniPermission COMMAND_CURRENCY_TAKE   = new UniPermission(PREFIX_COMMAND + "currency.take");
    public static final UniPermission COMMAND_CURRENCY_CREATE = new UniPermission(PREFIX_COMMAND + "currency.create");
    public static final UniPermission COMMAND_RELOAD          = new UniPermission(PREFIX_COMMAND + "reload");

    static {
        PLUGIN.addChildren(
            COMMAND,
            COMMAND_FLAGS,
            KEY_SELL_ALL
        );

        COMMAND.addChildren(
            COMMAND_CURRENCY,
            COMMAND_RELOAD
        );

        COMMAND_CURRENCY.addChildren(
            COMMAND_CURRENCY_CREATE,
            COMMAND_CURRENCY_GIVE,
            COMMAND_CURRENCY_TAKE
        );
    }
}
