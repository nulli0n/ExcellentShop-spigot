package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.IScheduled;
import su.nightexpress.nexshop.shop.Discount;
import su.nightexpress.nexshop.shop.virtual.editor.menu.DiscountMainEditor;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VirtualDiscount implements IScheduled, ICleanable, Placeholder {

    private VirtualShop    shop;
    private Set<DayOfWeek> days;
    private Set<LocalTime> times;
    private double         discount;
    private int            duration;

    private BukkitTask         updateTask;
    private DiscountMainEditor editor;

    private final PlaceholderMap placeholderMap;

    public VirtualDiscount() {
        this.setDays(new HashSet<>());
        this.setTimes(new HashSet<>());

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.DISCOUNT_CONFIG_DURATION, () -> TimeUtil.formatTime(this.getDuration() * 1000L))
            .add(Placeholders.DISCOUNT_CONFIG_DAYS, () -> String.join(", ", this.getDays().stream().map(DayOfWeek::name).toList()))
            .add(Placeholders.DISCOUNT_CONFIG_TIMES, () -> String.join(", ", this.getTimes().stream().map(TIME_FORMATTER::format).toList()))
            .add(Placeholders.DISCOUNT_CONFIG_AMOUNT, () -> NumberUtil.format(this.getDiscount()))
        ;
    }

    @NotNull
    public static VirtualDiscount read(@NotNull JYML cfg, @NotNull String path) {
        cfg.addMissing(path + ".Duration", 3600);
        cfg.saveChanges();

        VirtualDiscount config = new VirtualDiscount();
        config.setDiscount(cfg.getDouble(path + ".Discount", 0D));
        config.setDuration(cfg.getInt(path + ".Duration", 0));
        config.setDays(IScheduled.parseDays(cfg.getString(path + ".Activation.Days", "")));
        config.setTimes(IScheduled.parseTimes(cfg.getStringList(path + ".Activation.Times")));
        return config;
    }

    public static void write(@NotNull VirtualDiscount discount, @NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Discount", discount.getDiscount());
        cfg.set(path + ".Duration", discount.getDuration());
        cfg.set(path + ".Activation.Days", discount.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
        cfg.set(path + ".Activation.Times", discount.getTimes().stream().map(TIME_FORMATTER::format).toList());
    }

    @Override
    public void clear() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @Override
    public boolean canSchedule() {
        if (this.getDiscount() <= 0D || this.getDuration() <= 0) {
            return false;
        }
        return this.updateTask == null || this.updateTask.isCancelled();
    }

    @Override
    public void startScheduler() {
        this.updateTask = this.createScheduler();
    }

    @Override
    public void stopScheduler() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
        }
    }

    @Override
    @NotNull
    public Runnable getCommand() {
        return () -> {
            this.getShop().getDiscounts().add(new Discount(this.getDiscount(), this.getDuration()));
        };
    }

    @NotNull
    public DiscountMainEditor getEditor() {
        if (this.editor == null) {
            this.editor = new DiscountMainEditor(this.getShop(), this);
        }
        return editor;
    }

    @NotNull
    public VirtualShop getShop() {
        if (this.shop == null) {
            throw new IllegalStateException("Shop is undefined!");
        }
        return shop;
    }

    public void setShop(@NotNull VirtualShop shop) {
        this.shop = shop;
    }

    @NotNull
    @Override
    public Set<DayOfWeek> getDays() {
        return days;
    }

    @Override
    public void setDays(@NotNull Set<DayOfWeek> days) {
        this.days = days;
    }

    @NotNull
    @Override
    public Set<LocalTime> getTimes() {
        return times;
    }

    @Override
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
