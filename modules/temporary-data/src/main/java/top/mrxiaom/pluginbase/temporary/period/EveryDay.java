package top.mrxiaom.pluginbase.temporary.period;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EveryDay implements Period {
    private final LocalTime time;
    public EveryDay(LocalTime time) {
        this.time = time;
    }

    @Override
    public LocalDateTime getNextOutdateTime(LocalDateTime now) {
        if (now.toLocalTime().isAfter(time)) {
            return LocalDate.now().plusDays(1).atTime(time);
        } else {
            return LocalDate.now().atTime(time);
        }
    }

    public static EveryDay at(LocalTime time) {
        return new EveryDay(time);
    }
}
