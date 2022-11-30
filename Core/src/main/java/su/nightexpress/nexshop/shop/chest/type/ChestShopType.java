package su.nightexpress.nexshop.shop.chest.type;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Perms;

public enum ChestShopType {
    PLAYER, ADMIN;

    public boolean hasPermission(@NotNull Player player) {
        return player.hasPermission(Perms.CHEST_SHOP_TYPE)
            || player.hasPermission(Perms.PREFIX_CHEST_TYPE + this.name().toLowerCase());
    }
}
