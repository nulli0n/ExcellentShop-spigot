package su.nightexpress.nexshop.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;

public abstract class AbstractShopView<T extends IShop> extends AbstractMenu<ExcellentShop> {

    protected final @NotNull T shop;

    public AbstractShopView(@NotNull T shop, @NotNull JYML cfg) {
        super(shop.plugin(), cfg, "");
        this.shop = shop;
    }

    @NotNull
    public T getShop() {
        return this.shop;
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        this.displayProducts(player, inventory, this.getPage(player));
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    public abstract void displayProducts(@NotNull Player player, @NotNull Inventory inventory, int page);

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUT.replace(item, this.shop.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
