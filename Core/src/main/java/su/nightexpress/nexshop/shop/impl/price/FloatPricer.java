package su.nightexpress.nexshop.shop.impl.price;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FloatPricer extends RangedPricer {

    private Set<DayOfWeek>   days;
    private Set<LocalTime> times;
    private boolean roundDecimals;

    public FloatPricer() {
        super(PriceType.FLOAT);
        this.days = new HashSet<>(Arrays.asList(DayOfWeek.values()));
        this.times = new HashSet<>();
        this.placeholderMap.add(Placeholders.forFloatPricer(this));
    }

    @NotNull
    public static FloatPricer read(@NotNull FileConfig config, @NotNull String path) {
        FloatPricer pricer = new FloatPricer();
        for (TradeType tradeType : TradeType.values()) {
            UniDouble price = UniDouble.read(config, path + "." + tradeType.name());
            pricer.setPriceRange(tradeType, price);
        }
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
        config.set(path + ".Refresh.Days", this.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
        config.set(path + ".Refresh.Times", this.getTimes().stream().map(ShopUtils.TIME_FORMATTER::format).toList());
        config.set(path + ".Round_Decimals", this.isRoundDecimals());
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
        LocalDateTime dateTime = this.getClosest();
        return dateTime == null ? 0L : TimeUtil.toEpochMillis(dateTime);
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
