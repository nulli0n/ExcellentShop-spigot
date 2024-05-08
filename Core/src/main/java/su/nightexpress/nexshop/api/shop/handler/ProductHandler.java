package su.nightexpress.nexshop.api.shop.handler;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;

public interface ProductHandler {

    @NotNull ProductPacker createPacker();

    @NotNull String getName();
}
