package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.Product;

public interface ShopPricer extends TransactionListener {

    void updatePrices();

    void updatePrice(@NotNull Product product);

    //@Nullable PriceData getData(@NotNull Product product);

    //void addData(@NotNull PriceData data);

    void saveData(@NotNull Product product);

    void deleteData(@NotNull Product product);

    void deleteData();
}
