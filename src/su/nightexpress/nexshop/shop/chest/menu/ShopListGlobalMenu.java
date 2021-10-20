package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;

import java.util.ArrayList;
import java.util.List;

public class ShopListGlobalMenu extends AbstractShopListMenu {

    public ShopListGlobalMenu(@NotNull ChestShop chestShop) {
        super(chestShop, ChestShopConfig.YML_LIST_GLOBAL);
    }

    @Override
    @NotNull
    protected List<IShopChest> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.chestShop.getShops().stream().toList());
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IShopChest shop) {
        return (player1, type, e) -> {
            if (e.isRightClick()) {
                if (shop.isOwner(player1) || player1.hasPermission(Perms.ADMIN)) {
                    shop.getEditor().open(player1, 1);
                }
                else plugin.lang().Error_NoPerm.send(player1);
                return;
            }

            if ((shop.isOwner(player1) && !player1.hasPermission(Perms.CHEST_TELEPORT))
                    || (!shop.isOwner(player1) && !player1.hasPermission(Perms.CHEST_TELEPORT_OTHERS))) {
                plugin.lang().Error_NoPerm.send(player1);
                return;
            }

            shop.teleport(player1);
        };
    }
}
