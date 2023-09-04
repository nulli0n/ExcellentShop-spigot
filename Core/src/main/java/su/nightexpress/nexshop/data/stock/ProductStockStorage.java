package su.nightexpress.nexshop.data.stock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProductStockStorage {

    private static final Map<String, Map<String, Map<StockType, Map<TradeType, ProductStockData>>>> DATAS = new ConcurrentHashMap<>();

    @NotNull
    public static CompletableFuture<Void> loadData(@NotNull VirtualShop<?, ?> shop) {
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
            ShopAPI.getDataHandler().getVirtualDataHandler().getProductStockData(holder).forEach(stockData -> {
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

    private static void removeData(@NotNull String holder, @NotNull VirtualProduct<?, ?> product,
                                   @NotNull StockType stockType, @NotNull TradeType tradeType) {
        DATAS.getOrDefault(holder, Collections.emptyMap())
            .getOrDefault(product.getId(), Collections.emptyMap())
            .getOrDefault(stockType, Collections.emptyMap()).remove(tradeType);
    }

    @Nullable
    public static ProductStockData getData(@NotNull String holder, @NotNull String productId,
                                           @NotNull StockType stockType, @NotNull TradeType tradeType) {
        return DATAS.getOrDefault(holder, Collections.emptyMap())
            .getOrDefault(productId, Collections.emptyMap())
            .getOrDefault(stockType, Collections.emptyMap()).get(tradeType);
    }

    public static void createData(@NotNull String holder, @NotNull ProductStockData stockData) {
        if (getData(holder, stockData.getProductId(), stockData.getStockType(), stockData.getTradeType()) != null) return;

        addData(holder, stockData);

        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().getVirtualDataHandler().createProductStockData(holder, stockData));
    }

    public static void saveData(@NotNull String holder, @NotNull ProductStockData stockData) {
        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().getVirtualDataHandler().saveProductStockData(holder, stockData));
    }

    public static void deleteData(@NotNull String holder, @NotNull VirtualProduct<?, ?> product,
                                  @NotNull StockType stockType, @NotNull TradeType tradeType) {
        if (getData(holder, product.getId(), stockType, tradeType) == null) return;

        removeData(holder, product, stockType, tradeType);

        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().getVirtualDataHandler().removeProductStockData(holder, product, stockType, tradeType));
    }

    public static void deleteData(@NotNull VirtualProduct<?, ?> product) {
        //DATAS.values().forEach(map -> map.remove(product.getId()));
        DATAS.values().forEach(prodMap -> {
            prodMap.values().forEach(stockMap -> {
                stockMap.values().forEach(tradeMap -> {
                    tradeMap.values().removeIf(data -> data.getShopId().equalsIgnoreCase(product.getShop().getId()) && data.getProductId().equalsIgnoreCase(product.getId()));
                });
            });
        });

        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().getVirtualDataHandler().removeProductStockData(product));
    }

    public static void deleteData(@NotNull VirtualShop<?, ?> shop) {
        DATAS.remove(shop.getId());
        DATAS.values().forEach(prodMap -> {
            prodMap.values().forEach(stockMap -> {
                stockMap.values().forEach(tradeMap -> {
                    tradeMap.values().removeIf(data -> data.getShopId().equalsIgnoreCase(shop.getId()));
                });
            });
        });

        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().getVirtualDataHandler().removeShopStockData(shop));
    }
}
