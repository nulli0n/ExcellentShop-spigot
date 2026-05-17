package su.nightexpress.excellentshop.core;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlugin;

public class Keys {

    //@Deprecated public static NamespacedKey brokenItem;
    public static NamespacedKey keyProductCache;

    public static void load(@NonNull ShopPlugin plugin) {
        //brokenItem = new NamespacedKey(plugin, "broken_item_tag");
        keyProductCache = new NamespacedKey(plugin, "product_cache");
    }

    public static void clear() {
        //brokenItem = null;
        keyProductCache = null;
    }
}
