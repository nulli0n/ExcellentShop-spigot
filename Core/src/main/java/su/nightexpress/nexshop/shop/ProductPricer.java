package su.nightexpress.nexshop.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.nexshop.api.AbstractTimed;
import su.nightexpress.nexshop.api.shop.IProduct;
import su.nightexpress.nexshop.api.shop.IProductPricer;
import su.nightexpress.nexshop.api.shop.IShop;
import su.nightexpress.nexshop.api.shop.IShopDiscount;
import su.nightexpress.nexshop.api.type.TradeType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProductPricer extends AbstractTimed implements IProductPricer {

    private IProduct product;

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
    public void updatePrice() {
        if (this.randomizePrices(false)) {
            return;
        }

        double buyMin = this.getPriceMin(TradeType.BUY);
        double sellMin = this.getPriceMin(TradeType.SELL);
        this.setPrice(TradeType.BUY, buyMin);
        this.setPrice(TradeType.SELL, sellMin);
    }

    @Override
    @NotNull
    public IProduct getProduct() {
        return this.product;
    }

    @Override
    public void setProduct(@NotNull IProduct product) {
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
        double price = this.priceReal.computeIfAbsent(buyType, b -> -1D);

        if (buyType == TradeType.BUY && price > 0 && this.getProduct().isDiscountAllowed()) {
            IShop shop = this.getProduct().getShop();
            IShopDiscount discount = shop.getDiscount();
            if (discount != null) price *= discount.getDiscount();
        }
        return price;
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

    @Override
    public boolean randomizePrices(boolean force) {
        if (!this.isRandomizerEnabled()) return false;

        LocalTime[] times = this.getCurrentTimes();
        if (!force) {
            if (times == null) return false;

            LocalTime lastTime = this.getLastRandomizedTime();
            if (times[1].equals(lastTime)) return false;
        }

        double buyPrice = Rnd.getDouble(this.getPriceMin(TradeType.BUY), this.getPriceMax(TradeType.BUY));
        double sellPrice = Rnd.getDouble(this.getPriceMin(TradeType.SELL), this.getPriceMax(TradeType.SELL));
        if (sellPrice > buyPrice && buyPrice >= 0) {
            sellPrice = buyPrice;
        }

        this.setPrice(TradeType.BUY, buyPrice);
        this.setPrice(TradeType.SELL, sellPrice);

        if (times == null) return true;

        this.setLastRandomizedTime(times[1]);
        return true;
    }
}
