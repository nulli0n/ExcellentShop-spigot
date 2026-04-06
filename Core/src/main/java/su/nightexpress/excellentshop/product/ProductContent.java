package su.nightexpress.excellentshop.product;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
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

    public abstract boolean isPhysical();

    public abstract boolean isItemMatches(@NonNull ItemStack itemStack);

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

    public abstract int getMaxStackSize();
}
