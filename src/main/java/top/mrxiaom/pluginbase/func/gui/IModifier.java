package top.mrxiaom.pluginbase.func.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModifier<T> {
    @NotNull
    T run(@NotNull T origin);

    @NotNull
    static <T> T fit(@Nullable IModifier<T> modifier, @NotNull T value) {
        return modifier == null ? value : modifier.run(value);
    }
}
