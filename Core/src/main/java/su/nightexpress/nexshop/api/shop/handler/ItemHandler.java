package su.nightexpress.nexshop.api.shop.handler;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemHandler extends ProductHandler {

    boolean canHandle(@NotNull ItemStack item);
}
