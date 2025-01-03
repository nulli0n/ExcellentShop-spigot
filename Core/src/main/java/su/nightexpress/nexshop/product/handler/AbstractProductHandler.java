package su.nightexpress.nexshop.product.handler;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nightcore.config.FileConfig;

public abstract class AbstractProductHandler implements ProductHandler {

    public static final String DELIMITER = " | ";

    protected final ShopPlugin plugin;

    public AbstractProductHandler(@NotNull ShopPlugin plugin) {
        this.plugin = plugin;
    }
}
