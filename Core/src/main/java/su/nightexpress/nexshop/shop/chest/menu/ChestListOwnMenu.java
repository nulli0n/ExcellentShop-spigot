package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShop;

import java.util.ArrayList;
import java.util.List;

public class ChestListOwnMenu extends AbstractChestListMenu {

    public ChestListOwnMenu(@NotNull ChestShop chestShop) {
        super(chestShop, JYML.loadOrExtract(chestShop.plugin(), chestShop.getPath() + "list.own.menu.yml"));
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
                plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(p);
                return;
            }

            shop.teleport(p);
        };
    }
}
