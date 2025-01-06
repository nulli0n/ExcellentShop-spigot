package su.nightexpress.nexshop.api.shop.product.typing;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PhysicalTyping extends ProductTyping {

    @NotNull String serialize();

    boolean isItemMatches(@NotNull ItemStack other);

    @NotNull ItemStack getItem();
}
