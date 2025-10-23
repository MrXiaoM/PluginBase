package top.mrxiaom.pluginbase.temporary.period;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EveryMonth implements Period {
    private final LocalTime time;
    private final List<Integer> days;
    public EveryMonth(LocalTime time, Integer... days) {
        this(time, Lists.newArrayList(days));
    }
    public EveryMonth(LocalTime time, Iterable<Integer> days) {
        this(time, Lists.newArrayList(days));
    }
    public EveryMonth(LocalTime time, List<Integer> days) {
        this.time = time;
        this.days = days;
        if (days.isEmpty()) {
            throw new IllegalArgumentException("输入的参数 days 无效，没有任何一个日期可用");
        }
    }

    public List<Integer> getDayListOfMonth(LocalDate date) {
        int length = date.lengthOfMonth();
        List<Integer> days = new ArrayList<>();
        for (Integer day : this.days) {
            // 正数，且在本月范围内的日期
            if (day > 0 && day <= length) {
                if (!days.contains(day)) {
                    days.add(day);
                }
            }
            // 负数，且在本月范围内的日期
            if (day <= 0 && day > -length) {
                int fin = length - day;
                if (!days.contains(fin)) {
                    days.add(fin);
                }
            }
        }
        days.sort(Comparator.comparingInt(Integer::intValue));
        if (days.isEmpty()) {
            throw new IllegalArgumentException("输入的参数 days 无效，无法计算出任何一个日期");
        }
        return days;
    }

    @Nullable
    public Integer getNextDay(LocalDate date, List<Integer> days) {
        int dayOfMonth = date.getDayOfMonth();
        for (Integer day : days) {
            if (day > dayOfMonth) return day;
        }
        return null;
    }

    public LocalDateTime getNextDate(LocalDate date, List<Integer> days) {
        Integer nextDay = getNextDay(date, days);
        if (nextDay != null) {
            // 如果这个月还有下一天可用，返回那一天
            return date.withDayOfMonth(nextDay).atTime(time);
        } else {
            // 如果这个月没有下一天可用了，返回下个月的第一个日期
            LocalDate nextMonth = date.withDayOfMonth(1).plusMonths(1);
            List<Integer> newDays = getDayListOfMonth(nextMonth);
            return nextMonth.withDayOfMonth(newDays.get(0)).atTime(time);
        }
    }

    @Override
    public LocalDateTime getNextOutdateTime(LocalDateTime now) {
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        List<Integer> days = getDayListOfMonth(nowDate);
        if (days.contains(nowDate.getDayOfMonth())) {
            // 如果今天在需要重置的日期内，需要考虑今天的时间有没有到
            if (nowTime.isAfter(time)) {
                return getNextDate(nowDate, days);
            } else {
                return nowDate.atTime(time);
            }
        } else {
            // 如果今天不在需要重置的日期内，无脑获取下一个日期
            return getNextDate(nowDate, days);
        }
    }

    public static EveryMonth at(LocalTime time, Integer... days) {
        return new EveryMonth(time, days);
    }

    public static EveryMonth at(LocalTime time, Iterable<Integer> days) {
        return new EveryMonth(time, days);
    }
}
