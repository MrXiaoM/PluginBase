package top.mrxiaom.pluginbase.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@FunctionalInterface
public interface IActionProvider {
    /**
     * IAction 提供器，返回 null 代表字符串不匹配当前 Action
     */
    @Nullable
    IAction provide(@NotNull String s);

    /**
     * 处理优先级，数字越小越先处理
     */
    default int priority() {
        return 1000;
    }

    @NotNull
    static IActionProvider newProvider(int priority, @NotNull Function<String, IAction> function) {
        return new IActionProvider() {
            @Override
            public @Nullable IAction provide(@NotNull String s) {
                return function.apply(s);
            }

            @Override
            public int priority() {
                return priority;
            }
        };
    }
}
