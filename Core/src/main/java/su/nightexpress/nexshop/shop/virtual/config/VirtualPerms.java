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

    public static final UniPermission MODULE  = new UniPermission(PREFIX + Placeholders.WILDCARD, "Full access to the Virtual Shop module.");
    public static final UniPermission SHOP    = new UniPermission(PREFIX_SHOP + Placeholders.WILDCARD, "Access to all the Virtual Shops.");
    public static final UniPermission COMMAND = new UniPermission(PREFIX_COMMAND + Placeholders.WILDCARD, "Access to all the Virtual Shop commands.");
    public static final UniPermission BYPASS  = new UniPermission(PREFIX_BYPASS + Placeholders.WILDCARD, "Bypasses all Virtual Shop restrictions.");

    public static final UniPermission COMMAND_EDITOR           = new UniPermission(PREFIX_COMMAND + "editor", "Access to '/virtualshop editor' command.");
    public static final UniPermission COMMAND_OPEN             = new UniPermission(PREFIX_COMMAND + "open", "Access to '/virtualshop open' command.");
    public static final UniPermission COMMAND_OPEN_OTHERS      = new UniPermission(PREFIX_COMMAND + "open.others", "Access to '/virtualshop open' command on other players.");
    public static final UniPermission COMMAND_MENU             = new UniPermission(PREFIX_COMMAND + "menu", "Access to '/virtualshop menu' command.");
    public static final UniPermission COMMAND_MENU_OTHERS      = new UniPermission(PREFIX_COMMAND + "menu.others");
    public static final UniPermission COMMAND_SELL_MENU        = new UniPermission(PREFIX_COMMAND + "sellmenu", "Access to the Sell Menu commands.");
    public static final UniPermission COMMAND_SELL_MENU_OTHERS = new UniPermission(PREFIX_COMMAND + "sellmenu.others", "Access to the Sell Menu commands on other players.");
    public static final UniPermission COMMAND_SELL_ALL         = new UniPermission(PREFIX_COMMAND + "sellall", "Access to the Sell All command.");
    public static final UniPermission COMMAND_SELL_ALL_OTHERS  = new UniPermission(PREFIX_COMMAND + "sellall.others", "Access to the Sell All command on other players.");
    public static final UniPermission COMMAND_SELL_HAND        = new UniPermission(PREFIX_COMMAND + "sellhand", "Access to the Sell Hand command.");
    public static final UniPermission COMMAND_SELL_HAND_OTHERS = new UniPermission(PREFIX_COMMAND + "sellhand.others", "Access to the Sell Hand command on other players.");
    public static final UniPermission COMMAND_SHOP             = new UniPermission(PREFIX_COMMAND + "shop", "Access to the Shop Shortcut commands.");

    public static final UniPermission BYPASS_WORLDS   = new UniPermission(PREFIX_BYPASS + "worlds", "Allows to use shops in any world.");
    public static final UniPermission BYPASS_GAMEMODE = new UniPermission(PREFIX_BYPASS + "gamemode", "Allows to use shops in any GameMode.");

    static {
        Perms.PLUGIN.addChildren(MODULE);

        MODULE.addChildren(
            SHOP,
            COMMAND,
            BYPASS
        );

        COMMAND.addChildren(
            COMMAND_EDITOR,
            COMMAND_OPEN, COMMAND_OPEN_OTHERS,
            COMMAND_MENU, COMMAND_MENU_OTHERS,
            COMMAND_SELL_MENU, COMMAND_SELL_MENU_OTHERS,
            COMMAND_SELL_ALL, COMMAND_SELL_ALL_OTHERS,
            COMMAND_SELL_HAND, COMMAND_SELL_HAND_OTHERS,
            COMMAND_SHOP
        );

        BYPASS.addChildren(BYPASS_GAMEMODE, BYPASS_WORLDS);
    }
}
