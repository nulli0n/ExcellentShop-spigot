package su.nightexpress.nexshop.shop.chest.config;

import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class ChestPerms {

    public static final String PREFIX               = Perms.PREFIX + "chestshop.";
    public static final String PREFIX_COMMAND       = PREFIX + "command.";
    public static final String PREFIX_BYPASS        = PREFIX + "bypass.";
    public static final String PREFIX_PRICE_TYPE    = PREFIX + "price.";
    public static final String PREFIX_SHOP_TYPE     = PREFIX + "type.";
    public static final String PREFIX_PRODUCT_LIMIT = PREFIX + "products.amount.";
    public static final String PREFIX_SHOP_LIMIT    = PREFIX + "shops.amount.";

    public static final UniPermission MODULE     = new UniPermission(PREFIX + Placeholders.WILDCARD);
    public static final UniPermission COMMAND    = new UniPermission(PREFIX_COMMAND + Placeholders.WILDCARD);
    public static final UniPermission BYPASS     = new UniPermission(PREFIX_BYPASS + Placeholders.WILDCARD);
    public static final UniPermission SHOP_TYPE  = new UniPermission(PREFIX_SHOP_TYPE + Placeholders.WILDCARD);
    public static final UniPermission PRICE_TYPE = new UniPermission(PREFIX_PRICE_TYPE + Placeholders.WILDCARD);

    public static final UniPermission DISPLAY_CUSTOMIZATION = new UniPermission(PREFIX + "display.customization");
    public static final UniPermission TELEPORT              = new UniPermission(PREFIX + "teleport");
    public static final UniPermission TELEPORT_OTHERS       = new UniPermission(PREFIX + "teleport.others");
    public static final UniPermission CREATE                = new UniPermission(PREFIX + "create");
    public static final UniPermission REMOVE                = new UniPermission(PREFIX + "remove");
    public static final UniPermission REMOVE_OTHERS         = new UniPermission(PREFIX + "remove.others");
    public static final UniPermission EDIT_OTHERS           = new UniPermission(PREFIX + "edit.others");

    public static final UniPermission COMMAND_LIST        = new UniPermission(PREFIX_COMMAND + "list");
    public static final UniPermission COMMAND_BROWSE      = new UniPermission(PREFIX_COMMAND + "browse");
    public static final UniPermission COMMAND_OPEN        = new UniPermission(PREFIX_COMMAND + "open");
    public static final UniPermission COMMAND_BANK        = new UniPermission(PREFIX_COMMAND + "bank");
    public static final UniPermission COMMAND_BANK_OTHERS = new UniPermission(PREFIX_COMMAND + "bank.others");

    public static final UniPermission BYPASS_CREATION_CLAIMS = new UniPermission(PREFIX_BYPASS + "creation.claims");

    static {
        Perms.PLUGIN.addChildren(MODULE);

        MODULE.addChildren(
            COMMAND,
            BYPASS,
            SHOP_TYPE,
            PRICE_TYPE,
            CREATE,
            DISPLAY_CUSTOMIZATION,
            REMOVE, REMOVE_OTHERS,
            TELEPORT, TELEPORT_OTHERS,
            EDIT_OTHERS
        );

        COMMAND.addChildren(
            COMMAND_OPEN,
            COMMAND_LIST,
            COMMAND_BROWSE,
            COMMAND_BANK, COMMAND_BANK_OTHERS
        );

        BYPASS.addChildren(BYPASS_CREATION_CLAIMS);
    }
}
