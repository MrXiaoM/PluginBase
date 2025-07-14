package top.mrxiaom.pluginbase.temporary;

import top.mrxiaom.pluginbase.temporary.period.Period;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.function.Supplier;

public class TemporaryDouble extends TemporaryData<Double> {
    public TemporaryDouble(Period period, Supplier<Double> defaultValue) {
        super(period, defaultValue);
    }

    @Override
    public String serializeData() {
        return String.valueOf(value);
    }

    @Override
    public Double deserializeData(String string) {
        return Util.parseDouble(string).orElseGet(defaultValue);
    }
}
