package top.mrxiaom.pluginbase.temporary;

import top.mrxiaom.pluginbase.temporary.period.Period;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.function.Supplier;

public class TemporaryInteger extends TemporaryData<Integer> {
    protected TemporaryInteger(Period period, Supplier<Integer> defaultValue) {
        super(period, defaultValue);
    }

    @Override
    public String serializeData() {
        return String.valueOf(value);
    }

    @Override
    public Integer deserializeData(String string) {
        return Util.parseInt(string).orElseGet(defaultValue);
    }
}
