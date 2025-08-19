package top.mrxiaom.pluginbase.func.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
