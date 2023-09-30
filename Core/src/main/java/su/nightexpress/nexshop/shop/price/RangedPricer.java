package su.nightexpress.nexshop.shop.price;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.values.UniDouble;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.HashMap;
import java.util.Map;

public abstract class RangedPricer extends ProductPricer {

    protected final Map<TradeType, UniDouble> priceRange;

    public RangedPricer() {
        this.priceRange = new HashMap<>();
    }

    @NotNull
    public UniDouble getPriceRange(@NotNull TradeType tradeType) {
        return this.priceRange.computeIfAbsent(tradeType, k -> UniDouble.of(-1, -1));
    }

    public double getPriceMin(@NotNull TradeType tradeType) {
        return this.getPriceRange(tradeType).getMinValue();
    }

    public double getPriceMax(@NotNull TradeType tradeType) {
        return this.getPriceRange(tradeType).getMaxValue();
    }

    public void setPrice(@NotNull TradeType tradeType, @NotNull UniDouble price) {
        this.priceRange.put(tradeType, price);
    }
}
