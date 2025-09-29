package su.nightexpress.nexshop.product.content;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.Writeable;

public abstract class ProductContent implements Writeable {

    protected final ContentType type;

    public ProductContent(@NotNull ContentType type) {
        this.type = type;
    }

    @NotNull
    public ContentType type() {
        return this.type;
    }

    @NotNull
    public abstract String getName();

    public abstract boolean isValid();

    @NotNull
    public abstract ItemStack getPreview();

    public abstract boolean hasSpace(@NotNull Inventory inventory);

    public abstract int countSpace(@NotNull Inventory inventory);

    public abstract void delivery(@NotNull Inventory inventory, int count);

    public abstract void take(@NotNull Inventory inventory, int count);

    public abstract int count(@NotNull Inventory inventory);

    public abstract int getUnitAmount();
}
