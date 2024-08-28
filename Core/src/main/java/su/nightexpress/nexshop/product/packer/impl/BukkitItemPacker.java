package su.nightexpress.nexshop.product.packer.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.ProductHandlerRegistry;
import su.nightexpress.nexshop.product.packer.AbstractItemPacker;
import su.nightexpress.nexshop.product.handler.impl.BukkitItemHandler;
import su.nightexpress.nightcore.config.FileConfig;

public class BukkitItemPacker extends AbstractItemPacker<BukkitItemHandler> {

    private ItemStack item;
    private ItemStack preview;
    private boolean   respectItemMeta;

    public BukkitItemPacker(@NotNull BukkitItemHandler handler, @NotNull ItemStack item, @NotNull ItemStack preview, boolean respectItemMeta) {
        super(handler);
        this.setItem(item);
        this.setPreview(preview);
        this.setRespectItemMeta(respectItemMeta);

        this.placeholderMap
            .add(Placeholders.PRODUCT_ITEM_META_ENABLED, () -> Lang.getYesOrNo(this.isRespectItemMeta()));
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        config.setItemEncoded(path + ".Content.Preview", this.getPreview());
        config.setItemEncoded(path + ".Content.Item", this.getItem());
        config.set(path + ".Item_Meta_Enabled", this.isRespectItemMeta());
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

    @NotNull
    @Override
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    private void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    @Override
    public boolean isItemMatches(@NotNull ItemStack item) {
        ProductHandler itemHandler = ProductHandlerRegistry.getHandler(item);
        if (itemHandler != this.handler) return false;

        return this.isRespectItemMeta() ? this.getItem().isSimilar(item) : this.getItem().getType() == item.getType();
    }

    public boolean isRespectItemMeta() {
        return this.respectItemMeta;
    }

    public void setRespectItemMeta(boolean respectItemMeta) {
        this.respectItemMeta = respectItemMeta;
    }
}
