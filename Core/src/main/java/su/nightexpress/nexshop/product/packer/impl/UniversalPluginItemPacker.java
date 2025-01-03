package su.nightexpress.nexshop.product.packer.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.product.handler.AbstractPluginItemHandler;
import su.nightexpress.nexshop.product.handler.AbstractProductHandler;
import su.nightexpress.nexshop.product.packer.AbstractItemPacker;

import java.util.function.UnaryOperator;

public class UniversalPluginItemPacker<T extends AbstractPluginItemHandler> extends AbstractItemPacker<T> implements PluginItemPacker {

    protected final String itemId;
    protected final int    amount;

    public UniversalPluginItemPacker(@NotNull T handler, @NotNull String itemId, int amount) {
        super(handler);
        this.itemId = itemId;
        this.amount = Math.max(1, amount);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return s -> s;
    }

    @Override
    @Nullable
    public String serialize() {
        return this.itemId + AbstractProductHandler.DELIMITER + this.amount;
    }

    @Override
    @Nullable
    public ItemStack createItem() {
        return this.handler.createItem(this.itemId);
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        ItemStack item = this.createItem();
        if (item == null) {
            item = new ItemStack(INVALID_ITEM);
            //this.handler.logBadItem(this.itemId);
        }
        else item.setAmount(this.getAmount());

        return item;
    }

    @Override
    public boolean isItemMatches(@NotNull ItemStack item) {
        String itemId = this.handler.getItemId(item);
        return itemId != null && itemId.equalsIgnoreCase(this.itemId);
    }

    @Override
    public int getUnitAmount() {
        return this.getAmount();
    }

    @Override
    @NotNull
    public String getItemId() {
        return this.itemId;
    }

    private int getAmount() {
        return this.amount;
    }
}
