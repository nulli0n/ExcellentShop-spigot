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

    public static final UniPermission COMMAND_RELOAD          = new UniPermission(PREFIX_COMMAND + "reload");

    static {
        PLUGIN.addChildren(
            COMMAND,
            COMMAND_FLAGS,
            KEY_SELL_ALL
        );

        COMMAND.addChildren(
            COMMAND_RELOAD
        );
    }
}
