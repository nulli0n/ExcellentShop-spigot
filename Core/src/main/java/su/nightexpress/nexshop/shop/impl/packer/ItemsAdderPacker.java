package su.nightexpress.nexshop.shop.impl.packer;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderPacker extends AbstractPluginItemPacker {

    public ItemsAdderPacker() {
        this("null", 0);
    }

    public ItemsAdderPacker(@NotNull String itemId, int amount) {
        super(itemId, amount);
    }

    @Override
    public boolean isValidId(@NotNull String itemId) {
        return CustomStack.isInRegistry(itemId);
    }

    @Override
    @NotNull
    public ItemStack createItem() {
        return CustomStack.getInstance(this.getItemId()).getItemStack();
    }

    @Override
    @Nullable
    public String getItemId(@NotNull ItemStack item) {
        if (item.getType().isAir()) return null;

        CustomStack stack = CustomStack.byItemStack(item);
        return stack == null ? null : stack.getNamespacedID();
    }
}
