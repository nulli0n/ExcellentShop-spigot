package su.nightexpress.excellentshop.playershop.core;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlugin;

public class ChestKeys {

    public static NamespacedKey shopItemType;

    public static void load(@NonNull ShopPlugin plugin) {
        shopItemType = new NamespacedKey(plugin, "chestshop.item.block_type");
    }
}
