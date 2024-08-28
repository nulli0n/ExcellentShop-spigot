package su.nightexpress.nexshop.product.handler.impl;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.product.handler.AbstractPluginItemHandler;
import su.nightexpress.nexshop.product.packer.impl.OraxenItemPacker;

public class OraxenItemHandler extends AbstractPluginItemHandler {

    public OraxenItemHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getName() {
        return HookId.ORAXEN;
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return OraxenItems.exists(item);
    }

    @Override
    @NotNull
    public OraxenItemPacker createPacker(@NotNull String itemId, int amount) {
        return new OraxenItemPacker(this, itemId, amount);
    }

    @Override
    public boolean isValidId(@NotNull String itemId) {
        return OraxenItems.exists(itemId);
    }

    @Override
    @Nullable
    public ItemStack createItem(@NotNull String itemId) {
        ItemBuilder builder = OraxenItems.getItemById(itemId);
        return builder == null ? null : builder.build();
    }

    @Override
    @Nullable
    public String getItemId(@NotNull ItemStack item) {
        return OraxenItems.getIdByItem(item);
    }
}
