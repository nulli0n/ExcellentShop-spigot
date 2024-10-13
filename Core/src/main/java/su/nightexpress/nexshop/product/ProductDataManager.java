package su.nightexpress.nexshop.product;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.product.data.AbstractData;
import su.nightexpress.nexshop.product.data.ProductData;
import su.nightexpress.nightcore.manager.AbstractManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProductDataManager extends AbstractManager<ShopPlugin> {

    private final Set<AbstractData>                     scheduledSave;
    private final Map<String, Map<String, ProductData>> dataMap; // shopId -> productId -> ProductData

    private boolean loaded;

    public ProductDataManager(@NotNull ShopPlugin plugin) {
        super(plugin);
        this.scheduledSave = ConcurrentHashMap.newKeySet();
        this.dataMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.loadData();
        this.loaded = true;

        this.addTask(this.plugin.createAsyncTask(this::saveScheduled).setSecondsInterval(Config.DATA_PRODUCT_SAVE_INTERVAL.get()));
    }

    @Override
    protected void onShutdown() {
        this.saveScheduled();
        this.scheduledSave.clear();
        this.dataMap.clear();
        this.loaded = false;
    }

    public void loadData() {
        //this.dataMap.clear();
        this.loadPriceData();
        this.loadStockData();
    }

    private void loadPriceData() {
        this.plugin.getData().getVirtualDataHandler().getPriceDatas().forEach(priceData -> {
            if (this.isScheduledToSave(priceData)) return;

            this.getData(priceData.getShopId(), priceData.getProductId()).loadPrice(priceData);
        });
        //this.plugin.info("Loaded product price datas.");
    }

    private void loadStockData() {
        this.plugin.getData().getVirtualDataHandler().getStockDatas().forEach(stockData -> {
            if (this.isScheduledToSave(stockData)) return;

            this.getData(stockData.getShopId(), stockData.getProductId()).loadStock(stockData);
        });
        //this.plugin.info("Loaded product stock datas.");
    }

    private boolean isScheduledToSave(@NotNull AbstractData data) {
        return this.scheduledSave.stream().anyMatch(other -> other.getProductId().equalsIgnoreCase(data.getProductId()) && other.getShopId().equalsIgnoreCase(data.getShopId()));
    }

    public void saveScheduled() {
        if (this.scheduledSave.isEmpty()) return;

        this.plugin.getData().getVirtualDataHandler().saveProductDatas(this.scheduledSave);
        this.scheduledSave.clear();
    }

    public void cleanUp() {
        this.plugin.info("Initialized product data clean up...");

        Map<String, Shop> shopMap = new HashMap<>();
        this.plugin.getShopManager().getShops().forEach(shop -> shopMap.put(shop.getId(), shop));
        boolean affectDatabase = Config.DATA_PRODUCT_CLEAN_UP.get();

        // Remove from cache (and database) product data objects of invalid shops or shop products.
        this.dataMap.entrySet().removeIf(entry -> {
            String shopId = entry.getKey();
            Shop shop = shopMap.get(shopId);
            if (shop == null) {
                if (affectDatabase) {
                    this.plugin.info("[1] Deleted product data for invalid shop '" + shopId + "'.");
                    plugin.getData().getVirtualDataHandler().deletePriceData(shopId);
                    plugin.getData().getVirtualDataHandler().deleteStockData(shopId);
                }
                return true;
            }

            entry.getValue().keySet().removeIf(productId -> {
                Product product = shop.getProductById(productId);
                if (product == null) {
                    if (affectDatabase) {
                        this.plugin.info("[2] Deleted product data for invalid shop product '" + shopId + " -> " + productId + "'.");
                        plugin.getData().getVirtualDataHandler().deletePriceData(shopId, productId);
                        plugin.getData().getVirtualDataHandler().deleteStockData(shopId, productId);
                    }
                    return true;
                }

                return false;
            });

            return false;
        });
    }

    public void scheduleSave(@NotNull AbstractData data) {
        this.scheduledSave.add(data);
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    @NotNull
    public Map<String, ProductData> getDataMap(@NotNull Shop shop) {
        return this.getDataMap(shop.getId());
    }

    @NotNull
    public Map<String, ProductData> getDataMap(@NotNull String shopId) {
        return this.dataMap.computeIfAbsent(shopId.toLowerCase(), k -> new HashMap<>());
    }

    @NotNull
    public Set<ProductData> getDatas(@NotNull Shop shop) {
        return new HashSet<>(this.getDataMap(shop).values());
    }

    @NotNull
    public ProductData getData(@NotNull Product product) {
        return this.getData(product.getShop().getId(), product.getId());
    }

    @NotNull
    public ProductData getData(@NotNull String shopId, @NotNull String productId) {
        return this.getDataMap(shopId).computeIfAbsent(productId, k -> new ProductData());
    }
}
