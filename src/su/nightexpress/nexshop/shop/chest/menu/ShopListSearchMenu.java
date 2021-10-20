package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;

import java.util.ArrayList;
import java.util.List;

public class ShopListSearchMenu extends AbstractShopListMenu {

    public ShopListSearchMenu(@NotNull ChestShop chestShop) {
        super(chestShop, ChestShopConfig.YML_LIST_SEARCH);
    }

    @Override
    protected @NotNull List<IShopChest> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.chestShop.getShopsSearched(player));
    }

    @Override
    protected @NotNull IMenuClick getObjectClick(@NotNull Player player, @NotNull IShopChest shop) {
        return (player1, type, e) -> {
            shop.open(player1, 1);
        };
    }
}
