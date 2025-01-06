package su.nightexpress.nexshop.api.shop.product.typing;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nightcore.config.Writeable;

import java.util.function.UnaryOperator;

public interface ProductTyping extends Writeable {

    @NotNull ProductType type();

    @NotNull String getName();

    @NotNull UnaryOperator<String> replacePlaceholders();

    boolean isValid();

    @NotNull ItemStack getPreview();

    boolean hasSpace(@NotNull Inventory inventory);

    int countSpace(@NotNull Inventory inventory);

    void delivery(@NotNull Inventory inventory, int count);

    void take(@NotNull Inventory inventory, int count);

    int count(@NotNull Inventory inventory);

    int getUnitAmount();
}
