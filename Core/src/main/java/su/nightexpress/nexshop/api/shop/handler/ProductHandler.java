package su.nightexpress.nexshop.api.shop.handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nightcore.config.FileConfig;

public interface ProductHandler {

    @NotNull ProductPacker readPacker(@NotNull FileConfig config, @NotNull String path);

    @NotNull ProductPacker deserialize(@NotNull String str);

    @NotNull String getName();
}
