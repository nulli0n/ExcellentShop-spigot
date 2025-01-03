package su.nightexpress.nexshop.product.handler.impl;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.product.handler.AbstractPluginItemHandler;
import su.nightexpress.nexshop.product.packer.impl.UniversalPluginItemPacker;

public class NexoItemHandler extends AbstractPluginItemHandler {

    public NexoItemHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getName() {
        return HookId.NEXO;
    }

    @Override
    @NotNull
    public PluginItemPacker createPacker(@NotNull String itemId, int amount) {
        return new UniversalPluginItemPacker<>(this, itemId, amount);
    }

    @Override
    @Nullable
    public ItemStack createItem(@NotNull String itemId) {
        ItemBuilder builder = NexoItems.itemFromId(itemId);
        return builder == null ? null : builder.build();
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return NexoItems.exists(item);
    }

    @Override
    public boolean isValidId(@NotNull String itemId) {
        return NexoItems.exists(itemId);
    }

    @Override
    @Nullable
    public String getItemId(@NotNull ItemStack item) {
        return NexoItems.idFromItem(item);
    }
}
