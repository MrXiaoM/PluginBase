package top.mrxiaom.pluginbase.temporary;

import top.mrxiaom.pluginbase.temporary.period.Period;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.function.Supplier;

public class TemporaryEnum<T> extends TemporaryData<T> {
    private final Class<T> type;
    public TemporaryEnum(Period period, Class<T> type, Supplier<T> defaultValue) {
        super(period, defaultValue);
        this.type = type;
    }

    @Override
    public String serializeData() {
        return String.valueOf(value);
    }

    @Override
    public T deserializeData(String string) {
        T value = Util.valueOr(type, string, null);
        if (value == null) {
            return getDefaultValue();
        } else {
            return value;
        }
    }
}
