package su.nightexpress.nexshop.shop.chest.type;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;

public enum ChestType {
    PLAYER, ADMIN;

    public boolean hasPermission(@NotNull Player player) {
        return player.hasPermission(Perms.CHEST_TYPE + Placeholders.MASK_ANY)
            || player.hasPermission(Perms.CHEST_TYPE + this.name().toLowerCase());
    }
}
