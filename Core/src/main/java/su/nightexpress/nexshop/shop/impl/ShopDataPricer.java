package su.nightexpress.nexshop.shop.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.ShopPricer;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.data.ProductData;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.product.data.impl.PriceData;
import su.nightexpress.nexshop.product.price.impl.DynamicPricer;
import su.nightexpress.nexshop.product.price.impl.FloatPricer;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.Map;

public class ShopDataPricer implements ShopPricer {

    private final ShopPlugin plugin;
    private final Shop       shop;

    public ShopDataPricer(@NotNull ShopPlugin plugin, @NotNull Shop shop) {
        this.plugin = plugin;
        this.shop = shop;
    }

    @NotNull
    public Map<String, ProductData> getDataMap() {
        return this.plugin.getShopManager().getProductDataManager().getDataMap(this.shop);
    }

    @NotNull
    public ProductData getProductData(@NotNull Product product) {
        return this.getProductData(product.getId());
    }

    @NotNull
    public ProductData getProductData(@NotNull String productId) {
        return this.plugin.getShopManager().getProductDataManager().getData(this.shop.getId(), productId);
    }

    @Nullable
    public PriceData getPriceData(@NotNull Product product) {
        return this.getProductData(product).getPriceData();
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        Transaction result = event.getTransaction();
        Product product = result.getProduct();
        AbstractProductPricer pricer = product.getPricer();

        if (pricer.getType() == PriceType.DYNAMIC) {
            PriceData priceData = this.getDataOrCreate(product);
            priceData.countTransaction(result.getTradeType(), result.getUnits());
            this.updateDynamic(product, true);
        }
    }

    @Override
    public void updatePrices() {
        this.shop.getValidProducts().forEach(product -> {
            if (product.getPricer().getType() == PriceType.FLAT) return;

            this.updatePrice(product);
        });
    }

    @Override
    public void updatePrice(@NotNull Product product) {
        AbstractProductPricer pricer = product.getPricer();
        if (pricer.getType() == PriceType.DYNAMIC) {
            this.updateDynamic(product, false);
        }
        else if (pricer.getType() == PriceType.FLOAT) {
            this.updateFloat(product);
        }
    }

    private void updateDynamic(@NotNull Product product, boolean save) {
        if (!(product.getPricer() instanceof DynamicPricer pricer)) return;

        PriceData priceData = this.getDataOrCreate(product);
        double difference = priceData.getPurchases() - priceData.getSales();
        for (TradeType tradeType : TradeType.values()) {
            double min = pricer.getPriceMin(tradeType);
            double max = pricer.getPriceMax(tradeType);
            if (min < 0 && max < 0) {
                product.setPrice(tradeType, -1);
            }
            else {
                double price = pricer.getInitial(tradeType) + (difference * pricer.getStep(tradeType));
                if (price > max && max >= 0) price = max;
                else if (price < min) price = min;

                product.setPrice(tradeType, price);
            }
        }

        priceData.setLastBuyPrice(pricer.getPrice(TradeType.BUY));
        priceData.setLastSellPrice(pricer.getPrice(TradeType.SELL));
        priceData.setLastUpdated(System.currentTimeMillis());
        priceData.setExpireDate(-1);
        if (save) this.saveData(priceData);
    }

    private void updateFloat(@NotNull Product product) {
        if (!(product.getPricer() instanceof FloatPricer)) return;

        PriceData priceData = this.getPriceData(product);
        boolean hasData = priceData != null;
        if (hasData && !priceData.isExpired()) {
            product.setPrice(TradeType.BUY, priceData.getLastBuyPrice());
            product.setPrice(TradeType.SELL, priceData.getLastSellPrice());
        }
        else {
            this.flushFloatPrices(product);
        }
    }

    private void flushFloatPrices(@NotNull Product product) {
        if (!(product.getPricer() instanceof FloatPricer pricer)) return;

        double buyPrice = pricer.getPriceRange(TradeType.BUY).roll();
        double sellPrice = pricer.getPriceRange(TradeType.SELL).roll();
        if (pricer.isRoundDecimals()) {
            buyPrice = Math.floor(buyPrice);
            sellPrice = Math.floor(sellPrice);
        }
        else {
            buyPrice = NumberUtil.round(buyPrice);
            sellPrice = NumberUtil.round(sellPrice);
        }
        if (sellPrice > buyPrice && buyPrice >= 0) {
            sellPrice = buyPrice;
        }

        PriceData priceData = this.getDataOrCreate(product);
        priceData.setLastBuyPrice(buyPrice);
        priceData.setLastSellPrice(sellPrice);
        priceData.setLastUpdated(System.currentTimeMillis());
        priceData.setExpireDate(pricer.getClosestTimestamp());
        product.setPrice(TradeType.BUY, priceData.getLastBuyPrice());
        product.setPrice(TradeType.SELL, priceData.getLastSellPrice());
        this.saveData(priceData);
    }

    @NotNull
    private PriceData getDataOrCreate(@NotNull Product product) {
        ProductData productData = this.getProductData(product);
        PriceData data = productData.getPriceData();
        if (data == null) {
            data = new PriceData(product);
            productData.loadPrice(data);
            this.insertData(data);
        }
        return data;
    }

    private void insertData(@NotNull PriceData data) {
        this.plugin.getData().getVirtualDataHandler().insertPriceData(data);
    }

    @Override
    public void saveData(@NotNull Product product) {
        PriceData data = this.getPriceData(product);
        if (data == null) return;

        this.saveData(data);
    }

    private void saveData(@NotNull PriceData data) {
        data.setSaveRequired(true);
    }

    @Override
    public void deleteData(@NotNull Product product) {
        this.getDataMap().remove(product.getId());

        this.plugin.runTaskAsync(task -> {
            this.plugin.getData().getVirtualDataHandler().deletePriceData(product);
        });
    }

    @Override
    public void deleteData() {
        this.getDataMap().clear();
        this.plugin.getData().getVirtualDataHandler().deletePriceData(shop);
    }
}
