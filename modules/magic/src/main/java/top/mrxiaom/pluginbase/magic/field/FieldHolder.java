package top.mrxiaom.pluginbase.magic.field;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FieldHolder<Target, FieldType> {
    private final Class<Target> targetClass;
    private final MethodHandles.Lookup lookup;
    private final Class<FieldType> fieldType;
    private final String fieldName;
    private final MethodHandle getterHandle;
    private final MethodHandle setterHandle;

    public FieldHolder(Class<Target> targetClass, MethodHandles.Lookup lookup, Class<FieldType> fieldType, String fieldName, MethodHandle getterHandle, MethodHandle setterHandle) {
        this.targetClass = targetClass;
        this.lookup = lookup;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.getterHandle = getterHandle;
        this.setterHandle = setterHandle;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public MethodHandles.Lookup getLookup() {
        return lookup;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldAccessor<Target, FieldType> createAccessor() throws Throwable {
        // noinspection unchecked
        Function<Target, FieldType> getter = (Function<Target, FieldType>)
                LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        MethodType.methodType(Function.class),
                        MethodType.methodType(Object.class, Object.class),
                        getterHandle,
                        MethodType.methodType(fieldType, targetClass)
                ).getTarget().invoke();
        // noinspection unchecked
        BiConsumer<Target, FieldType> setter = (BiConsumer<Target, FieldType>)
                LambdaMetafactory.metafactory(
                        lookup,
                        "accept",
                        MethodType.methodType(BiConsumer.class),
                        MethodType.methodType(void.class, Object.class, Object.class),
                        setterHandle,
                        MethodType.methodType(void.class, targetClass, fieldType)
                ).getTarget().invoke();
        return new FieldAccessor<>(getter, setter);
    }
}
