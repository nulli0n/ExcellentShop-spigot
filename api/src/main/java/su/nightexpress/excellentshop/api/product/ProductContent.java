package su.nightexpress.excellentshop.api.product;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import su.nightexpress.nightcore.config.Writeable;

public abstract class ProductContent implements Writeable {

    protected final ContentType type;

    protected ProductContent(@NonNull ContentType type) {
        this.type = type;
    }

    @NonNull
    public ContentType type() {
        return this.type;
    }

    public abstract boolean isPhysical();

    public abstract boolean isItemMatches(@NonNull ItemStack itemStack);

    @NonNull
    public abstract String getName();

    public abstract boolean isValid();

    @NonNull
    public abstract ItemStack getPreview();

    public abstract boolean hasSpace(@NonNull Inventory inventory);

    public abstract int countSpace(@NonNull Inventory inventory);

    public abstract void delivery(@NonNull Inventory inventory, int count);

    public abstract void take(@NonNull Inventory inventory, int count);

    public abstract int count(@NonNull Inventory inventory);

    public abstract int getUnitAmount();

    public abstract int getMaxStackSize();
}
