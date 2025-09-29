package su.nightexpress.nexshop.product.price.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.HashMap;
import java.util.Map;

public class FlatPricing extends ProductPricing {

    private final Map<TradeType, Double> prices;

    public FlatPricing() {
        super(PriceType.FLAT);
        this.prices = new HashMap<>();
    }

    @NotNull
    public static FlatPricing of(double buyPrice, double sellPrice) {
        FlatPricing pricing = new FlatPricing();
        pricing.setPrice(TradeType.BUY, buyPrice);
        pricing.setPrice(TradeType.SELL, sellPrice);
        return pricing;
    }

    @NotNull
    public static FlatPricing read(@NotNull FileConfig config, @NotNull String path) {
        FlatPricing pricing = new FlatPricing();
        for (TradeType tradeType : TradeType.values()) {
            pricing.setPrice(tradeType, config.getDouble(path + "." + tradeType.name()));
        }
        return pricing;
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        this.prices.forEach((tradeType, amount) -> config.set(path + "." + tradeType.name(), amount));
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event, @NotNull Product product, @NotNull PriceData priceData) {

    }

    @Override
    public void updatePrice(@NotNull Product product, @NotNull PriceData priceData) {
        for (TradeType tradeType : TradeType.values()) {
            product.setPrice(tradeType, this.getPrice(tradeType));
        }
    }

    @Override
    public double getAveragePrice(@NotNull TradeType type) {
        return this.getPrice(type);
    }

    public double getPrice(@NotNull TradeType type) {
        return this.prices.getOrDefault(type, DISABLED);
    }

    public void setPrice(@NotNull TradeType type, double price) {
        this.prices.put(type, price);
    }
}
