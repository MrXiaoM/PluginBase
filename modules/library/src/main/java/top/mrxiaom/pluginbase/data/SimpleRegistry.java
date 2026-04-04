package top.mrxiaom.pluginbase.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import top.mrxiaom.pluginbase.api.IRegistry;
import top.mrxiaom.pluginbase.api.WithPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 简单的注册表实现类
 */
public class SimpleRegistry<T extends WithPriority> implements IRegistry<T> {
    private final List<T> list = new ArrayList<>();

    @Override
    public void register(@NotNull T provider) {
        list.add(provider);
        resort();
    }

    @Override
    public boolean unregister(@NotNull T provider) {
        boolean result = list.remove(provider);
        resort();
        return result;
    }

    @Override
    public boolean unregister(@NotNull Predicate<T> filter) {
        boolean result = list.removeIf(filter);
        resort();
        return result;
    }

    @Override
    public void unregisterAll() {
        list.clear();
    }

    public void resort() {
        list.sort(Comparator.comparingInt(WithPriority::getPriority));
    }

    @Override
    public @NotNull @Unmodifiable List<T> all() {
        return Collections.unmodifiableList(list);
    }
}
