package su.nightexpress.nexshop.api.shop.handler;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;

public interface PluginItemHandler extends ItemHandler {

    @NotNull PluginItemPacker createPacker(@NotNull String itemId, int amount);

    @Nullable ItemStack createItem(@NotNull String itemId);

    @Nullable String getItemId(@NotNull ItemStack itemStack);

    boolean isValidId(@NotNull String itemId);
}
