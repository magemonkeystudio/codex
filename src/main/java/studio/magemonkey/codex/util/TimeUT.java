package studio.magemonkey.codex.util;

import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.core.config.CoreLang;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUT {

    private static final CodexEngine ENGINE = CodexEngine.get();

    @NotNull
    public static String formatTime(long time) {
        long days    = TimeUnit.MILLISECONDS.toDays(time);
        long hours   = TimeUnit.MILLISECONDS.toHours(time) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;

        CoreLang      lang = ENGINE.lang();
        StringBuilder str  = new StringBuilder();

        if (days > 0) {
            if (str.length() > 0) {
                str.append(" ");
            }
            str.append(lang.Time_Day.replace("%s%", days).getMsg());
        }
        if (hours > 0) {
            if (str.length() > 0) {
                str.append(" ");
            }
            str.append(lang.Time_Hour.replace("%s%", hours).getMsg());
        }
        if (minutes > 0) {
            if (str.length() > 0) {
                str.append(" ");
            }
            str.append(lang.Time_Min.replace("%s%", minutes).getMsg());
        }
        if (str.length() == 0 || seconds > 0) {
            if (str.length() > 0) {
                str.append(" ");
            }
            str.append(lang.Time_Sec.replace("%s%", seconds).getMsg());
        }

        return StringUT.oneSpace(str.toString());
    }

    @NotNull
    public static String formatTimeLeft(long max, long min) {
        long time = max - min;
        return formatTime(time);
    }

    @NotNull
    public static String formatTimeLeft(long until) {
        return formatTime(until - System.currentTimeMillis());
    }

    @NotNull
    public static LocalTime getLocalTimeOf(long ms) {
        long hours   = TimeUnit.MILLISECONDS.toHours(ms) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;

        return LocalTime.of((int) hours, (int) minutes, (int) seconds);
    }

    @NotNull
    public static LocalDateTime getLocalDateTimeOf(long ms) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), TimeZone.getDefault().toZoneId());
    }
}
