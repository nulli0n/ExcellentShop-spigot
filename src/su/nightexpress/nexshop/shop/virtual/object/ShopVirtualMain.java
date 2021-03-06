package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class ShopVirtualMain extends AbstractMenu<ExcellentShop> {

    private final VirtualShop virtualShop;

    public ShopVirtualMain(@NotNull VirtualShop virtualShop, @NotNull JYML cfg, @NotNull String path) {
        super(virtualShop.plugin(), cfg, path);
        this.virtualShop = virtualShop;

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection(path + "Content")) {
            IMenuItem menuItem = cfg.getMenuItem(path + "Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection(path + "Shops")) {
            IMenuItem menuItem = cfg.getMenuItem(path + "Shops." + sId);

            IShopVirtual shop = virtualShop.getShopById(sId);
            if (shop == null) {
                plugin.error("Invalid shop item in the main menu: '" + sId + "' !");
                continue;
            }

            menuItem.setClick((p, type, e) -> {
                shop.open(p, 1);
            });

            this.addItem(menuItem);
        }
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        IShopVirtual shop = this.virtualShop.getShopById(menuItem.getId());
        if (shop == null) return;

        ItemUT.replace(item, shop.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
