package su.nightexpress.nexshop.api;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.CollectionsUtil;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractTimed implements ITimed {

    private final Set<DayOfWeek>   days;
    private final Set<LocalTime[]> times;

    public AbstractTimed(@NotNull Set<DayOfWeek> days, @NotNull Set<LocalTime[]> times) {
        this.days = days;
        this.times = times;
    }

    @Override
    @NotNull
    public Set<DayOfWeek> getDays() {
        return this.days;
    }

    @Override
    @NotNull
    public Set<LocalTime[]> getTimes() {
        return this.times;
    }

    @NotNull
    public static Set<LocalTime[]> parseTimes(@NotNull List<String> list) {
        Set<LocalTime[]> times = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

        list.forEach(timeRaw -> {
            String[] split = timeRaw.split("-");
            if (split.length < 2) return;

            LocalTime start = LocalTime.parse(split[0], formatter);
            LocalTime end = LocalTime.parse(split[1], formatter);
            times.add(new LocalTime[]{start, end});
        });
        return times;
    }

    @NotNull
    public static Set<DayOfWeek> parseDays(@NotNull String str) {
        Set<DayOfWeek> days = new HashSet<>();
        for (String split : str.split(",")) {
            DayOfWeek day = CollectionsUtil.getEnum(split.trim(), DayOfWeek.class);
            if (day != null) days.add(day);
        }
        return days;
    }
}
