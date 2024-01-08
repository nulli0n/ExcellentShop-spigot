package su.nightexpress.nexshop.api.shop.handler;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.Product;

public interface ProductHandler {

    @NotNull ProductPacker createPacker();

    @NotNull String getName();

    void loadEditor(@NotNull EditorMenu<ExcellentShop, ? extends Product> menu, @NotNull Product product);
}
