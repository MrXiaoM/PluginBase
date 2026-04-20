package top.mrxiaom.pluginbase.magic.method;

import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.*;

public class MethodHolder {
    private final Class<?> targetClass;
    private final MethodHandles.Lookup lookup;
    private final MethodHandle handle;
    private final Class<?> returnType;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final boolean isStatic;
    @ApiStatus.Internal
    public MethodHolder(Class<?> targetClass, MethodHandles.Lookup lookup, MethodHandle handle, Class<?> returnType, String methodName, Class<?>[] parameterTypes, boolean isStatic) {
        this.targetClass = targetClass;
        this.lookup = lookup;
        this.handle = handle;
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.isStatic = isStatic;
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
        CallSite callSite;
        if (isStatic) {
            callSite = LambdaMetafactory.metafactory(
                    lookup,
                    methodName,
                    MethodType.methodType(interfaceType),
                    MethodType.methodType(returnType, parameterTypes),
                    handle,
                    MethodType.methodType(returnType, parameterTypes)
            );
        } else {
            callSite = LambdaMetafactory.metafactory(
                    lookup,
                    methodName,
                    MethodType.methodType(interfaceType),
                    MethodType.methodType(returnType, targetClass, parameterTypes),
                    handle,
                    MethodType.methodType(returnType, targetClass, parameterTypes)
            );
        }
        // noinspection unchecked
        return (T) callSite.getTarget().invoke();
    }
}
