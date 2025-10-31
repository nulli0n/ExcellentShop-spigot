package su.nightexpress.nexshop.config;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;

public class Keys {

    public static NamespacedKey              keyProductCache;

    public static void load(@NotNull ShopPlugin plugin) {
        keyProductCache = new NamespacedKey(plugin, "product_cache");
    }

    public static void clear() {
        keyProductCache = null;
    }
}
