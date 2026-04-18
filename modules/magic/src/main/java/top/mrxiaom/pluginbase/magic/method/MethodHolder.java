package top.mrxiaom.pluginbase.magic.method;

import java.lang.invoke.*;

public class MethodHolder {
    private final Class<?> targetClass;
    private final MethodHandles.Lookup lookup;
    private final MethodHandle handle;
    private final Class<?> returnType;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    public MethodHolder(Class<?> targetClass, MethodHandles.Lookup lookup, MethodHandle handle, Class<?> returnType, String methodName, Class<?>[] parameterTypes) {
        this.targetClass = targetClass;
        this.lookup = lookup;
        this.handle = handle;
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public MethodHandles.Lookup getLookup() {
        return lookup;
    }

    public MethodHandle getHandle() {
        return handle;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public <T> T createAccessor(Class<?> interfaceType) throws Throwable {
        // noinspection unchecked
        return (T) LambdaMetafactory.metafactory(
                lookup,
                methodName,
                MethodType.methodType(interfaceType),
                MethodType.methodType(returnType, targetClass, parameterTypes),
                handle,
                MethodType.methodType(returnType, targetClass, parameterTypes)
        ).getTarget().invoke();
    }
}
