package su.nightexpress.nexshop.shop.chest.config;

import su.nexmedia.engine.api.server.JPermission;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;

public class ChestPerms {

    private static final String PREFIX         = Perms.PREFIX + "chestshop.";
    private static final String PREFIX_COMMAND = PREFIX + "command.";
    public static final String PREFIX_PRICE_TYPE = PREFIX + "price.";
    public static final String PREFIX_SHOP_TYPE  = PREFIX + "type.";

    public static final JPermission MODULE     = new JPermission(PREFIX + Placeholders.WILDCARD, "Full access to the Chest Shop module.");
    public static final JPermission COMMAND    = new JPermission(PREFIX_COMMAND + Placeholders.WILDCARD, "Access to all the Chest Shop commands.");
    public static final JPermission SHOP_TYPE  = new JPermission(PREFIX_SHOP_TYPE + Placeholders.WILDCARD);
    public static final JPermission PRICE_TYPE = new JPermission(PREFIX_PRICE_TYPE + Placeholders.WILDCARD);

    public static final JPermission TELEPORT        = new JPermission(PREFIX + "teleport");
    public static final JPermission TELEPORT_OTHERS = new JPermission(PREFIX + "teleport.others");
    public static final JPermission CREATE          = new JPermission(PREFIX + "create");
    public static final JPermission REMOVE          = new JPermission(PREFIX + "remove");
    public static final JPermission REMOVE_OTHERS   = new JPermission(PREFIX + "remove.others");

    public static final JPermission COMMAND_LIST        = new JPermission(PREFIX_COMMAND + "list");
    public static final JPermission COMMAND_SEARCH      = new JPermission(PREFIX_COMMAND + "search");
    public static final JPermission COMMAND_OPEN        = new JPermission(PREFIX_COMMAND + "open");
    public static final JPermission COMMAND_BANK        = new JPermission(PREFIX_COMMAND + "bank");
    public static final JPermission COMMAND_BANK_OTHERS = new JPermission(PREFIX_COMMAND + "bank.others");

    static {
        Perms.PLUGIN.addChildren(MODULE);

        MODULE.addChildren(
            COMMAND, SHOP_TYPE, PRICE_TYPE,
            CREATE, REMOVE, REMOVE_OTHERS, TELEPORT, TELEPORT_OTHERS
        );

        COMMAND.addChildren(
            COMMAND_OPEN,
            COMMAND_LIST, COMMAND_SEARCH, COMMAND_BANK, COMMAND_BANK_OTHERS
        );
    }
}
