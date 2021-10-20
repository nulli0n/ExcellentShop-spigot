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

public class ShopListOwnMenu extends AbstractShopListMenu {

    public ShopListOwnMenu(@NotNull ChestShop chestShop) {
        super(chestShop, ChestShopConfig.YML_LIST_OWN);
    }

    @Override
    @NotNull
    protected List<IShopChest> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.chestShop.getShops(player));
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IShopChest shop) {
        return (p, type, e) -> {
            if (e.isRightClick()) {
                shop.getEditor().open(p, 1);
                return;
            }

            if (!p.hasPermission(Perms.CHEST_TELEPORT)) {
                plugin.lang().Error_NoPerm.send(p);
                return;
            }

            shop.teleport(p);
        };
    }
}
