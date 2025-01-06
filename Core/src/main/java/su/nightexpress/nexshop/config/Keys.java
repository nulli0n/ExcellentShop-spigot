package su.nightexpress.nexshop.config;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;

public class Keys {

    public static NamespacedKey brokenItem;

    public static void load(@NotNull ShopPlugin plugin) {
        brokenItem = new NamespacedKey(plugin, "broken_item_tag");
    }

    public static void clear() {
        brokenItem = null;
    }
}
