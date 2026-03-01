package top.mrxiaom.pluginbase.utils;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegistryUtils {
    @SuppressWarnings("unchecked")
    public static <T> T fromType(Class<T> type, String s) {
        Registry<?> registry = RegistryConverter.fromType(type);
        if (registry != null) {
            Keyed matched = match(registry, s);
            if (/*matched != null && */type.isInstance(matched)) {
                return (T) matched;
            }
            Keyed newerMatched = match(registry, s.replace('_', '.'));
            if (/*newerMatched != null && */type.isInstance(matched)) {
                return (T) newerMatched;
            }
        }
        return null;
    }

    @Nullable
    public static <T extends Keyed> T match(@NotNull Registry<T> registry, @NotNull String input) {
        String filtered = input.toLowerCase().replaceAll("\\s+", "_");
        NamespacedKey namespacedKey = NamespacedKey.fromString(filtered);
        return (namespacedKey != null) ? registry.get(namespacedKey) : null;
    }
}
