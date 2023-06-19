package su.nightexpress.nexshop.shop.util;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.StringUtil;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimeUtils {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    @NotNull
    public static Set<LocalTime> parseTimes(@NotNull List<String> list) {
        return list.stream().map(timeRaw -> LocalTime.parse(timeRaw, TIME_FORMATTER)).collect(Collectors.toSet());
    }

    @NotNull
    @Deprecated
    public static Set<LocalTime> parseTimesOld(@NotNull List<String> list) {
        return list.stream().map(timeRaw -> LocalTime.parse(timeRaw.split("-")[0], TIME_FORMATTER)).collect(Collectors.toSet());
    }

    @NotNull
    public static Set<DayOfWeek> parseDays(@NotNull String str) {
        return Stream.of(str.split(","))
            .map(raw -> StringUtil.getEnum(raw.trim(), DayOfWeek.class).orElse(null))
            .filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
