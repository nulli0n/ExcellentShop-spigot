package su.nightexpress.excellentshop.feature.playershop.core;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.ShopPlugin;

public class ChestKeys {

    public static NamespacedKey shopItemType;

    public static void load(@NotNull ShopPlugin plugin) {
        shopItemType = new NamespacedKey(plugin, "chestshop.item.block_type");
    }
}
