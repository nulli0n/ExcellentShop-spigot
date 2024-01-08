package su.nightexpress.nexshop.api.shop.packer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemPacker extends ProductPacker {

    void load(@NotNull ItemStack item);

    @NotNull ItemStack getItem();

    void setItem(@NotNull ItemStack item);

    boolean isUsePreview();

    void setUsePreview(boolean usePreview);

    boolean isItemMatches(@NotNull ItemStack item);
}
