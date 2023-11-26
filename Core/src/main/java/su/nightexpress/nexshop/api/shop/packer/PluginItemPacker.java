package su.nightexpress.nexshop.api.shop.packer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginItemPacker {

    @NotNull ItemStack createItem();

    boolean isValidId(@NotNull String itemId);

    @Nullable String getItemId(@NotNull ItemStack itemStack);
}
