package top.mrxiaom.pluginbase.temporary;

import top.mrxiaom.pluginbase.temporary.period.Period;

import java.util.function.Supplier;

public class TemporaryString extends TemporaryData<String> {
    public TemporaryString(Period period, Supplier<String> defaultValue) {
        super(period, defaultValue);
    }

    @Override
    public String serializeData() {
        return String.valueOf(value);
    }

    @Override
    public String deserializeData(String string) {
        return string;
    }
}
