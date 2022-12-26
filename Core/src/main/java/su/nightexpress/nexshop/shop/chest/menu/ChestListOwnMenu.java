package su.nightexpress.nexshop.shop.chest.menu;

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

public class ChestListOwnMenu extends AbstractChestListMenu {

    public ChestListOwnMenu(@NotNull ChestShopModule chestShop) {
        super(chestShop, JYML.loadOrExtract(chestShop.plugin(), chestShop.getPath() + "list.own.menu.yml"));
    }

    @Override
    @NotNull
    protected List<ChestShop> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.chestShop.getShops(player));
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull ChestShop shop) {
        return (p, type, e) -> {
            if (e.isRightClick()) {
                shop.getEditor().open(p, 1);
                return;
            }

            if (!p.hasPermission(Perms.CHEST_SHOP_TELEPORT)) {
                plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(p);
                return;
            }

            shop.teleport(p);
        };
    }
}
