package su.nightexpress.nexshop.shop.virtual.impl.product.specific;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.ItemProduct;
import su.nightexpress.nexshop.shop.virtual.util.ShopUtils;

public class ItemSpecific implements ProductSpecific, ItemProduct {

    private ItemStack item;
    private ItemStack preview;
    private     boolean   respectItemMeta;

    private final PlaceholderMap placeholderMap;

    public ItemSpecific(@NotNull ItemStack item) {
        this(item, item);
    }

    public ItemSpecific(@NotNull ItemStack item, @NotNull ItemStack preview) {
        this.setItem(item);
        this.setPreview(preview);
        this.setRespectItemMeta(item.hasItemMeta());

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.PRODUCT_ITEM_META_ENABLED, () -> LangManager.getBoolean(this.isRespectItemMeta()));
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
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
    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(this.preview);
    }

    @Override
    public void setPreview(@NotNull ItemStack preview) {
        this.preview = new ItemStack(preview);
    }

    public boolean isRespectItemMeta() {
        return this.respectItemMeta;
    }

    public void setRespectItemMeta(boolean respectItemMeta) {
        this.respectItemMeta = respectItemMeta;
    }

    @Override
    public int getUnitAmount() {
        return this.getItem().getAmount();
    }

    @NotNull
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    @Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        return ShopUtils.countItemSpace(inventory, this.getItem()) > 0;
    }
}
