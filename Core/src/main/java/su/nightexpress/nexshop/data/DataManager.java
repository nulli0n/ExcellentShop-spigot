package su.nightexpress.nexshop.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.data.Saveable;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.data.key.ProductKey;
import su.nightexpress.nexshop.data.key.RotationKey;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.data.product.StockData;
import su.nightexpress.nexshop.data.shop.RotationData;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Lists;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DataManager extends AbstractManager<ShopPlugin> {

    private final Map<ProductKey, PriceData>     priceDataMap;
    private final Map<ProductKey, StockData>     stockDataMap;
    private final Map<RotationKey, RotationData> rotationDataMap;

    private boolean loaded;

    public DataManager(@NotNull ShopPlugin plugin) {
        super(plugin);
        this.priceDataMap = new ConcurrentHashMap<>();
        this.stockDataMap = new ConcurrentHashMap<>();
        this.rotationDataMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        // Initial load is triggered by ShopPlugin AFTER modules are loaded.
        this.addAsyncTask(this::saveScheduledDatas, Config.DATA_SAVE_INTERVAL.get());
    }

    @Override
    protected void onShutdown() {
        this.saveScheduledDatas();
        this.clear();
    }

    public void clear() {
        this.priceDataMap.clear();
        this.stockDataMap.clear();
        this.rotationDataMap.clear();
        this.loaded = false;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void loadAllData() {
        this.loadPriceDatas();
        this.loadStockDatas();
        this.loadRotationDatas();
        this.loaded = true;
        this.plugin.getShopManager().getShops().forEach(shop -> shop.updatePrices(false)); // Update prices in the same thread to prevent data duplications.
    }


    private void loadPriceDatas() {
        this.plugin.getDataHandler().loadPriceDatas().forEach(this::loadPriceData);
    }

    public void loadPriceData(@NotNull PriceData data) {
        ProductKey key = new ProductKey(data.getShopId(), data.getProductId(), data.getShopId());
        this.priceDataMap.put(key, data);
    }


    private void loadStockDatas() {
        this.plugin.getDataHandler().loadStockDatas().forEach(this::loadStockData);
    }

    public void loadStockData(@NotNull StockData data) {
        ProductKey key = new ProductKey(data.getShopId(), data.getProductId(), data.getHolder());
        this.stockDataMap.put(key, data);
    }


    private void loadRotationDatas() {
        this.plugin.getDataHandler().loadRotationDatas().forEach(this::loadRotationData);
    }

    public void loadRotationData(@NotNull RotationData data) {
        this.rotationDataMap.put(new RotationKey(data.getShopId(), data.getRotationId()), data);
    }

    public void saveScheduledDatas() {
        this.saveScheduledPriceDatas();
        this.saveScheduledStockDatas();
        this.saveScheduledRotationDatas();
    }

    public void saveScheduledPriceDatas() {
        this.saveScheduledDatas(this.getPriceDatas(), DataHandler::updatePriceDatas, "price");
    }

    public void saveScheduledStockDatas() {
        this.saveScheduledDatas(this.getStockDatas(), DataHandler::updateStockDatas, "stock");
    }

    public void saveScheduledRotationDatas() {
        this.saveScheduledDatas(this.getRotationDatas(), DataHandler::updateRotationDatas, "rotation");
    }

    private <T extends Saveable> void saveScheduledDatas(@NotNull Set<T> originDatas, @NotNull BiConsumer<DataHandler, Set<T>> consumer, @NotNull String name) {
        Set<T> filteredDatas = originDatas.stream().filter(Saveable::isSaveRequired).peek(data -> data.setSaveRequired(false)).collect(Collectors.toSet());
        if (filteredDatas.isEmpty()) return;

        consumer.accept(this.plugin.getDataHandler(), filteredDatas);
    }

    public void deleteAllData(@NotNull VirtualShop shop) {
        this.plugin.runTaskAsync(() -> {
            // First remove from the database.
            this.plugin.getDataHandler().deleteRotationData(shop);
            this.plugin.getDataHandler().deletePriceData(shop);
            this.plugin.getDataHandler().deleteStockData(shop);

            // Now clean up memory (so no duplicates can be created during the deletion process).
            this.rotationDataMap.keySet().removeIf(key -> key.isShop(shop));
            this.priceDataMap.keySet().removeIf(key -> key.isShop(shop));
            this.stockDataMap.keySet().removeIf(key -> key.isShop(shop));
        });
    }

    @NotNull
    public Map<RotationKey, RotationData> getRotationDataMap() {
        return this.rotationDataMap;
    }

    @NotNull
    public Set<RotationData> getRotationDatas() {
        return new HashSet<>(this.rotationDataMap.values());
    }

    @Nullable
    public RotationData getRotationData(@NotNull Rotation rotation) {
        return this.rotationDataMap.get(RotationKey.from(rotation));
    }

    @NotNull
    public RotationData getRotationDataOrCreate(@NotNull Rotation rotation) {
        RotationData currentData = this.getRotationData(rotation);
        if (currentData != null) return currentData;

        RotationData data = new RotationData(rotation.getShop().getId(), rotation.getId());
        this.loadRotationData(data);
        this.plugin.runTaskAsync(() -> plugin.getDataHandler().insertRotationData(data));
        return data;
    }

    public void deleteRotationData(@NotNull Rotation rotation) {
        this.plugin.runTaskAsync(() -> {
            this.plugin.getDataHandler().deleteRotationData(rotation); // First remove from the database.
            this.rotationDataMap.remove(RotationKey.from(rotation)); // Now clean up memory (so no duplicates can be created during the deletion process).
        });
    }

    @NotNull
    public Map<ProductKey, PriceData> getPriceDataMap() {
        return this.priceDataMap;
    }

    @NotNull
    public Set<PriceData> getPriceDatas() {
        return new HashSet<>(this.priceDataMap.values());
    }

    @Nullable
    public PriceData getPriceData(@NotNull Product product) {
        return this.priceDataMap.get(ProductKey.global(product));
    }

    @NotNull
    public PriceData getPriceDataOrCreate(@NotNull Product product) {
        // Return fresh empty data for products with pricing that do not need a data.
        if (product.getPricingType() == PriceType.FLAT || product.getPricingType() == PriceType.PLAYER_AMOUNT) {
            return PriceData.create(product);
        }

        PriceData data = this.getPriceData(product);
        if (data != null) return data;

        PriceData fresh = PriceData.create(product);
        this.loadPriceData(fresh);
        this.plugin.runTaskAsync(() -> this.plugin.getDataHandler().insertPriceData(fresh));
        return fresh;
    }

    public void savePriceData(@NotNull Product product) {
        PriceData data = this.getPriceData(product);
        if (data == null) return;

        data.setSaveRequired(true);
    }

    public void deletePriceData(@NotNull Product product) {
        this.plugin.runTaskAsync(() -> {
            this.plugin.getDataHandler().deletePriceData(product); // First remove from the database.
            this.priceDataMap.remove(ProductKey.global(product)); // Now clean up memory (so no duplicates can be created during the deletion process).
        });
    }

    public void resetPriceDatas(@NotNull Shop shop) {
        this.resetPriceDatas(new HashSet<>(shop.getValidProducts()));
    }

    public void resetPriceData(@NotNull Product product) {
        this.resetPriceDatas(Lists.newSet(product));
    }

    public void resetPriceDatas(@NotNull Set<Product> products) {
        products.forEach(product -> {
            PriceData data = this.getPriceData(product);
            if (data == null) return;

            data.reset();
            data.setSaveRequired(true);
        });
    }

    @NotNull
    public Map<ProductKey, StockData> getStockDataMap() {
        return this.stockDataMap;
    }

    @NotNull
    public Set<StockData> getStockDatas() {
        return new HashSet<>(this.stockDataMap.values());
    }

    @Nullable
    public StockData getStockData(@NotNull Product product) {
        return this.stockDataMap.get(ProductKey.global(product));
    }

    @Nullable
    public StockData getStockData(@NotNull VirtualProduct product, @Nullable Player player) {
        return this.getStockData(product, player == null ? null : player.getUniqueId());
    }

    @Nullable
    public StockData getStockData(@NotNull VirtualProduct product, @Nullable UUID playerId) {
        return this.stockDataMap.get(ProductKey.globalOrPerosnal(product, playerId));
    }

    @NotNull
    public StockData getStockDataOrCreate(@NotNull VirtualProduct product, @Nullable Player player) {
        return this.getStockDataOrCreate(product, player == null ? null : player.getUniqueId());
    }

    @NotNull
    public StockData getStockDataOrCreate(@NotNull VirtualProduct product, @Nullable UUID playerId) {
        StockValues values = product.getStocksOrLimits(playerId);

        StockData data = this.getStockData(product, playerId);
        if (data != null) {
            if (data.isRestockTime()) {
                data.restock(values);
                data.setSaveRequired(true);
            }
            return data;
        }

        StockData fresh = StockData.create(product, values, playerId);
        this.loadStockData(fresh);
        this.plugin.runTaskAsync(() -> plugin.getDataHandler().insertStockData(fresh));
        return fresh;
    }

    public void deleteStockData(@NotNull VirtualProduct product) {
        this.plugin.runTaskAsync(() -> {
            this.plugin.getDataHandler().deleteStockData(product);  // First remove from the database.
            this.stockDataMap.remove(ProductKey.global(product)); // Now clean up memory (so no duplicates can be created during the deletion process).
        });
    }

    public void resetStockDatas(@NotNull Product product) {
        this.resetStockDatas(Lists.newSet(product));
    }

    public void resetStockDatas(@NotNull VirtualShop shop) {
        this.resetStockDatas(new HashSet<>(shop.getProducts()));
    }

    public void resetStockDatas(@NotNull Set<Product> products) {
        products.forEach(product -> this.stockDataMap.entrySet().stream().filter(e -> e.getKey().isProduct(product)).map(Map.Entry::getValue).forEach(data -> {
            data.setExpired();
            data.setSaveRequired(true);
        }));
    }
}
