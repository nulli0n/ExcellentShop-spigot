package su.nightexpress.excellentshop.data;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.data.state.Stateful;
import su.nightexpress.excellentshop.api.data.state.StatefulData;
import su.nightexpress.excellentshop.data.legacy.*;
import su.nightexpress.excellentshop.shop.data.ProductLimitData;
import su.nightexpress.excellentshop.shop.data.ProductPriceData;
import su.nightexpress.excellentshop.shop.data.ProductStockData;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.data.RotationData;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.core.Config;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.Rotation;
import su.nightexpress.nightcore.manager.AbstractManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DataManager extends AbstractManager<ShopPlugin> {

    private final DataHandler dataHandler;

    // LEGACY - START

    private final Map<LegacyProductKey, LegacyStockData>     legacyStockDataMap;
    private final Map<LegacyProductKey, LegacyPriceData>     legacyPriceDataMap;
    private final Map<LegacyRotationKey, LegacyRotationData> legacyRotationDataMap;

    // LEGACY - END

    private final Map<UUID, ProductPriceData>            priceDataById;
    private final Map<UUID, ProductStockData>            stockDataById;
    private final Map<UUID, Map<UUID, ProductLimitData>> limitDataById;
    private final Map<UUID, RotationData>                rotationDataById;

    private boolean loaded;

    public DataManager(@NonNull ShopPlugin plugin, @NonNull DataHandler dataHandler) {
        super(plugin);
        this.dataHandler = dataHandler;

        // LEGACY - START
        this.legacyStockDataMap = new ConcurrentHashMap<>();
        this.legacyPriceDataMap = new ConcurrentHashMap<>();
        this.legacyRotationDataMap = new ConcurrentHashMap<>();
        // LEGACY - END

        this.priceDataById = new ConcurrentHashMap<>();
        this.stockDataById = new ConcurrentHashMap<>();
        this.limitDataById = new ConcurrentHashMap<>();
        this.rotationDataById = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.addAsyncTask(this::saveData, Config.DATA_SAVE_INTERVAL.get());
    }

    @Override
    protected void onShutdown() {
        this.saveData();
        this.clear();
    }

    public void clear() {
        this.legacyStockDataMap.clear();
        this.legacyPriceDataMap.clear();
        this.legacyRotationDataMap.clear();

        this.priceDataById.clear();
        this.stockDataById.clear();
        this.limitDataById.clear();
        this.rotationDataById.clear();

        this.loaded = false;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    @NonNull
    public CompletableFuture<Void> loadAllData() {
        return CompletableFuture.runAsync(() -> {
            // LEGACY - START
            if (this.dataHandler.hasLegacyStocks()) {
                this.dataHandler.loadLegacyStockDatas().forEach(data -> {
                    LegacyProductKey key = new LegacyProductKey(data.getShopId(), data.getProductId(), data.getHolder());
                    this.legacyStockDataMap.put(key, data);
                });
            }
            if (this.dataHandler.hasLegacyPrices()) {
                this.dataHandler.loadLegacyPriceDatas().forEach(data -> {
                    LegacyProductKey key = new LegacyProductKey(data.getShopId(), data.getProductId(), data.getShopId());
                    this.legacyPriceDataMap.put(key, data);
                });
            }
            if (this.dataHandler.isHasLegacyRotations()) {
                this.dataHandler.loadLegacyRotationDatas().forEach(data -> {
                    LegacyRotationKey key = new LegacyRotationKey(data.getShopId(), data.getRotationId());
                    this.legacyRotationDataMap.put(key, data);
                });
            }
            // LEGACY - END

            this.loadStockDatas();
            this.loadLimitDatas();
            this.loadPriceDatas();
            this.loadRotationDatas();

            this.dataHandler.addPriceDataSync(this::loadPriceData);
            this.dataHandler.addStockDataSync(this::loadStockData);
            this.dataHandler.addLimitDataSync(this::loadLimitData);
            this.dataHandler.addRotationDataSync(this::loadRotationData);

            this.loaded = true;
        });
    }



    private void loadPriceDatas() {
        this.dataHandler.loadPriceDatas().forEach(this::loadPriceData);
    }

    public void loadPriceData(@NonNull ProductPriceData data) {
        this.priceDataById.put(data.getProductId(), data);
    }



    private void loadStockDatas() {
        this.dataHandler.loadStockDatas().forEach(this::loadStockData);
    }

    public void loadStockData(@NonNull ProductStockData data) {
        this.stockDataById.put(data.getProductId(), data);
    }



    private void loadLimitDatas() {
        this.dataHandler.loadLimitDatas().forEach(this::loadLimitData);
    }

    public void loadLimitData(@NonNull ProductLimitData data) {
        this.limitDataById.computeIfAbsent(data.getProductId(), k -> new ConcurrentHashMap<>()).put(data.getPlayerId(), data);
    }



    private void loadRotationDatas() {
        this.dataHandler.loadRotationDatas().forEach(this::loadRotationData);
    }

    public void loadRotationData(@NonNull RotationData data) {
        this.rotationDataById.put(data.getRotationId(), data);
    }


    public void saveData() {
        // Insert, Update or Delete datas.
        this.saveDataOrRemove(this.getPriceDatas(), DataHandler::upsertPriceData, DataHandler::deletePriceData);
        this.saveDataOrRemove(this.getStockDatas(), DataHandler::upsertStockData, DataHandler::deleteStockData);
        this.saveDataOrRemove(this.getLimitDatas(), DataHandler::upsertLimitData, DataHandler::deleteLimitData);
        this.saveDataOrRemove(this.getRotationDatas(), DataHandler::upsertRotationData, DataHandler::deleteRotationData);

        // Clean up from "removed" datas.
        this.priceDataById.values().removeIf(StatefulData::isRemoved);
        this.stockDataById.values().removeIf(StatefulData::isRemoved);
        this.rotationDataById.values().removeIf(StatefulData::isRemoved);
        this.limitDataById.values().removeIf(inner -> {
            inner.values().removeIf(StatefulData::isRemoved);
            return inner.isEmpty();
        });

        // LEGACY - START
        Set<LegacyStockData> oldStocks = this.legacyStockDataMap.values().stream().filter(LegacyStockData::isRemoved).collect(Collectors.toSet());
        if (!oldStocks.isEmpty()) {
            this.dataHandler.deleteLegacyStockData(oldStocks);
            this.legacyStockDataMap.values().removeAll(oldStocks);
        }

        Set<LegacyPriceData> oldPrices = this.legacyPriceDataMap.values().stream().filter(LegacyPriceData::isRemoved).collect(Collectors.toSet());
        if (!oldPrices.isEmpty()) {
            this.dataHandler.deleteLegacyPriceData(oldPrices);
            this.legacyPriceDataMap.values().removeAll(oldPrices);
        }

        Set<LegacyRotationData> oldRotations = this.legacyRotationDataMap.values().stream().filter(LegacyRotationData::isRemoved).collect(Collectors.toSet());
        if (!oldRotations.isEmpty()) {
            this.dataHandler.deleteLegacyRotationData(oldRotations);
            this.legacyRotationDataMap.values().removeAll(oldRotations);
        }
        // LEGACY - END
    }

    private <T extends Stateful> void saveDataOrRemove(@NonNull Collection<T> originDatas,
                                                       @NonNull BiConsumer<DataHandler, Set<T>> onSave,
                                                       @NonNull BiConsumer<DataHandler, Set<T>> onDelete) {

        Set<T> toSave = new HashSet<>();
        Set<T> toRemove = new HashSet<>();

        originDatas.forEach(data -> {
            if (data.isRemoved()) {
                toRemove.add(data);
            }
            else if (data.isDirty()) {
                toSave.add(data);
                data.markClean();
            }
        });

        if (!toSave.isEmpty()) {
            onSave.accept(this.dataHandler, toSave);
        }
        if (!toRemove.isEmpty()) {
            onDelete.accept(this.dataHandler, toRemove);
        }
    }



    @NonNull
    public Set<RotationData> getRotationDatas() {
        return Set.copyOf(this.rotationDataById.values());
    }

    @Nullable
    public RotationData getRotationData(@NonNull Shop shop, @NonNull Rotation rotation) {
        RotationData data = this.getRotationData(rotation.getGlobalId());
        if (data != null) return data;

        // LEGACY - START
        LegacyRotationKey key = new LegacyRotationKey(shop.getId(), rotation.getId());
        LegacyRotationData oldData = this.legacyRotationDataMap.get(key);
        if (oldData != null && !oldData.isRemoved()) {
            oldData.setRemoved(true);

            RotationData newData = new RotationData(rotation.getGlobalId(), oldData.getNextRotationDate(), oldData.getProducts());
            this.loadRotationData(newData);
            this.plugin.debug("Replaced old rotation data for shop rotation: %s [%s]".formatted(shop.getId(), rotation.getGlobalId()));
            return newData;
        }
        // LEGACY - END

        return null;
    }

    @Nullable
    public RotationData getRotationData(@NonNull UUID rotationId) {
        return this.rotationDataById.get(rotationId);
    }



    @NonNull
    public Set<ProductPriceData> getPriceDatas() {
        return Set.copyOf(this.priceDataById.values());
    }

    @Nullable
    public ProductPriceData getPriceData(@NonNull Product product) {
        ProductPriceData data = this.getPriceData(product.getGlobalId());
        if (data != null) return data;

        // LEGACY - START
        LegacyProductKey key = LegacyProductKey.global(product);
        LegacyPriceData oldData = this.legacyPriceDataMap.get(key);
        if (oldData != null && !oldData.isRemoved()) {
            oldData.setRemoved(true);

            ProductPriceData newData = new ProductPriceData(product.getGlobalId(), oldData.getBuyOffset(), oldData.getSellOffset(), oldData.getExpireDate(), oldData.getPurchases(), oldData.getSales());
            this.loadPriceData(newData);
            this.plugin.debug("Replaced old price data for product: %s [%s]".formatted(product.getId(), product.getGlobalId()));
            return newData;
        }
        // LEGACY - END

        return null;
    }

    @Nullable
    public ProductPriceData getPriceData(@NonNull UUID productId) {
        return this.priceDataById.get(productId);
    }



    @NonNull
    public Set<ProductStockData> getStockDatas() {
        return Set.copyOf(this.stockDataById.values());
    }

    @Nullable
    public ProductStockData getStockData(@NonNull Product product) {
        ProductStockData data = this.getStockData(product.getGlobalId());
        if (data != null) return data;

        // LEGACY - START
        LegacyProductKey key = LegacyProductKey.global(product);
        LegacyStockData oldData = this.legacyStockDataMap.get(key);
        if (oldData != null && !oldData.isRemoved()) {
            oldData.setRemoved(true);

            ProductStockData newData = new ProductStockData(product.getGlobalId(), oldData.getBuyStock(), oldData.getRestockDate());
            this.loadStockData(newData);
            this.plugin.debug("Replaced old stock data for product: %s [%s]".formatted(product.getId(), product.getGlobalId()));
            return newData;
        }
        // LEGACY - END

        return null;
    }

    @Nullable
    public ProductStockData getStockData(@NonNull UUID productId) {
        return this.stockDataById.get(productId);
    }



    @NonNull
    public Set<ProductLimitData> getLimitDatas() {
        Set<ProductLimitData> dataSet = new HashSet<>();
        this.limitDataById.values().forEach(innerMap -> dataSet.addAll(innerMap.values()));

        return Set.copyOf(dataSet);
    }

    @NonNull
    public Set<ProductLimitData> getLimitDatas(@NonNull UUID productId) {
        return Set.copyOf(this.limitDataById.getOrDefault(productId, Collections.emptyMap()).values());
    }

    @Nullable
    public ProductLimitData getLimitData(@NonNull Player player, @NonNull Product product) {
        UUID playerId = player.getUniqueId();
        UUID globalId = product.getGlobalId();

        ProductLimitData data = this.limitDataById.getOrDefault(globalId, Collections.emptyMap()).get(playerId);
        if (data != null) return data;

        // LEGACY - START
        LegacyProductKey key = LegacyProductKey.personal(product, playerId);
        LegacyStockData oldData = this.legacyStockDataMap.get(key);
        if (oldData != null && !oldData.isRemoved()) {
            oldData.setRemoved(true);

            int initialBuy = product.getBuyLimit();
            int initialSell = product.getSellLimit();

            int oldBuyLeft = oldData.getBuyStock();
            int oldSellLeft = oldData.getSellStock();

            int purchases = initialBuy - oldBuyLeft;
            int sales = initialSell - oldSellLeft;

            ProductLimitData newData = new ProductLimitData(playerId, globalId, purchases, sales, oldData.getRestockDate());
            this.loadLimitData(newData);
            this.plugin.debug("Replaced old %s player limit data for product: %s [%s]".formatted(playerId, product.getId(), product.getGlobalId()));
            return newData;
        }
        // LEGACY - END

        return null;
    }
}
