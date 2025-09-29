package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.permissions.PermissionDefault;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class ChestPerms {

    public static final String PREFIX               = Perms.PREFIX + "chestshop.";
    public static final String PREFIX_COMMAND       = PREFIX + "command.";
    public static final String PREFIX_BYPASS        = PREFIX + "bypass.";
    public static final String PREFIX_PRODUCT_LIMIT = PREFIX + "products.amount.";
    public static final String PREFIX_SHOP_LIMIT    = PREFIX + "shops.amount.";

    @Deprecated
    public static final String PREFIX_CURRENCY = PREFIX + "currency.";

    public static final UniPermission MODULE     = new UniPermission(PREFIX + Placeholders.WILDCARD);
    public static final UniPermission COMMAND    = new UniPermission(PREFIX_COMMAND + Placeholders.WILDCARD);
    public static final UniPermission BYPASS     = new UniPermission(PREFIX_BYPASS + Placeholders.WILDCARD);
    public static final UniPermission ADMIN_SHOP = new UniPermission(PREFIX + "type.admin");

    public static final UniPermission DISABLE_BUYING  = new UniPermission(PREFIX + "disable.buying", PermissionDefault.TRUE);
    public static final UniPermission DISABLE_SELLING = new UniPermission(PREFIX + "disable.selling", PermissionDefault.TRUE);

    public static final UniPermission DISPLAY_CUSTOMIZATION = new UniPermission(PREFIX + "display.customization");

    public static final UniPermission RENT            = new UniPermission(PREFIX + "rent");
    public static final UniPermission TELEPORT        = new UniPermission(PREFIX + "teleport");
    public static final UniPermission TELEPORT_OTHERS = new UniPermission(PREFIX + "teleport.others");
    public static final UniPermission CREATE          = new UniPermission(PREFIX + "create");
    public static final UniPermission REMOVE          = new UniPermission(PREFIX + "remove");
    public static final UniPermission REMOVE_OTHERS   = new UniPermission(PREFIX + "remove.others");
    public static final UniPermission EDIT_OTHERS     = new UniPermission(PREFIX + "edit.others");

    public static final UniPermission COMMAND_LIST          = new UniPermission(PREFIX_COMMAND + "list");
    public static final UniPermission COMMAND_BROWSE        = new UniPermission(PREFIX_COMMAND + "browse");
    public static final UniPermission COMMAND_SEARCH        = new UniPermission(PREFIX_COMMAND + "search");
    public static final UniPermission COMMAND_PLAYER_SEARCH = new UniPermission(PREFIX_COMMAND + "playersearch");
    public static final UniPermission COMMAND_GIVE_ITEM     = new UniPermission(PREFIX_COMMAND + "giveitem");
    public static final UniPermission COMMAND_OPEN_INV      = new UniPermission(PREFIX_COMMAND + "open");
    public static final UniPermission COMMAND_BANK          = new UniPermission(PREFIX_COMMAND + "bank");
    public static final UniPermission COMMAND_BANK_OTHERS   = new UniPermission(PREFIX_COMMAND + "bank.others");

    public static final UniPermission BYPASS_CREATION_CLAIMS = new UniPermission(PREFIX_BYPASS + "creation.claims");

    static {
        Perms.PLUGIN.addChildren(MODULE);

        MODULE.addChildren(
            COMMAND,
            BYPASS,
            CREATE,
            DISABLE_BUYING,
            DISABLE_SELLING,
            DISPLAY_CUSTOMIZATION,
            REMOVE,
            REMOVE_OTHERS,
            TELEPORT,
            TELEPORT_OTHERS,
            EDIT_OTHERS,
            RENT
        );

        COMMAND.addChildren(
            COMMAND_OPEN_INV,
            COMMAND_LIST,
            COMMAND_BROWSE,
            COMMAND_SEARCH,
            COMMAND_PLAYER_SEARCH,
            COMMAND_GIVE_ITEM,
            COMMAND_BANK, COMMAND_BANK_OTHERS
        );

        BYPASS.addChildren(BYPASS_CREATION_CLAIMS);
    }
}
