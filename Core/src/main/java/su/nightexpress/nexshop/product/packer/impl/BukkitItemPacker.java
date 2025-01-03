package su.nightexpress.nexshop.product.packer.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.product.ProductHandlerRegistry;
import su.nightexpress.nexshop.product.handler.impl.BukkitItemHandler;
import su.nightexpress.nexshop.product.packer.AbstractItemPacker;
import su.nightexpress.nightcore.util.ItemNbt;

import java.util.function.UnaryOperator;

public class BukkitItemPacker extends AbstractItemPacker<BukkitItemHandler> {

    private ItemStack item;
    private boolean   respectItemMeta;

    public BukkitItemPacker(@NotNull BukkitItemHandler handler, @NotNull ItemStack item, boolean respectItemMeta) {
        super(handler);
        this.setItem(item);
        this.setRespectItemMeta(respectItemMeta);
    }

    @Override
    @Nullable
    public String serialize() {
        String tagString = ItemNbt.getTagString(this.item);
        if (tagString == null) return null;

        return tagString + BukkitItemHandler.DELIMITER + this.respectItemMeta;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.BUKKIT_ITEM_PACKER.replacer(this);
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

        return this.isRespectItemMeta() ? this.item.isSimilar(item) : this.item.getType() == item.getType();
    }

    public boolean isRespectItemMeta() {
        return this.respectItemMeta;
    }

    public void setRespectItemMeta(boolean respectItemMeta) {
        this.respectItemMeta = respectItemMeta;
    }
}
