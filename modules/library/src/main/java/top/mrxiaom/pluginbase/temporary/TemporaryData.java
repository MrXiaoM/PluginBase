package top.mrxiaom.pluginbase.temporary;

import top.mrxiaom.pluginbase.temporary.period.Period;
import top.mrxiaom.pluginbase.utils.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

/**
 * 过时自动重置数据
 */
public abstract class TemporaryData<T> {
    public static final DateTimeFormatter format = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    protected T value;
    protected LocalDateTime nextOutdateTime;
    protected Period period;
    protected Supplier<T> defaultValue;
    protected TemporaryData(Period period, Supplier<T> defaultValue) {
        this.period = period;
        this.defaultValue = defaultValue;
        this.value = defaultValue.get();
    }

    /**
     * 设置数值重置周期，并自动检查更新数值
     * @param period 周期
     */
    public void setPeriod(Period period) {
        setPeriod(period, true);
    }

    /**
     * 设置数值重置周期，决定是否更新数值
     * @param period 周期
     * @param update 是否更新数值
     */
    public void setPeriod(Period period, boolean update) {
        this.period = period;
        if (update && isOutdated()) {
            applyDefaultValue();
        }
    }

    /**
     * 获取数值重置周期
     */
    public Period getPeriod() {
        return period;
    }

    /**
     * 设置数值，并更新下次刷新时间
     */
    public void setValue(T data) {
        this.value = data;
        this.nextOutdateTime = period.getNextOutdateTime();
    }

    /**
     * 获取数值
     */
    public T getValue() {
        if (this.isOutdated()) {
            this.applyDefaultValue();
        }
        return value;
    }

    /**
     * 获取默认数值
     */
    public T getDefaultValue() {
        return defaultValue.get();
    }

    /**
     * 设置默认数值
     */
    public void setDefaultValue(Supplier<T> def) {
        this.defaultValue = def;
    }

    /**
     * 设置默认数值
     */
    public void setDefaultValue(T def) {
        this.defaultValue = () -> def;
    }

    /**
     * 将数值设为默认数值，并更新上次更新时间
     */
    public void applyDefaultValue() {
        setValue(getDefaultValue());
    }

    public LocalDateTime getNextOutdateTime() {
        return nextOutdateTime;
    }

    /**
     * 获取数值是否已过期
     */
    public boolean isOutdated() {
        return nextOutdateTime != null && LocalDateTime.now().isAfter(nextOutdateTime);
    }

    public abstract String serializeData();
    public abstract T deserializeData(String string);

    @SuppressWarnings("StringBufferReplaceableByString")
    public String serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append(dateTime(nextOutdateTime));
        builder.append(";").append(serializeData());
        return builder.toString();
    }

    public void deserialize(String string) {
        List<String> split = Util.split(string, ';', 2);
        List<String> arguments = Util.split(split.get(0), ',');
        this.nextOutdateTime = dateTime(arguments.get(0));
        if (this.isOutdated()) {
            this.applyDefaultValue();
        } else {
            this.value = deserializeData(split.get(1));
        }
    }

    public static String dateTime(LocalDateTime time) {
        return time.format(format);
    }

    public static LocalDateTime dateTime(String str) {
        return LocalDateTime.parse(str, format);
    }
}
