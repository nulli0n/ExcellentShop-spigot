package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.placeholder.Placeholder;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VirtualDiscount implements Placeholder {

    private StaticShop     shop;
    private Set<DayOfWeek> days;
    private Set<LocalTime> times;
    private double         discount;
    private int            duration;

    private final PlaceholderMap placeholderMap;

    public VirtualDiscount() {
        this.setDays(new HashSet<>());
        this.setTimes(new HashSet<>());

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.DISCOUNT_CONFIG_DURATION, () -> TimeUtil.formatTime(this.getDuration() * 1000L))
            .add(Placeholders.DISCOUNT_CONFIG_DAYS, () -> String.join(", ", this.getDays().stream().map(DayOfWeek::name).toList()))
            .add(Placeholders.DISCOUNT_CONFIG_TIMES, () -> String.join(", ", this.getTimes().stream().map(ShopUtils.TIME_FORMATTER::format).toList()))
            .add(Placeholders.DISCOUNT_CONFIG_AMOUNT, () -> NumberUtil.format(this.getDiscount()))
        ;
    }

    @NotNull
    public static VirtualDiscount read(@NotNull FileConfig cfg, @NotNull String path) {
        cfg.addMissing(path + ".Duration", 3600);
        cfg.saveChanges();

        VirtualDiscount config = new VirtualDiscount();
        config.setDiscount(cfg.getDouble(path + ".Discount", 0D));
        config.setDuration(cfg.getInt(path + ".Duration", 0));
        config.setDays(ShopUtils.parseDays(cfg.getString(path + ".Activation.Days", "")));
        config.setTimes(ShopUtils.parseTimes(cfg.getStringList(path + ".Activation.Times")));
        return config;
    }

    public static void write(@NotNull VirtualDiscount discount, @NotNull FileConfig cfg, @NotNull String path) {
        cfg.set(path + ".Discount", discount.getDiscount());
        cfg.set(path + ".Duration", discount.getDuration());
        cfg.set(path + ".Activation.Days", discount.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
        cfg.set(path + ".Activation.Times", discount.getTimes().stream().map(ShopUtils.TIME_FORMATTER::format).toList());
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    public void update() {
        this.getShop().getDiscounts().add(new Discount(this.getDiscount(), this.getDuration()));
    }

    public boolean isDiscountTime() {
        if (this.getDays().isEmpty()) return false;
        if (this.getTimes().isEmpty()) return false;
        if (!this.getDays().contains(LocalDate.now().getDayOfWeek())) return false;

        LocalTime roundNow = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        return this.getTimes().stream().anyMatch(time -> {
            return time.truncatedTo(ChronoUnit.MINUTES).equals(roundNow);
        });
    }

    @NotNull
    public StaticShop getShop() {
        if (this.shop == null) {
            throw new IllegalStateException("Shop is undefined!");
        }
        return shop;
    }

    public void setShop(@NotNull StaticShop shop) {
        this.shop = shop;
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

    public double getDiscount() {
        return this.discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
