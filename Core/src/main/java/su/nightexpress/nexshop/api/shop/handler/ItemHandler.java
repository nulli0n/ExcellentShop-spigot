package su.nightexpress.nexshop.api.shop.handler;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;

public interface ItemHandler extends ProductHandler {

    @Nullable ItemPacker createPacker(@NotNull ItemStack itemStack);

    boolean canHandle(@NotNull ItemStack item);
}
