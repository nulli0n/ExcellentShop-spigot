package su.nightexpress.nexshop.shop.price;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.values.UniDouble;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.util.TimeUtils;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.price.ProductPriceData;
import su.nightexpress.nexshop.data.price.ProductPriceStorage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FloatPricer extends RangedPricer {

    private Set<DayOfWeek>   days;
    private Set<LocalTime> times;

    public FloatPricer() {
        this.days = new HashSet<>();
        this.times = new HashSet<>();

        this.placeholderMap
            .add(Placeholders.PRODUCT_PRICER_BUY_MIN, () -> String.valueOf(this.getPriceMin(TradeType.BUY)))
            .add(Placeholders.PRODUCT_PRICER_BUY_MAX, () -> String.valueOf(this.getPriceMax(TradeType.BUY)))
            .add(Placeholders.PRODUCT_PRICER_SELL_MIN, () -> String.valueOf(this.getPriceMin(TradeType.SELL)))
            .add(Placeholders.PRODUCT_PRICER_SELL_MAX, () -> String.valueOf(this.getPriceMax(TradeType.SELL)))
            .add(Placeholders.PRODUCT_PRICER_FLOAT_REFRESH_DAYS, () -> String.join(", ", this.getDays()
                .stream().map(DayOfWeek::name).toList()))
            .add(Placeholders.PRODUCT_PRICER_FLOAT_REFRESH_TIMES, () -> String.join(", ", this.getTimes()
                .stream().map(TimeUtils.TIME_FORMATTER::format).toList()))
        ;
    }

    @NotNull
    public static FloatPricer read(@NotNull JYML cfg, @NotNull String path) {
        FloatPricer pricer = new FloatPricer();
        Map<TradeType, double[]> priceMap = new HashMap<>();
        for (TradeType tradeType : TradeType.values()) {
            UniDouble price = UniDouble.read(cfg, path + "." + tradeType.name());
            pricer.setPrice(tradeType, price);
            /*double min = cfg.getDouble(path + "." + tradeType.name() + ".Min", -1D);
            double max = cfg.getDouble(path + "." + tradeType.name() + ".Max", -1D);
            pricer.setPriceMin(tradeType, min);
            pricer.setPriceMax(tradeType, max);*/
        }
        pricer.setDays(TimeUtils.parseDays(cfg.getString(path + ".Refresh.Days", "")));
        pricer.setTimes(TimeUtils.parseTimes(cfg.getStringList(path + ".Refresh.Times")));

        return pricer;
    }

    @Override
    public void write(@NotNull JYML cfg, @NotNull String path) {
        this.priceRange.forEach(((tradeType, price) -> {
            price.write(cfg, path + "." + tradeType.name());
            //cfg.set(path + "." + tradeType.name() + ".Min", price[0]);
            //cfg.set(path + "." + tradeType.name() + ".Max", price[1]);
        }));
        cfg.set(path + ".Refresh.Days", this.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
        cfg.set(path + ".Refresh.Times", this.getTimes().stream().map(TimeUtils.TIME_FORMATTER::format).toList());
    }

    @Override
    public void update() {
        ProductPriceData priceData = this.getData();
        boolean hasData = priceData != null;
        if (hasData) {
            this.setPrice(TradeType.BUY, priceData.getLastBuyPrice());
            this.setPrice(TradeType.SELL, priceData.getLastSellPrice());
        }
        else {
            this.randomize();
        }
    }

    public boolean isUpdateTime() {
        if (this.getDays().isEmpty()) return false;
        if (this.getTimes().isEmpty()) return false;
        if (!this.getDays().contains(LocalDate.now().getDayOfWeek())) return false;

        LocalTime roundNow = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        return this.getTimes().stream().anyMatch(time -> {
            return time.truncatedTo(ChronoUnit.MINUTES).equals(roundNow);
        });
    }

    @Override
    @NotNull
    public PriceType getType() {
        return PriceType.FLOAT;
    }

    @NotNull
    public Set<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(@NotNull Set<DayOfWeek> days) {
        this.days = days;
    }

    @NotNull
    public Set<LocalTime> getTimes() {
        return times;
    }

    public void setTimes(@NotNull Set<LocalTime> times) {
        this.times = times;
    }

    @Nullable
    public ProductPriceData getData() {
        return ProductPriceStorage.getData(this.getProduct().getShop().getId(), this.getProduct().getId());
    }

    public void randomize() {
        double buyPrice = this.getPriceRange(TradeType.BUY).roll();//Rnd.getDouble(this.getPriceMin(TradeType.BUY), this.getPriceMax(TradeType.BUY));
        double sellPrice = this.getPriceRange(TradeType.SELL).roll();//Rnd.getDouble(this.getPriceMin(TradeType.SELL), this.getPriceMax(TradeType.SELL));
        if (sellPrice > buyPrice && buyPrice >= 0) {
            sellPrice = buyPrice;
        }

        //this.setPrice(TradeType.BUY, buyPrice);
        //this.setPrice(TradeType.SELL, sellPrice);

        ProductPriceData priceData = this.getData();
        boolean hasData = priceData != null;
        if (!hasData) {
            priceData = new ProductPriceData(this);
        }
        priceData.setLastBuyPrice(buyPrice);
        priceData.setLastSellPrice(sellPrice);
        priceData.setLastUpdated(System.currentTimeMillis());
        if (!hasData) {
            ProductPriceStorage.createData(priceData);
        }
        else {
            ProductPriceStorage.saveData(priceData);
        }

        this.update();
    }
}
