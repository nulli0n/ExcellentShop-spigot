package su.nightexpress.nexshop.shop.virtual.menu;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.Menu;

public interface ShopEditor extends Menu {

    default void save(@NotNull MenuViewer viewer, @NotNull Shop shop) {
        shop.saveSettings();
    }

    default void saveAndFlush(@NotNull MenuViewer viewer, @NotNull Shop shop) {
        this.save(viewer, shop);
        this.doFlush(viewer);
    }

    /*default void save(@NotNull MenuViewer viewer, @NotNull Shop shop) {
        shop.save();
    }

    default void saveAndFlush(@NotNull MenuViewer viewer, @NotNull Shop shop) {
        this.save(viewer, shop);
        this.doFlush(viewer);
    }*/


    default void saveProducts(@NotNull MenuViewer viewer, @NotNull Shop shop) {
        shop.saveProducts();
    }

    default void saveProductsAndFlush(@NotNull MenuViewer viewer, @NotNull Shop shop) {
        this.saveProducts(viewer, shop);
        this.doFlush(viewer);
    }


    default void saveProduct(@NotNull MenuViewer viewer, @NotNull Product product) {
        product.getShop().saveProduct(product);
    }

    default void saveProductAndFlush(@NotNull MenuViewer viewer, @NotNull Product product) {
        this.saveProduct(viewer, product);
        this.doFlush(viewer);
    }


    default void doFlush(@NotNull MenuViewer viewer) {
        this.runNextTick(() -> this.flush(viewer));
    }
}
