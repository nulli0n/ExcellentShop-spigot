package su.nightexpress.nexshop.product.handler;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nightcore.config.FileConfig;

public abstract class AbstractProductHandler implements ProductHandler {

    protected final ShopPlugin plugin;

    public AbstractProductHandler(@NotNull ShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void logBadItem(@NotNull String itemId) {
        this.plugin.error("Invalid item '" + itemId + "' in the '" + this.getName() + "' handler.");
    }

    public void logBadItem(@NotNull String itemId, @NotNull FileConfig config, @NotNull String path) {
        this.plugin.error("Invalid item '" + itemId + "' in the '" + this.getName() + "' handler. Caused by: '" + config.getFile().getName() + "' -> '" + path + "'.");
    }
}
