package top.mrxiaom.pluginbase.func.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

/**
 * 数值修饰器，常用于让开发者可修改物品的名称、lore 等显示文字
 * @param <T> 数值类型
 */
public interface IModifier<T> {
    @NotNull
    T run(@NotNull T origin);

    @NotNull
    static <T> T fit(@Nullable IModifier<T> modifier, @NotNull T value) {
        return modifier == null ? value : modifier.run(value);
    }

    static IModifier<String> replaceString(List<Pair<String, Object>> r) {
        return r == null ? null : (input -> Pair.replace(input, r));
    }

    static IModifier<List<String>> replaceList(List<Pair<String, Object>> r) {
        return r == null ? null : (input -> Pair.replace(input, r));
    }
}
