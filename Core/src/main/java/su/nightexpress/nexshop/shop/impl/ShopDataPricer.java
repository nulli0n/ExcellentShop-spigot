package su.nightexpress.nexshop.shop.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.ShopPricer;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.object.PriceData;
import su.nightexpress.nexshop.shop.impl.price.DynamicPricer;
import su.nightexpress.nexshop.shop.impl.price.FloatPricer;
import su.nightexpress.nexshop.api.shop.Transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShopDataPricer implements ShopPricer {

    private final ExcellentShop plugin;
    private final Shop                   shop;
    private final Map<String, PriceData> dataMap;

    public ShopDataPricer(@NotNull ExcellentShop plugin, @NotNull Shop shop) {
        this.plugin = plugin;
        this.shop = shop;
        this.dataMap = new ConcurrentHashMap<>();
    }

    @Override
    public void load() {
        this.getDataMap().clear();
        this.plugin.getData().getVirtualDataHandler().getPriceData(this.shop).forEach(this::addData);
        this.updatePrices();

        //this.plugin.info("Loaded " + this.getDataMap().size() + " price datas for '" + shop.getId() + "' shop!");
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        Transaction result = event.getTransaction();
        Product product = result.getProduct();
        AbstractProductPricer pricer = product.getPricer();

        if (pricer.getType() == PriceType.DYNAMIC) {
            this.plugin.runTaskAsync(task -> {
                PriceData priceData = this.getDataOrCreate(product);
                priceData.countTransaction(result.getTradeType(), result.getUnits());
                this.updateDynamic(product);
            });
        }
    }

    @Override
    public void refreshPrices() {
        this.shop.getProducts().forEach(product -> {
            if (product.getPricer().getType() == PriceType.FLOAT) {
                if (!(product.getPricer() instanceof FloatPricer pricer)) return;
                if (!pricer.isUpdateTime()) return;

                this.flushFloatPrices(product);
            }
        });
    }

    @Override
    public void updatePrices() {
        this.shop.getProducts().forEach(product -> {
            if (product.getPricer().getType() == PriceType.FLAT) return;

            this.updatePrice(product);
        });
    }

    @Override
    public void updatePrice(@NotNull Product product) {
        AbstractProductPricer pricer = product.getPricer();
        if (pricer.getType() == PriceType.DYNAMIC) {
            this.updateDynamic(product);
        }
        else if (pricer.getType() == PriceType.FLOAT) {
            this.updateFloat(product);
        }
    }

    private void updateDynamic(@NotNull Product product) {
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
        this.saveData(priceData);
    }

    private void updateFloat(@NotNull Product product) {
        if (!(product.getPricer() instanceof FloatPricer pricer)) return;

        PriceData priceData = this.getData(product);
        boolean hasData = priceData != null;
        if (hasData) {
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
        if (sellPrice > buyPrice && buyPrice >= 0) {
            sellPrice = buyPrice;
        }

        PriceData priceData = this.getDataOrCreate(product);
        priceData.setLastBuyPrice(buyPrice);
        priceData.setLastSellPrice(sellPrice);
        priceData.setLastUpdated(System.currentTimeMillis());
        this.saveData(priceData);
        this.updateFloat(product);
    }

    @NotNull
    public Map<String, PriceData> getDataMap() {
        return dataMap;
    }

    private void addData(@NotNull PriceData data) {
        this.getDataMap().put(data.getProductId(), data);
    }

    @NotNull
    private PriceData getDataOrCreate(@NotNull Product product) {
        PriceData data = this.getData(product);
        if (data == null) {
            data = new PriceData(product);
            //this.createData(data);
            this.addData(data);
            this.plugin.getData().getVirtualDataHandler().insertPriceData(data);
        }
        return data;
    }

    @Override
    @Nullable
    public PriceData getData(@NotNull Product product) {
        return this.getData(product.getId());
    }

    @Nullable
    private PriceData getData(@NotNull String productId) {
        return this.getDataMap().get(productId);
    }

    @Override
    public void saveData(@NotNull Product product) {
        PriceData data = this.getData(product);
        if (data == null) return;

        this.saveData(data);
    }

    private void saveData(@NotNull PriceData data) {
        this.plugin.getData().getVirtualDataHandler().savePriceData(data);
    }

    @Override
    public void deleteData(@NotNull Product product) {
        this.getDataMap().remove(product.getId());
        //PriceData data = this.getDataMap().remove(product.getId());
        //if (data == null) return;

        this.plugin.runTaskAsync(task -> {
            this.plugin.getData().getVirtualDataHandler().deletePriceData(product);
        });
    }

    public void deleteData() {
        this.getDataMap().clear();
        this.plugin.getData().getVirtualDataHandler().deletePriceData(shop);
    }
}
