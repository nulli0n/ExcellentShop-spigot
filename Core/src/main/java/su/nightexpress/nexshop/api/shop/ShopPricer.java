package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.data.object.PriceData;

public interface ShopPricer extends TransactionListener {

    void load();

    void updatePrices();

    void updatePrice(@NotNull Product product);

    @Nullable PriceData getData(@NotNull Product product);

    void saveData(@NotNull Product product);

    void deleteData(@NotNull Product product);

    void deleteData();
}
