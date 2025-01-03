package su.nightexpress.nexshop.product.handler.impl;

import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.product.handler.AbstractPluginItemHandler;
import su.nightexpress.nexshop.product.packer.impl.UniversalPluginItemPacker;

import java.util.Optional;

public class ExecutableItemsHandler extends AbstractPluginItemHandler {

    public ExecutableItemsHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getName() {
        return HookId.EXECUTABLE_ITEMS;
    }

    @Override
    @NotNull
    public PluginItemPacker createPacker(@NotNull String itemId, int amount) {
        return new UniversalPluginItemPacker<>(this, itemId, amount);
    }

    @Override
    @Nullable
    public ItemStack createItem(@NotNull String itemId) {
        var item = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(itemId).orElse(null);
        return item == null ? null : item.buildItem(1, Optional.empty());
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(item).isPresent();
    }

    @Override
    public boolean isValidId(@NotNull String itemId) {
        return ExecutableItemsAPI.getExecutableItemsManager().isValidID(itemId);
    }

    @Override
    @Nullable
    public String getItemId(@NotNull ItemStack item) {
        return ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(item).map(ExecutableItemInterface::getId).orElse(null);
    }
}
