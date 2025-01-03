package su.nightexpress.nexshop.api.shop.packer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginItemPacker extends ItemPacker {

    @Nullable ItemStack createItem();

    @NotNull String getItemId();
}
