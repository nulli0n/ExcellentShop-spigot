package su.nightexpress.nexshop.api.shop.packer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginItemPacker {

    @Nullable ItemStack createItem();

    @NotNull String getItemId();
}
