package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;

public abstract class ShopView<S extends Shop<S, ?>> extends AbstractMenu<ExcellentShop> {

    protected final S shop;

    public ShopView(@NotNull S shop, @NotNull JYML cfg) {
        super(shop.plugin(), cfg, "");
        this.shop = shop;
    }

    @NotNull
    public S getShop() {
        return this.shop;
    }

    public abstract void displayProducts(@NotNull Player player, @NotNull Inventory inventory, int page);

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }

    @Override
    public boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        this.displayProducts(player, inventory, this.getPage(player));
        return true;
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUtil.replace(item, this.shop.replacePlaceholders());
    }
}
