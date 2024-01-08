package su.nightexpress.nexshop.shop.impl.price;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.values.UniDouble;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.util.ShopUtils;

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
        super(PriceType.FLOAT);
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
                .stream().map(ShopUtils.TIME_FORMATTER::format).toList()));
    }

    @NotNull
    public static FloatPricer read(@NotNull JYML cfg, @NotNull String path) {
        FloatPricer pricer = new FloatPricer();
        Map<TradeType, double[]> priceMap = new HashMap<>();
        for (TradeType tradeType : TradeType.values()) {
            UniDouble price = UniDouble.read(cfg, path + "." + tradeType.name());
            pricer.setPrice(tradeType, price);
        }
        pricer.setDays(ShopUtils.parseDays(cfg.getString(path + ".Refresh.Days", "")));
        pricer.setTimes(ShopUtils.parseTimes(cfg.getStringList(path + ".Refresh.Times")));

        return pricer;
    }

    @Override
    protected void writeAdditional(@NotNull JYML cfg, @NotNull String path) {
        this.priceRange.forEach(((tradeType, price) -> {
            price.write(cfg, path + "." + tradeType.name());
        }));
        cfg.set(path + ".Refresh.Days", this.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
        cfg.set(path + ".Refresh.Times", this.getTimes().stream().map(ShopUtils.TIME_FORMATTER::format).toList());
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
}
