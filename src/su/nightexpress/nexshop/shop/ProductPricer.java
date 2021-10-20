package su.nightexpress.nexshop.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.AbstractTimed;
import su.nightexpress.nexshop.api.IProductPricer;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.type.TradeType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProductPricer extends AbstractTimed implements IProductPricer {

    private IShopProduct product;

    private final Map<TradeType, double[]> priceMinMax;
    private final Map<TradeType, Double>   priceReal;

    private boolean   isRndEnabled;
    private LocalTime rndLastTime;

    public ProductPricer() {
        this(
                new HashMap<>(),

                false,
                new HashSet<>(),
                new HashSet<>()
        );
        this.setPriceMin(TradeType.BUY, 50D);
        this.setPriceMax(TradeType.BUY, 50D);
        this.setPriceMin(TradeType.SELL, 20D);
        this.setPriceMax(TradeType.SELL, 20D);

        this.updatePrice();
    }

    public ProductPricer(
            @NotNull Map<TradeType, double[]> priceMinMax,

            boolean isRndEnabled,
            @NotNull Set<DayOfWeek> days,
            @NotNull Set<LocalTime[]> times) {
        super(days, times);
        this.priceMinMax = priceMinMax;
        this.priceReal = new HashMap<>();

        this.isRndEnabled = isRndEnabled;

        this.updatePrice();
    }

    @Override
    @NotNull
    public IShopProduct getProduct() {
        return this.product;
    }

    @Override
    public void setProduct(@NotNull IShopProduct product) {
        this.product = product;
    }

    @Override
    public boolean isRandomizerEnabled() {
        return this.isRndEnabled;
    }

    @Override
    public void setRandomizerEnabled(boolean isEnabled) {
        this.isRndEnabled = isEnabled;
    }

    @Override
    public double getPriceMin(@NotNull TradeType buyType) {
        return this.priceMinMax.computeIfAbsent(buyType, b -> new double[]{-1, 1})[0];
    }

    @Override
    public double getPriceMax(@NotNull TradeType buyType) {
        return this.priceMinMax.computeIfAbsent(buyType, b -> new double[]{-1, 1})[1];
    }

    @Override
    public void setPriceMin(@NotNull TradeType buyType, double price) {
        this.priceMinMax.computeIfAbsent(buyType, b -> new double[]{-1, 1})[0] = price;
        this.updatePrice();
    }

    @Override
    public void setPriceMax(@NotNull TradeType buyType, double price) {
        this.priceMinMax.computeIfAbsent(buyType, b -> new double[]{-1, 1})[1] = price;
        this.updatePrice();
    }

    @Override
    public double getPrice(@NotNull TradeType buyType) {
        return this.priceReal.computeIfAbsent(buyType, b -> -1D);
    }

    @Override
    public void setPrice(@NotNull TradeType buyType, double price) {
        this.priceReal.put(buyType, price);
    }

    @Override
    @Nullable
    public LocalTime getLastRandomizedTime() {
        return this.rndLastTime;
    }

    @Override
    public void setLastRandomizedTime(@NotNull LocalTime time) {
        this.rndLastTime = time;
    }
}
