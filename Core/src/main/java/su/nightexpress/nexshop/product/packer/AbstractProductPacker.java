package su.nightexpress.nexshop.product.packer;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.product.packer.impl.DummyPacker;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

public abstract class AbstractProductPacker<T extends ProductHandler> implements ProductPacker {

    protected final T handler;
    protected final PlaceholderMap placeholderMap;

    public AbstractProductPacker(@NotNull T handler) {
        this.handler = handler;
        this.placeholderMap = new PlaceholderMap();
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        if (!(this instanceof DummyPacker)) {
            config.remove("Content");
        }
        this.writeAdditional(config, path);
    }

    protected abstract void writeAdditional(@NotNull FileConfig config, @NotNull String path);

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    public T getHandler() {
        return handler;
    }
}
