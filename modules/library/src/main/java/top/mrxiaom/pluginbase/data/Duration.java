package top.mrxiaom.pluginbase.data;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.StringJoiner;

public class Duration {
    private final int days, hours, minutes, seconds;
    private final long totalSeconds;

    public Duration(int days, int hours, int minutes, int seconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.totalSeconds =
                days * 24L * 60L * 60L
                + hours * 60L * 60L
                + minutes * 60L
                + seconds;
    }

    public int days() {
        return days;
    }

    public int hours() {
        return hours;
    }

    public int minutes() {
        return minutes;
    }

    public int seconds() {
        return seconds;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public LocalDateTime addFrom(LocalDateTime time) {
        return time.plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);
    }

    public String getDisplay() {
        return getDisplay("天", "时", "分", "秒");
    }

    public String getDisplay(String daysText, String hoursText, String minutesText, String secondsText) {
        StringJoiner joiner = new StringJoiner("");
        if (days > 0) joiner.add(days + daysText);
        if (hours > 0) joiner.add(hours + hoursText);
        if (minutes > 0) joiner.add(minutes + minutesText);
        if (seconds > 0) joiner.add(seconds + secondsText);
        return joiner.toString();
    }

    @Override
    public String toString() {
        return getDisplay("d", "h", "m", "s");
    }

    /**
     * 计算两个时间点之间的间隔时间
     * @param startTime 起始时间
     * @param endTime 结束时间
     */
    public static Duration between(LocalDateTime startTime, LocalDateTime endTime) {
        long totalSeconds = endTime.toEpochSecond(ZoneOffset.UTC) - startTime.toEpochSecond(ZoneOffset.UTC);
        return fromTotalSeconds(totalSeconds);
    }

    /**
     * 根据时间段的总秒数来解析间隔时间
     * @param totalSeconds 总秒数
     */
    public static Duration fromTotalSeconds(long totalSeconds) {
        int days = (int)((totalSeconds / 86400));
        int hours = (int)((totalSeconds / 3600) % 24);
        int minutes = (int)((totalSeconds / 60) % 60);
        int seconds = (int)(totalSeconds % 60);
        return new Duration(days, hours, minutes, seconds);
    }

    /**
     * 从字符串解析时间段
     * @param text 要解析的字符串，使用 d、h、m、s 格式
     */
    @NotNull
    public static Result<Duration> parse(String text) throws IllegalArgumentException {
        if (text.isEmpty()) {
            return Result.illegalArgs("Can't parse empty text");
        }
        char[] chars = text.toCharArray();
        Integer currentNum = null;
        int days = 0, hours = 0, minutes = 0, seconds = 0;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (ch >= '0' && ch <= '9') {
                int num = ch - '0';
                if (currentNum == null) {
                    currentNum = num;
                } else {
                    currentNum = (currentNum * 10) + num;
                }
                continue;
            }
            if (currentNum != null) {
                if (ch == 'd') {
                    days = currentNum;
                    currentNum = null;
                    continue;
                }
                if (ch == 'h') {
                    hours = currentNum;
                    currentNum = null;
                    continue;
                }
                if (ch == 'm') {
                    minutes = currentNum;
                    currentNum = null;
                    continue;
                }
                if (ch == 's') {
                    seconds = currentNum;
                    currentNum = null;
                    continue;
                }
            }
            return Result.illegalArgs("Unknown token '" + ch + "' at index " + i);
        }
        if (currentNum != null) {
            return Result.illegalArgs("Can't find end token for number '" + currentNum + "' in '" + text + "'");
        }
        return Result.ok(new Duration(days, hours, minutes, seconds));
    }
}
