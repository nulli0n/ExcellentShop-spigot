package su.nightexpress.nexshop.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.HashMap;
import java.util.Map;

public abstract class RangedProductPricer extends ProductPricer {

    protected final Map<TradeType, double[]> priceMinMax;

    public RangedProductPricer() {
        this.priceMinMax = new HashMap<>();
    }

    public double getPriceMin(@NotNull TradeType tradeType) {
        return this.priceMinMax.computeIfAbsent(tradeType, b -> new double[]{-1, -1})[0];
    }

    public double getPriceMax(@NotNull TradeType tradeType) {
        return this.priceMinMax.computeIfAbsent(tradeType, b -> new double[]{-1, -1})[1];
    }

    public void setPriceMin(@NotNull TradeType tradeType, double price) {
        this.priceMinMax.computeIfAbsent(tradeType, b -> new double[]{-1, -1})[0] = price;
    }

    public void setPriceMax(@NotNull TradeType tradeType, double price) {
        this.priceMinMax.computeIfAbsent(tradeType, b -> new double[]{-1, -1})[1] = price;
    }
}
