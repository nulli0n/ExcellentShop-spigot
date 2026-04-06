package su.nightexpress.excellentshop.product.price;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.excellentshop.product.ProductPricing;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.HashMap;
import java.util.Map;

public class FlatPricing extends ProductPricing {

    private final Map<TradeType, Double> prices;

    public FlatPricing() {
        super(PriceType.FLAT);
        this.prices = new HashMap<>();
    }

    @NonNull
    public static FlatPricing of(double buyPrice, double sellPrice) {
        FlatPricing pricing = new FlatPricing();
        pricing.setPrice(TradeType.BUY, buyPrice);
        pricing.setPrice(TradeType.SELL, sellPrice);
        return pricing;
    }

    @NonNull
    public static FlatPricing read(@NonNull FileConfig config, @NonNull String path) {
        FlatPricing pricing = new FlatPricing();
        for (TradeType tradeType : TradeType.values()) {
            pricing.setPrice(tradeType, config.getDouble(path + "." + tradeType.name()));
        }
        return pricing;
    }

    @Override
    protected void writeAdditional(@NonNull FileConfig config, @NonNull String path) {
        this.prices.forEach((tradeType, amount) -> config.set(path + "." + tradeType.name(), amount));
    }

    @Override
    public boolean shouldResetOnExpire() {
        return false;
    }

    @Override
    public void handleTransaction(@NonNull ECompletedTransaction transaction, @NonNull Product product, int units, @NonNull PriceData priceData) {

    }

    @Override
    public void updatePrice(@NonNull Product product, @NonNull PriceData priceData) {
        for (TradeType tradeType : TradeType.values()) {
            product.setPrice(tradeType, this.getPrice(tradeType));
        }
    }

    @Override
    public double getAveragePrice(@NonNull TradeType type) {
        return this.getPrice(type);
    }

    public double getPrice(@NonNull TradeType type) {
        return this.prices.getOrDefault(type, DISABLED);
    }

    public void setPrice(@NonNull TradeType type, double price) {
        this.prices.put(type, price);
    }
}
