package su.nightexpress.nexshop.shop.impl.packer;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OraxenItemPacker extends AbstractPluginItemPacker {

    public OraxenItemPacker() {
        this("null", 0);
    }

    public OraxenItemPacker(@NotNull String itemId, int amount) {
        super(itemId, amount);
    }

    @Override
    public boolean isValidId(@NotNull String itemId) {
        return OraxenItems.exists(itemId);
    }

    @Override
    @NotNull
    public ItemStack createItem() {
        return OraxenItems.getItemById(this.getItemId())/*.setAmount(this.getAmount())*/.build();
    }

    @Override
    @Nullable
    public String getItemId(@NotNull ItemStack item) {
        return OraxenItems.getIdByItem(item);
    }
}
