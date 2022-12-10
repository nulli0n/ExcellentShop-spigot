package su.nightexpress.nexshop.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.CollectionsUtil;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface IScheduled {

    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    @NotNull
    static Set<LocalTime> parseTimes(@NotNull List<String> list) {
        return list.stream().map(timeRaw -> LocalTime.parse(timeRaw, TIME_FORMATTER)).collect(Collectors.toSet());
    }

    @NotNull
    @Deprecated
    static Set<LocalTime> parseTimesOld(@NotNull List<String> list) {
        return list.stream().map(timeRaw -> LocalTime.parse(timeRaw.split("-")[0], TIME_FORMATTER)).collect(Collectors.toSet());
    }

    @NotNull
    static Set<DayOfWeek> parseDays(@NotNull String str) {
        return Stream.of(str.split(","))
            .map(raw -> CollectionsUtil.getEnum(raw.trim(), DayOfWeek.class))
            .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @NotNull Set<DayOfWeek> getDays();

    void setDays(@NotNull Set<DayOfWeek> days);

    @NotNull Set<LocalTime> getTimes();

    void setTimes(@NotNull Set<LocalTime> times);

    @NotNull Runnable getCommand();

    boolean canSchedule();

    void startScheduler();

    void stopScheduler();

    @Nullable
    default ScheduledFuture<?> createScheduler() {
        LocalDateTime updateTime = this.getClosestDate();
        if (updateTime == null || !this.canSchedule()) return null;

        long delay = LocalDateTime.now().until(updateTime, ChronoUnit.MILLIS);

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> future = service.schedule(this.getCommand(), delay, TimeUnit.MILLISECONDS);
        service.schedule(this::startScheduler, delay + 1000L, TimeUnit.MILLISECONDS);
        service.shutdown();
        return future;
    }

    @Nullable
    private LocalDateTime getClosestDate() {
        if (this.getDays().isEmpty()) return null;
        if (this.getTimes().isEmpty()) return null;

        LocalDateTime closest = LocalDateTime.now();
        while (!this.getDays().contains(closest.getDayOfWeek())) {
            closest = closest.plusDays(1);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closest1 = closest;
        return this.getTimes().stream()
            .map(timeSch -> LocalDateTime.of(closest1.toLocalDate(), timeSch))
            .filter(now::isBefore).min(LocalDateTime::compareTo).orElse(null);
    }
}
