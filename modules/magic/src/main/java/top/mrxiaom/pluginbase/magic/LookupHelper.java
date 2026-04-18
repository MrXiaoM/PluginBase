package top.mrxiaom.pluginbase.magic;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class LookupHelper {
    private static final Unsafe UNSAFE;
    private static final long ALLOWED_MODES_OFFSET;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            Field modesField = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            ALLOWED_MODES_OFFSET = UNSAFE.objectFieldOffset(modesField);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandles.Lookup getPrivateLookup(Class<?> targetClass) {
        MethodHandles.Lookup lookup = MethodHandles.lookup().in(targetClass);
        UNSAFE.putInt(lookup, ALLOWED_MODES_OFFSET, -1);
        return lookup;
    }
}
