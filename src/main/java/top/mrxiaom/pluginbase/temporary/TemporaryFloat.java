package top.mrxiaom.pluginbase.temporary;

import top.mrxiaom.pluginbase.temporary.period.Period;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.function.Supplier;

public class TemporaryFloat extends TemporaryData<Float> {
    protected TemporaryFloat(Period period, Supplier<Float> defaultValue) {
        super(period, defaultValue);
    }

    @Override
    public String serializeData() {
        return String.valueOf(value);
    }

    @Override
    public Float deserializeData(String string) {
        return Util.parseFloat(string).orElseGet(defaultValue);
    }
}
