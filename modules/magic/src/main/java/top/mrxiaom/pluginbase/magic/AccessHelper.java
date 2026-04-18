package top.mrxiaom.pluginbase.magic;

import top.mrxiaom.pluginbase.magic.field.FieldHolder;
import top.mrxiaom.pluginbase.magic.method.MethodHolder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class AccessHelper<Target> {
    private final Class<Target> targetClass;
    private final MethodHandles.Lookup lookup;

    private AccessHelper(Class<Target> targetClass, MethodHandles.Lookup lookup) {
        this.targetClass = targetClass;
        this.lookup = lookup;
    }

    public static <Target> AccessHelper<Target> access(Class<Target> targetClass, MethodHandles.Lookup lookup) {
        return new AccessHelper<>(targetClass, lookup);
    }

    public static <Target> AccessHelper<Target> access(Class<Target> targetClass) {
        MethodHandles.Lookup lookup = LookupHelper.getPrivateLookup(targetClass);
        return new AccessHelper<>(targetClass, lookup);
    }

    public MethodHolder method(
            Class<?> returnType,
            String methodName,
            Class<?>... parameterTypes
    ) throws ReflectiveOperationException {
        MethodHandle handle = lookup.findVirtual(
                targetClass,
                methodName,
                MethodType.methodType(returnType, parameterTypes)
        );
        return new MethodHolder(targetClass, lookup, handle, returnType, methodName, parameterTypes);
    }

    public <FieldType> FieldHolder<Target, FieldType> field(
            Class<FieldType> fieldType,
            String fieldName
    ) throws ReflectiveOperationException {
        MethodHandle getterHandle = lookup.findGetter(targetClass, fieldName, fieldType);
        MethodHandle setterHandle = lookup.findSetter(targetClass, fieldName, fieldType);
        return new FieldHolder<>(targetClass, lookup, fieldType, fieldName, getterHandle, setterHandle);
    }
}
