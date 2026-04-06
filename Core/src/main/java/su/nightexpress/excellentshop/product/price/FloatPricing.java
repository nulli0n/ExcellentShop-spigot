package su.nightexpress.excellentshop.product.price;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.RefreshType;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.excellentshop.product.ProductPricing;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FloatPricing extends ProductPricing {

    private final Map<TradeType, UniDouble> priceRange;

    private RefreshType    refreshType;
    private long           refreshInterval;
    private Set<DayOfWeek> days;
    private Set<LocalTime> times;
    private boolean        roundDecimals;

    public FloatPricing() {
        super(PriceType.FLOAT);
        this.priceRange = new HashMap<>();

        this.setRefreshType(RefreshType.INTERVAL);
        this.setRefreshInterval(-1L);
        this.days = Lists.newSet(DayOfWeek.values());
        this.times = new HashSet<>();
    }

    @NonNull
    public static FloatPricing read(@NonNull FileConfig config, @NonNull String path) {
        FloatPricing pricer = new FloatPricing();

        for (TradeType tradeType : TradeType.values()) {
            UniDouble price = UniDouble.read(config, path + "." + tradeType.name());
            pricer.setPriceRange(tradeType, price);
        }

        pricer.setRefreshType(config.getEnum(path + ".Refresh.Type", RefreshType.class, RefreshType.FIXED));
        pricer.setRefreshInterval(config.getLong(path + ".Refresh.Interval", -1L));
        pricer.setDays(ShopUtils.parseDays(config.getString(path + ".Refresh.Days", "")));
        pricer.setTimes(ShopUtils.parseTimes(config.getStringList(path + ".Refresh.Times")));
        pricer.setRoundDecimals(config.getBoolean(path + ".Round_Decimals"));

        return pricer;
    }

    @Override
    protected void writeAdditional(@NonNull FileConfig config, @NonNull String path) {
        this.priceRange.forEach((tradeType, price) -> {
            price.write(config, path + "." + tradeType.name());
        });
        config.set(path + ".Refresh.Type", this.refreshType.name());
        config.set(path + ".Refresh.Interval", this.refreshInterval);
        config.set(path + ".Refresh.Days", this.days.stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
        config.set(path + ".Refresh.Times", ShopUtils.serializeTimes(this.times));
        config.set(path + ".Round_Decimals", this.roundDecimals);
    }

    public double rollPrice(@NonNull TradeType type) {
        double rolled = this.getRange(type).roll();

        return this.roundDecimals ? Math.floor(rolled) : NumberUtil.round(rolled);
    }

    @Override
    public boolean shouldResetOnExpire() {
        return true;
    }

    @Override
    public void handleTransaction(@NonNull ECompletedTransaction transaction, @NonNull Product product, int units, @NonNull PriceData priceData) {
        this.preventStale(priceData);

        if (priceData.isExpired()) {
            this.updatePrice(product, priceData);
        }
    }

    @Override
    public void updatePrice(@NonNull Product product, @NonNull PriceData priceData) {
        this.preventStale(priceData);

        if (priceData.isExpired()) {
            for (TradeType tradeType : TradeType.values()) {
                double average = this.getAveragePrice(tradeType);
                double roll = this.rollPrice(tradeType);
                double offset = average <= 0D ? 0D : roll / average;

                priceData.setOffset(tradeType, offset);
            }

            priceData.setExpireDate(this.getClosestTimestamp());
            priceData.markDirty();
        }

        for (TradeType tradeType : TradeType.values()) {
            double average = this.getAveragePrice(tradeType);
            double offset = priceData.getOffset(tradeType);
            double price = average * offset;

            product.setPrice(tradeType, price);
        }

        if (product.hasBuyPrice() && product.getSellPrice() > product.getBuyPrice()) {
            product.setSellPrice(product.getSellPrice());
        }
    }

    // If user didn't set update times, float price won't refresh until added.
    private void preventStale(@NonNull PriceData priceData) {
        if (priceData.isExpirable()) return;

        long closestTimestamp = this.getClosestTimestamp();
        if (closestTimestamp >= 0L) {
            priceData.setExpired();
            priceData.markDirty();
        }
    }

    @Nullable
    public Instant getNextScheduledUpdateTime() {
        if (this.days.isEmpty() || this.times.isEmpty()) return null;

        ZonedDateTime now = ZonedDateTime.now(TimeUtil.getZoneId());
        ZonedDateTime next = null;

        for (DayOfWeek day : this.days) {
            for (LocalTime time : this.times) {
                ZonedDateTime candidate = now.with(TemporalAdjusters.nextOrSame(day)).with(time);

                // If today is the right day but time already passed, shift to next week
                if (candidate.isBefore(now)) {
                    candidate = candidate.plusWeeks(1);
                }

                if (next == null || candidate.isBefore(next)) {
                    next = candidate;
                }
            }
        }

        return next == null ? null : next.toInstant();
    }

    public long getClosestTimestamp() {
        if (this.refreshType == RefreshType.INTERVAL) {
            return this.refreshInterval <= 0L ? -1L : TimeUtil.createTimestamp(this.refreshInterval);
        }

        Instant dateTime = this.getNextScheduledUpdateTime();
        return dateTime == null ? -1L : dateTime.toEpochMilli();//TimeUtil.toEpochMillis(dateTime);
    }

    @NonNull
    public UniDouble getRange(@NonNull TradeType tradeType) {
        return this.priceRange.computeIfAbsent(tradeType, k -> UniDouble.of(DISABLED, DISABLED));
    }

    @Override
    public double getAveragePrice(@NonNull TradeType tradeType) {
        UniDouble range = this.getRange(tradeType);
        double min = range.getMinValue();
        double max = range.getMaxValue();
        return (min + max) / 2D;
    }

    public double getMin(@NonNull TradeType tradeType) {
        return this.getRange(tradeType).getMinValue();
    }

    public double getMax(@NonNull TradeType tradeType) {
        return this.getRange(tradeType).getMaxValue();
    }

    public void setPriceRange(@NonNull TradeType tradeType, @NonNull UniDouble price) {
        this.priceRange.put(tradeType, price);
    }

    @NonNull
    public RefreshType getRefreshType() {
        return this.refreshType;
    }

    public void setRefreshType(@NonNull RefreshType refreshType) {
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

    public boolean hasDay(@NonNull DayOfWeek day) {
        return this.days.contains(day);
    }

    public void addDay(@NonNull DayOfWeek day) {
        this.days.add(day);
    }

    public void removeDay(@NonNull DayOfWeek day) {
        this.days.remove(day);
    }

    @NonNull
    public Set<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(@NonNull Set<DayOfWeek> days) {
        this.days = days;
    }

    @NonNull
    public Set<LocalTime> getTimes() {
        return times;
    }

    public void setTimes(@NonNull Set<LocalTime> times) {
        this.times = times;
    }

    public boolean isRoundDecimals() {
        return roundDecimals;
    }

    public void setRoundDecimals(boolean roundDecimals) {
        this.roundDecimals = roundDecimals;
    }
}
