package su.nightexpress.nexshop.data.price;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Product;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProductPriceStorage {

    private static final Map<String, Map<String, ProductPriceData>> DATAS = new ConcurrentHashMap<>();

    @NotNull
    public static CompletableFuture<Void> loadData(@NotNull Shop<?, ?> shop) {
        return loadData(shop.getId());
    }

    @NotNull
    private static CompletableFuture<Void> loadData(@NotNull String shopId) {
        DATAS.remove(shopId);
        return CompletableFuture.runAsync(() -> ShopAPI.getDataHandler().getProductPriceData(shopId).forEach(ProductPriceStorage::addData));
    }

    private static void addData(@NotNull ProductPriceData priceData) {
        DATAS.computeIfAbsent(priceData.getShopId(), k -> new ConcurrentHashMap<>()).put(priceData.getProductId(), priceData);
    }

    private static void removeData(@NotNull Product<?, ?, ?> product) {
        DATAS.getOrDefault(product.getShop().getId(), Collections.emptyMap()).remove(product.getId());
    }

    @Nullable
    public static ProductPriceData getData(@NotNull String shopId, @NotNull String productId) {
        return DATAS.getOrDefault(shopId, Collections.emptyMap()).get(productId);
    }

    public static void createData(@NotNull ProductPriceData priceData) {
        if (getData(priceData.getShopId(), priceData.getProductId()) != null) return;

        addData(priceData);

        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().createProductPriceData(priceData));
    }

    public static void saveData(@NotNull ProductPriceData priceData) {
        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().saveProductPriceData(priceData));
    }

    public static void deleteData(@NotNull Product<?, ?, ?> product) {
        if (getData(product.getShop().getId(), product.getId()) == null) return;

        removeData(product);

        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().removeProductPriceData(product));
    }
}
