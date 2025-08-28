package top.mrxiaom.pluginbase.utils;

import org.bukkit.Keyed;
import org.bukkit.Registry;

public class RegistryUtils {
    @SuppressWarnings("unchecked")
    public static <T> T fromType(Class<T> type, String s) {
        Registry<?> registry = RegistryConverter.fromType(type);
        if (registry != null) {
            Keyed matched = registry.match(s);
            if (/*matched != null && */type.isInstance(matched)) {
                return (T) matched;
            }
            Keyed newerMatched = registry.match(s.replace('_', '.'));
            if (/*newerMatched != null && */type.isInstance(matched)) {
                return (T) newerMatched;
            }
        }
        return null;
    }
}
