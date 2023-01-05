package su.nightexpress.nexshop.shop.chest.menu.list;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.ArrayList;
import java.util.List;

public class ChestListGlobalMenu extends AbstractChestListMenu {

    public ChestListGlobalMenu(@NotNull ChestShopModule chestShop) {
        super(chestShop, JYML.loadOrExtract(chestShop.plugin(), chestShop.getPath() + "list.global.menu.yml"));
    }

    @Override
    @NotNull
    protected List<ChestShop> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.chestShop.getShops().stream().toList());
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull ChestShop shop) {
        return (player1, type, e) -> {
            if (e.isRightClick()) {
                if (shop.isOwner(player1) || player1.hasPermission(Perms.ADMIN)) {
                    shop.getEditor().open(player1, 1);
                }
                else plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player1);
                return;
            }

            if ((shop.isOwner(player1) && !player1.hasPermission(Perms.CHEST_SHOP_TELEPORT))
                    || (!shop.isOwner(player1) && !player1.hasPermission(Perms.CHEST_SHOP_TELEPORT_OTHERS))) {
                plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player1);
                return;
            }

            shop.teleport(player1);
        };
    }
}
