package su.nightexpress.nexshop.api.shop.handler;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nightcore.config.FileConfig;

public interface ProductHandler {

    @NotNull ProductPacker createPacker(@NotNull FileConfig config, @NotNull String path);

    @NotNull String getName();
}
