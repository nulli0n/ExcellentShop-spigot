package su.nightexpress.nexshop.product.price.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.util.HashMap;
import java.util.Map;

public abstract class RangedPricer extends AbstractProductPricer {

    protected final Map<TradeType, UniDouble> priceRange;

    public RangedPricer(@NotNull PriceType type) {
        super(type);
        this.priceRange = new HashMap<>();
        this.placeholderMap.add(Placeholders.forRangedPricer(this));
    }

    @NotNull
    public UniDouble getPriceRange(@NotNull TradeType tradeType) {
        return this.priceRange.computeIfAbsent(tradeType, k -> UniDouble.of(-1, -1));
    }

    public double getPriceAverage(@NotNull TradeType tradeType) {
        UniDouble range = this.getPriceRange(tradeType);
        double min = range.getMinValue();
        double max = range.getMaxValue();
        return (min + max) / 2D;
    }

    public double getAverageDifferencePercent(@NotNull TradeType tradeType) {
        double current = this.getPrice(tradeType);
        double avg = this.getPriceAverage(tradeType);
        double point = current / avg;
        double raw = current < avg ? -(1D - point) : (point - 1D);

        return raw * 100D;
    }

    public double getPriceMin(@NotNull TradeType tradeType) {
        return this.getPriceRange(tradeType).getMinValue();
    }

    public double getPriceMax(@NotNull TradeType tradeType) {
        return this.getPriceRange(tradeType).getMaxValue();
    }

    public void setPriceRange(@NotNull TradeType tradeType, @NotNull UniDouble price) {
        this.priceRange.put(tradeType, price);
    }
}
