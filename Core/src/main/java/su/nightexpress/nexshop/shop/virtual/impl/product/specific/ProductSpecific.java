package su.nightexpress.nexshop.shop.virtual.impl.product.specific;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;

public interface ProductSpecific extends Placeholder {

    @NotNull ItemStack getPreview();

    void setPreview(@NotNull ItemStack preview);

    boolean hasSpace(@NotNull Inventory inventory);

    void delivery(@NotNull Inventory inventory, int count);

    void take(@NotNull Inventory inventory, int count);

    int count(@NotNull Inventory inventory);

    int getUnitAmount();
}
