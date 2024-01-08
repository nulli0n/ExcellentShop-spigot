package su.nightexpress.nexshop.shop.impl.packer;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.shop.impl.AbstractProductPacker;
import su.nightexpress.nexshop.shop.util.ShopUtils;

public abstract class AbstractItemPacker extends AbstractProductPacker implements ItemPacker {

    protected boolean usePreview;

    public AbstractItemPacker(@NotNull ItemStack preview) {
        this(preview, preview);
    }

    public AbstractItemPacker(@NotNull ItemStack preview, @NotNull ItemStack item) {
        this(preview, item, true);
    }

    public AbstractItemPacker(@NotNull ItemStack preview, @NotNull ItemStack item, boolean usePreview) {
        super(preview);
        this.setItem(item);
        this.setUsePreview(usePreview);
    }

    @Override
    public void delivery(@NotNull Inventory inventory, int count) {
        int amount = this.getUnitAmount() * count;
        ShopUtils.addItem(inventory, this.getItem(), amount);
    }

    @Override
    public void take(@NotNull Inventory inventory, int count) {
        int amount = this.getUnitAmount() * count;
        ShopUtils.takeItem(inventory, this::isItemMatches, amount);
    }

    @Override
    public int count(@NotNull Inventory inventory) {
        return ShopUtils.countItem(inventory, this::isItemMatches);
    }

    @Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        return ShopUtils.countItemSpace(inventory, this::isItemMatches, this.getItem().getMaxStackSize()) > 0;
    }

    @Override
    public int getUnitAmount() {
        return this.getItem().getAmount();
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.isUsePreview() ? super.getPreview() : this.getItem();
    }

    @Override
    public boolean isUsePreview() {
        return usePreview;
    }

    @Override
    public void setUsePreview(boolean usePreview) {
        this.usePreview = usePreview;
    }
}
