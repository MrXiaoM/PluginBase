package top.mrxiaom.pluginbase.temporary.period;

import com.google.common.collect.Lists;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public class EveryWeek implements Period {
    private final LocalTime time;
    private final List<DayOfWeek> weeks;
    public EveryWeek(LocalTime time, DayOfWeek... weeks) {
        this(time, Lists.newArrayList(weeks));
    }
    public EveryWeek(LocalTime time, Iterable<DayOfWeek> weeks) {
        this(time, Lists.newArrayList(weeks));
    }
    private EveryWeek(LocalTime time, List<DayOfWeek> weeks) {
        this.time = time;
        this.weeks = weeks;
        this.weeks.sort(Comparator.comparingInt(DayOfWeek::getValue));
        if (weeks.isEmpty()) {
            throw new IllegalArgumentException("输入的参数 weeks 无效，没有任何一个星期可用");
        }
    }

    public DayOfWeek getNextDay(DayOfWeek dayOfWeek) {
        for (DayOfWeek week : weeks) {
            if (week.getValue() > dayOfWeek.getValue()) {
                return week;
            }
        }
        return weeks.get(0);
    }

    public LocalDateTime getNextDay(DayOfWeek dayOfWeek, LocalDate monday) {
        DayOfWeek nextDay = getNextDay(dayOfWeek);
        // 下一周
        if (nextDay.getValue() <= dayOfWeek.getValue()) {
            return monday.plusDays(7 + nextDay.ordinal()).atTime(time);
        } else {
            return monday.plusDays(nextDay.ordinal()).atTime(time);
        }
    }

    @Override
    public LocalDateTime getNextOutdateTime(LocalDateTime now) {
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        LocalDate monday = getMonday(nowDate);
        DayOfWeek nowWeek = nowDate.getDayOfWeek();

        if (weeks.contains(nowWeek)) {
            // 如果今天在需要重置的星期内，需要考虑今天的时间有没有到
            if (nowTime.isAfter(time)) {
                return getNextDay(nowWeek, monday);
            } else {
                return nowDate.atTime(time);
            }
        } else {
            // 如果今天不在需要重置的星期内，无脑获取下一个日期
            return getNextDay(nowWeek, monday);
        }
    }

    public static LocalDate getMonday(LocalDate date) {
        int week = date.getDayOfWeek().ordinal();
        if (week == 0) {
            return date;
        } else {
            return date.minusDays(week);
        }
    }

    public static EveryWeek at(LocalTime time, DayOfWeek... weeks) {
        return new EveryWeek(time, weeks);
    }

    public static EveryWeek at(LocalTime time, Iterable<DayOfWeek> weeks) {
        return new EveryWeek(time, weeks);
    }
}
