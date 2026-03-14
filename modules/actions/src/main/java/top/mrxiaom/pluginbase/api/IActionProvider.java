package top.mrxiaom.pluginbase.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@FunctionalInterface
public interface IActionProvider {
    /**
     * IAction 提供器，返回 null 代表字符串不匹配当前 Action
     * @param input 输入值，可能是字符串，也可能是 {@link org.bukkit.configuration.ConfigurationSection}
     */
    @Nullable
    IAction provide(@NotNull Object input);

    /**
     * 处理优先级，数字越小越先处理
     */
    default int priority() {
        return 1000;
    }

    @NotNull
    static IActionProvider newProvider(int priority, @NotNull Function<Object, IAction> function) {
        return new IActionProvider() {
            @Override
            public @Nullable IAction provide(@NotNull Object input) {
                return function.apply(input);
            }

            @Override
            public int priority() {
                return priority;
            }
        };
    }
}
