package top.mrxiaom.pluginbase.magic;

import java.lang.invoke.MethodHandles;

public class LookupHelper {
    public static MethodHandles.Lookup getPrivateLookup(Class<?> targetClass) {
        try {
            return MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
