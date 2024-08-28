package su.nightexpress.nexshop.product.packer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.product.handler.AbstractPluginItemHandler;
import su.nightexpress.nightcore.config.FileConfig;

public abstract class AbstractPluginItemPacker<T extends AbstractPluginItemHandler> extends AbstractItemPacker<T> implements PluginItemPacker {

    protected final String itemId;
    protected final int    amount;

    public AbstractPluginItemPacker(@NotNull T handler, @NotNull String itemId, int amount) {
        super(handler);
        this.itemId = itemId;
        this.amount = Math.max(1, amount);
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Content.ItemId", this.getItemId());
        config.set(path + ".Content.Amount", this.getAmount());
    }

    @Override
    @NotNull
    public String getItemId() {
        return this.itemId;
    }

    private int getAmount() {
        return this.amount;
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        ItemStack item = this.createItem();
        if (item == null) {
            item = new ItemStack(INVALID_ITEM);
            this.getHandler().logBadItem(this.itemId);
        }
        else item.setAmount(this.getAmount());

        return item;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.getItem();
    }

    @Override
    public void setPreview(@NotNull ItemStack preview) {

    }

    @Override
    public boolean isItemMatches(@NotNull ItemStack item) {
        String itemId = this.handler.getItemId(item);
        return itemId != null && itemId.equalsIgnoreCase(this.getItemId());
    }

    @Override
    public int getUnitAmount() {
        return this.getAmount();
    }
}
