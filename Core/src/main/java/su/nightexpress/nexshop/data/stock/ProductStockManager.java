package su.nightexpress.nexshop.data.stock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.user.ShopUser;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProductStockManager {

    private static final Map<String, Map<String, Map<StockType, Map<TradeType, ProductStockData>>>> DATAS = new ConcurrentHashMap<>();

    @NotNull
    public static CompletableFuture<Void> loadData(@NotNull Shop<?, ?> shop) {
        return loadData(shop.getId());
    }

    @NotNull
    public static CompletableFuture<Void> loadData(@NotNull ShopUser user) {
        return loadData(user.getId().toString());
    }

    public static void unloadData(@NotNull ShopUser user) {
        DATAS.remove(user.getId().toString());
    }

    @NotNull
    private static CompletableFuture<Void> loadData(@NotNull String holder) {
        DATAS.remove(holder);

        return CompletableFuture.runAsync(() -> {
            ShopAPI.getDataHandler().getProductStockData(holder).forEach(stockData -> {
                addData(holder, stockData);
            });
        });
    }

    private static void addData(@NotNull String holder, @NotNull ProductStockData stockData) {
        DATAS.computeIfAbsent(holder, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(stockData.getProductId(), k -> new ConcurrentHashMap<>())
            .computeIfAbsent(stockData.getStockType(), k -> new ConcurrentHashMap<>())
            .put(stockData.getTradeType(), stockData);
    }

    private static void removeData(@NotNull String holder, @NotNull Product<?, ?, ?> product,
                                   @NotNull StockType stockType, @NotNull TradeType tradeType) {
        DATAS.getOrDefault(holder, Collections.emptyMap())
            .getOrDefault(product.getId(), Collections.emptyMap())
            .getOrDefault(stockType, Collections.emptyMap()).remove(tradeType);
    }

    /*@Nullable
    public static ProductStockData getData(@NotNull ShopUser user, @NotNull IProduct product,
                                           @NotNull IProductStock.StockType stockType, @NotNull TradeType tradeType) {
        return getData(user.getId().toString(), product.getId(), stockType, tradeType);
    }

    @Nullable
    public static ProductStockData getData(@NotNull IProduct product,
                                           @NotNull IProductStock.StockType stockType, @NotNull TradeType tradeType) {
        return getData(product.getShop().getId(), product.getId(), stockType, tradeType);
    }*/

    @Nullable
    public static ProductStockData getData(@NotNull String holder, @NotNull String productId,
                                           @NotNull StockType stockType, @NotNull TradeType tradeType) {
        return DATAS.getOrDefault(holder, Collections.emptyMap())
            .getOrDefault(productId, Collections.emptyMap())
            .getOrDefault(stockType, Collections.emptyMap()).get(tradeType);
    }

    /*public static void createProductStockData(@NotNull ShopUser user, @NotNull ProductStockData stockData) {
        createProductStockData(user.getId().toString(), stockData);
    }

    public static void createProductStockData(@NotNull IProduct product, @NotNull ProductStockData stockData) {
        createProductStockData(product.getShop().getId(), stockData);
    }*/

    public static void createProductStockData(@NotNull String holder, @NotNull ProductStockData stockData) {
        if (getData(holder, stockData.getProductId(), stockData.getStockType(), stockData.getTradeType()) != null) return;

        addData(holder, stockData);

        ShopAPI.PLUGIN.runTask(c -> {
            ShopAPI.getDataHandler().createProductStockData(holder, stockData);
        }, true);
    }

    /*public static void saveProductStockData(@NotNull ShopUser user, @NotNull ProductStockData stockData) {
        saveProductStockData(user.getId().toString(), stockData);
    }

    public static void saveProductStockData(@NotNull IProduct product, @NotNull ProductStockData stockData) {
        saveProductStockData(product.getShop().getId(), stockData);
    }*/

    public static void saveProductStockData(@NotNull String holder, @NotNull ProductStockData stockData) {
        ShopAPI.PLUGIN.runTask(c -> {
            ShopAPI.getDataHandler().saveProductStockData(holder, stockData);
        }, true);
    }

    /*public static void removeProductStockData(@NotNull ShopUser user, @NotNull IProduct product,
                                              @NotNull IProductStock.StockType stockType, @NotNull TradeType tradeType) {
        removeProductStockData(user.getId().toString(), product, stockType, tradeType);
    }

    public static void removeProductStockData(@NotNull IProduct product,
                                              @NotNull IProductStock.StockType stockType, @NotNull TradeType tradeType) {
        removeProductStockData(product.getShop().getId(), product, stockType, tradeType);
    }*/

    public static void removeProductStockData(@NotNull String holder, @NotNull Product<?, ?, ?> product,
                                              @NotNull StockType stockType, @NotNull TradeType tradeType) {
        if (getData(holder, product.getId(), stockType, tradeType) == null) return;

        removeData(holder, product, stockType, tradeType);

        ShopAPI.PLUGIN.runTask(c -> {
            ShopAPI.getDataHandler().removeProductStockData(holder, product, stockType, tradeType);
        }, true);
    }
}
