package top.mrxiaom.pluginbase.data;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DurationDisplayFormat {
    public static final DurationDisplayFormat DEFAULT = new DurationDisplayFormat("", "天", "天", "时", "时", "分", "分", "秒", "秒");
    public static final DurationDisplayFormat ENGLISH = new DurationDisplayFormat(", ", " days", " day", " hours", " hour", " minutes", " minute", " seconds", " second");
    private final String delimiter;
    private final String days, day, hours, hour, minutes, minute, seconds, second;

    public DurationDisplayFormat(String delimiter, String days, String day, String hours, String hour, String minutes, String minute, String seconds, String second) {
        this.delimiter = delimiter;
        this.days = days;
        this.day = day;
        this.hours = hours;
        this.hour = hour;
        this.minutes = minutes;
        this.minute = minute;
        this.seconds = seconds;
        this.second = second;
    }

    @NotNull
    public String get(@NotNull Duration duration) {
        return duration.getDisplay(delimiter, day, days, hour, hours, minute, minutes, second, seconds);
    }

    @NotNull
    public static DurationDisplayFormat load(@Nullable ConfigurationSection section) {
        return load(section, DEFAULT);
    }

    @NotNull
    public static DurationDisplayFormat load(@Nullable ConfigurationSection section, @NotNull DurationDisplayFormat defaultValues) {
        if (section == null) return defaultValues;

        String delimiter = section.getString("delimiter", defaultValues.delimiter);
        String days = section.getString("days", defaultValues.days);
        String day = section.getString("day", defaultValues.day);
        String hours = section.getString("hours", defaultValues.hours);
        String hour = section.getString("hour", defaultValues.hour);
        String minutes = section.getString("minutes", defaultValues.minutes);
        String minute = section.getString("minute", defaultValues.minute);
        String seconds = section.getString("seconds", defaultValues.seconds);
        String second = section.getString("second", defaultValues.second);

        return new DurationDisplayFormat(delimiter, days, day, hours, hour, minutes, minute, seconds, second);
    }

}
