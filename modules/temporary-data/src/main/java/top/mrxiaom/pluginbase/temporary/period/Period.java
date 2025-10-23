package top.mrxiaom.pluginbase.temporary.period;

import java.time.LocalDateTime;

public interface Period {
    LocalDateTime getNextOutdateTime(LocalDateTime now);

    default LocalDateTime getNextOutdateTime() {
        return getNextOutdateTime(LocalDateTime.now());
    }
}
