package su.nightexpress.nexshop.api.shop.packer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemPacker extends ProductPacker {

    @NotNull ItemStack getItem();

    boolean isItemMatches(@NotNull ItemStack item);
}
