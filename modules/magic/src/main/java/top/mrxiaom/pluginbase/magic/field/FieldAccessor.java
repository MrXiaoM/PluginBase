package top.mrxiaom.pluginbase.magic.field;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FieldAccessor<Target, FieldType> {
    private final Function<Target, FieldType> getter;
    private final BiConsumer<Target, FieldType> setter;
    public FieldAccessor(Function<Target, FieldType> getter, BiConsumer<Target, FieldType> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public FieldType get(Target $this) {
        return getter.apply($this);
    }
    public void set(Target $this, FieldType value) {
        setter.accept($this, value);
    }
}
