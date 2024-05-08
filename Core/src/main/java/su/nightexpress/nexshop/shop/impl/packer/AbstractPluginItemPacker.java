package su.nightexpress.nexshop.shop.impl.packer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nightcore.config.FileConfig;

public abstract class AbstractPluginItemPacker extends AbstractItemPacker implements PluginItemPacker {

    protected String itemId;
    protected int amount;

    public AbstractPluginItemPacker() {
        this("null", 0);
    }

    public AbstractPluginItemPacker(@NotNull String itemId, int amount) {
        super(new ItemStack(Material.AIR));
        this.setUsePreview(false);
        this.setItemId(itemId);
        this.setAmount(amount);
    }

    @Override
    public boolean load(@NotNull FileConfig cfg, @NotNull String path) {
        String itemId = cfg.getString(path + ".Content.ItemId");
        if (itemId == null/* || !this.isValidId(itemId)*/) {
            return false;
        }
        int amount = cfg.getInt(path + ".Content.Amount");

        this.setItemId(itemId);
        this.setAmount(amount);
        return true;
    }

    @Override
    public void load(@NotNull ItemStack item) {
        this.setItem(item);
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig cfg, @NotNull String path) {
        cfg.set(path + ".Content.ItemId", this.getItemId());
        cfg.set(path + ".Content.Amount", this.getAmount());
    }

    @Override
    @NotNull
    public String getItemId() {
        return itemId;
    }

    public void setItemId(@NotNull String itemId) {
        this.itemId = itemId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(1, amount);
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        ItemStack item = this.createItem();
        item.setAmount(this.getAmount());
        return item;
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        String itemId = this.getItemId(item);
        if (itemId == null) return;

        this.setItemId(itemId);
        this.setAmount(item.getAmount());
    }

    @Override
    public boolean isItemMatches(@NotNull ItemStack item) {
        String itemId = this.getItemId(item);
        return itemId != null && itemId.equalsIgnoreCase(this.getItemId());
    }

    @Override
    public int getUnitAmount() {
        return this.getAmount();
    }
}
