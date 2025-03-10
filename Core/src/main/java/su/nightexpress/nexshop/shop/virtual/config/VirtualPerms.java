package su.nightexpress.nexshop.shop.virtual.config;

import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class VirtualPerms {

    public static final String PREFIX                 = Perms.PREFIX + "virtual.";
    public static final String PREFIX_COMMAND         = PREFIX + "command.";
    public static final String PREFIX_BYPASS          = PREFIX + "bypass.";
    public static final String PREFIX_SHOP            = PREFIX + "shop.";
    public static final String PREFIX_SELL_MULTIPLIER = PREFIX + "sellmultiplier.";

    public static final UniPermission MODULE  = new UniPermission(PREFIX + Placeholders.WILDCARD);
    public static final UniPermission SHOP    = new UniPermission(PREFIX_SHOP + Placeholders.WILDCARD);
    public static final UniPermission COMMAND = new UniPermission(PREFIX_COMMAND + Placeholders.WILDCARD);
    public static final UniPermission BYPASS  = new UniPermission(PREFIX_BYPASS + Placeholders.WILDCARD);

    public static final UniPermission COMMAND_EDITOR               = new UniPermission(PREFIX_COMMAND + "editor");
    public static final UniPermission COMMAND_ROTATE               = new UniPermission(PREFIX_COMMAND + "rotate");
    public static final UniPermission COMMAND_OPEN                 = new UniPermission(PREFIX_COMMAND + "open");
    public static final UniPermission COMMAND_OPEN_OTHERS          = new UniPermission(PREFIX_COMMAND + "open.others");
    public static final UniPermission COMMAND_MENU                 = new UniPermission(PREFIX_COMMAND + "menu");
    public static final UniPermission COMMAND_MENU_OTHERS          = new UniPermission(PREFIX_COMMAND + "menu.others");
    public static final UniPermission COMMAND_SELL_MENU            = new UniPermission(PREFIX_COMMAND + "sellmenu");
    public static final UniPermission COMMAND_SELL_MENU_OTHERS     = new UniPermission(PREFIX_COMMAND + "sellmenu.others");
    public static final UniPermission COMMAND_SELL_ALL             = new UniPermission(PREFIX_COMMAND + "sellall");
    public static final UniPermission COMMAND_SELL_ALL_OTHERS      = new UniPermission(PREFIX_COMMAND + "sellall.others");
    public static final UniPermission COMMAND_SELL_HAND            = new UniPermission(PREFIX_COMMAND + "sellhand");
    public static final UniPermission COMMAND_SELL_HAND_OTHERS     = new UniPermission(PREFIX_COMMAND + "sellhand.others");
    public static final UniPermission COMMAND_SELL_HAND_ALL        = new UniPermission(PREFIX_COMMAND + "sellhandall");
    public static final UniPermission COMMAND_SELL_HAND_ALL_OTHERS = new UniPermission(PREFIX_COMMAND + "sellhandall.others");
    public static final UniPermission COMMAND_SHOP                 = new UniPermission(PREFIX_COMMAND + "shop");

    public static final UniPermission BYPASS_WORLDS   = new UniPermission(PREFIX_BYPASS + "worlds");
    public static final UniPermission BYPASS_GAMEMODE = new UniPermission(PREFIX_BYPASS + "gamemode");

    static {
        Perms.PLUGIN.addChildren(MODULE);

        MODULE.addChildren(
            SHOP,
            COMMAND,
            BYPASS
        );

        COMMAND.addChildren(
            COMMAND_EDITOR,
            COMMAND_ROTATE,
            COMMAND_OPEN, COMMAND_OPEN_OTHERS,
            COMMAND_MENU, COMMAND_MENU_OTHERS,
            COMMAND_SELL_MENU, COMMAND_SELL_MENU_OTHERS,
            COMMAND_SELL_ALL, COMMAND_SELL_ALL_OTHERS,
            COMMAND_SELL_HAND, COMMAND_SELL_HAND_OTHERS,
            COMMAND_SELL_HAND_ALL, COMMAND_SELL_HAND_ALL_OTHERS,
            COMMAND_SHOP
        );

        BYPASS.addChildren(BYPASS_GAMEMODE, BYPASS_WORLDS);
    }
}
