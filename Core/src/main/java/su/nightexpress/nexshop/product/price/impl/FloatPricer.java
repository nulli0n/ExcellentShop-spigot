package su.nightexpress.nexshop.product.price.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.api.type.RefreshType;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class FloatPricer extends RangedPricer {

    private RefreshType    refreshType;
    private long           refreshInterval;
    private Set<DayOfWeek> days;
    private Set<LocalTime> times;
    private boolean        roundDecimals;

    public FloatPricer() {
        super(PriceType.FLOAT);
        this.setRefreshType(RefreshType.INTERVAL);
        this.days = Lists.newSet(DayOfWeek.values());
        this.times = new HashSet<>();
    }

    @NotNull
    public static FloatPricer read(@NotNull FileConfig config, @NotNull String path) {
        FloatPricer pricer = new FloatPricer();

        for (TradeType tradeType : TradeType.values()) {
            UniDouble price = UniDouble.read(config, path + "." + tradeType.name());
            pricer.setPriceRange(tradeType, price);
        }

        pricer.setRefreshType(config.getEnum(path + ".Refresh.Type", RefreshType.class, RefreshType.FIXED));
        pricer.setRefreshInterval(config.getLong(path + ".Refresh.Interval", 0L));
        pricer.setDays(ShopUtils.parseDays(config.getString(path + ".Refresh.Days", "")));
        pricer.setTimes(ShopUtils.parseTimes(config.getStringList(path + ".Refresh.Times")));
        pricer.setRoundDecimals(config.getBoolean(path + ".Round_Decimals"));

        return pricer;
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        this.priceRange.forEach((tradeType, price) -> {
            price.write(config, path + "." + tradeType.name());
        });
        config.set(path + ".Refresh.Type", this.refreshType.name());
        config.set(path + ".Refresh.Interval", this.refreshInterval);
        config.set(path + ".Refresh.Days", this.days.stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
        config.set(path + ".Refresh.Times", this.times.stream().map(ShopUtils.TIME_FORMATTER::format).toList());
        config.set(path + ".Round_Decimals", this.roundDecimals);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.FLOAT_PRICER.replacer(this);
    }

    @Override
    public double rollPrice(@NotNull TradeType type) {
        double rolled = super.rollPrice(type);

        return this.roundDecimals ? Math.floor(rolled) : NumberUtil.round(rolled);
    }

    /*@Deprecated
    public boolean isUpdateTime() {
        if (this.getDays().isEmpty()) return false;
        if (this.getTimes().isEmpty()) return false;
        if (!this.getDays().contains(LocalDate.now().getDayOfWeek())) return false;

        LocalTime roundNow = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        return this.getTimes().stream().anyMatch(time -> {
            return time.truncatedTo(ChronoUnit.MINUTES).equals(roundNow);
        });
    }*/

    @Nullable
    public LocalDateTime getClosest() {
        if (this.days.isEmpty() || this.times.isEmpty()) return null;

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        int dayCounter = 0;
        while (dayCounter < 8) {
            LocalDateTime adjusted = LocalDateTime.now().plusDays(dayCounter);
            DayOfWeek day = adjusted.getDayOfWeek();

            if (this.days.contains(day)) {
                LocalDateTime time = this.times.stream()
                    .map(stored -> LocalDateTime.of(adjusted.toLocalDate(), stored))
                    .filter(now::isBefore).min(LocalDateTime::compareTo).orElse(null);

                if (time != null) return time;
            }

            dayCounter++;
        }

        return null;
    }

    public long getClosestTimestamp() {
        if (this.refreshType == RefreshType.INTERVAL) {
            return TimeUtil.createTimestamp(this.refreshInterval);
        }

        LocalDateTime dateTime = this.getClosest();
        return dateTime == null ? 0L : TimeUtil.toEpochMillis(dateTime);
    }

    @NotNull
    public RefreshType getRefreshType() {
        return this.refreshType;
    }

    public void setRefreshType(@NotNull RefreshType refreshType) {
        this.refreshType = refreshType;
    }

    public long getRefreshInterval() {
        return this.refreshInterval;
    }

    public long getRefreshIntervalMillis() {
        return this.refreshInterval * 1000L;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
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

    public boolean isRoundDecimals() {
        return roundDecimals;
    }

    public void setRoundDecimals(boolean roundDecimals) {
        this.roundDecimals = roundDecimals;
    }
}
