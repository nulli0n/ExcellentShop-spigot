package su.nightexpress.nexshop.module;

public class ModuleId {

    public static final String AUCTION      = "auction";
    public static final String CHEST_SHOP   = "chest_shop";
    public static final String VIRTUAL_SHOP = "virtual_shop";

    public static String[] values() {
        return new String[]{AUCTION, CHEST_SHOP, VIRTUAL_SHOP};
    }
}
