package su.nightexpress.nexshop.product.price.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.api.type.RefreshType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.product.price.ProductPricing;
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

    @NotNull
    public static FloatPricing read(@NotNull FileConfig config, @NotNull String path) {
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
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        this.priceRange.forEach((tradeType, price) -> {
            price.write(config, path + "." + tradeType.name());
        });
        config.set(path + ".Refresh.Type", this.refreshType.name());
        config.set(path + ".Refresh.Interval", this.refreshInterval);
        config.set(path + ".Refresh.Days", this.days.stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
        config.set(path + ".Refresh.Times", ShopUtils.serializeTimes(this.times));
        config.set(path + ".Round_Decimals", this.roundDecimals);
    }

    public double rollPrice(@NotNull TradeType type) {
        double rolled = this.getRange(type).roll();

        return this.roundDecimals ? Math.floor(rolled) : NumberUtil.round(rolled);
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event, @NotNull Product product, @NotNull PriceData priceData) {
        if (priceData.isExpired()) {
            this.updatePrice(product, priceData);
        }
    }

    @Override
    public void updatePrice(@NotNull Product product, @NotNull PriceData priceData) {
        if (priceData.isExpired()) {
            for (TradeType tradeType : TradeType.values()) {
                double average = this.getAveragePrice(tradeType);
                double roll = this.rollPrice(tradeType);
                double offset = average <= 0D ? 0D : roll / average;

                priceData.setOffset(tradeType, offset);
            }

            priceData.setExpireDate(this.getClosestTimestamp());
            priceData.setSaveRequired(true);
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
            return this.refreshInterval <= 0L ? 0L : TimeUtil.createTimestamp(this.refreshInterval);
        }

        Instant dateTime = this.getNextScheduledUpdateTime();
        return dateTime == null ? 0L : dateTime.toEpochMilli();//TimeUtil.toEpochMillis(dateTime);
    }

    @NotNull
    public UniDouble getRange(@NotNull TradeType tradeType) {
        return this.priceRange.computeIfAbsent(tradeType, k -> UniDouble.of(DISABLED, DISABLED));
    }

    @Override
    public double getAveragePrice(@NotNull TradeType tradeType) {
        UniDouble range = this.getRange(tradeType);
        double min = range.getMinValue();
        double max = range.getMaxValue();
        return (min + max) / 2D;
    }

    public double getMin(@NotNull TradeType tradeType) {
        return this.getRange(tradeType).getMinValue();
    }

    public double getMax(@NotNull TradeType tradeType) {
        return this.getRange(tradeType).getMaxValue();
    }

    public void setPriceRange(@NotNull TradeType tradeType, @NotNull UniDouble price) {
        this.priceRange.put(tradeType, price);
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

    public boolean hasDay(@NotNull DayOfWeek day) {
        return this.days.contains(day);
    }

    public void addDay(@NotNull DayOfWeek day) {
        this.days.add(day);
    }

    public void removeDay(@NotNull DayOfWeek day) {
        this.days.remove(day);
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
