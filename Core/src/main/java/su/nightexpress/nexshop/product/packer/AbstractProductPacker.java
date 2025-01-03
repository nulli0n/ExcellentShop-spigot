package su.nightexpress.nexshop.product.packer;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nightcore.config.FileConfig;

public abstract class AbstractProductPacker<T extends ProductHandler> implements ProductPacker {

    protected final T handler;

    public AbstractProductPacker(@NotNull T handler) {
        this.handler = handler;
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.remove(path + ".Content");
        config.set(path + ".Data", this.serialize());
    }

    @Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        int space = this.countSpace(inventory);
        return space != 0;
    }

    @NotNull
    public T getHandler() {
        return this.handler;
    }

    @Override
    public boolean isDummy() {
        return false;
    }
}
