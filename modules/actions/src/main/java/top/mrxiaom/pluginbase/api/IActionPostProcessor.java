package top.mrxiaom.pluginbase.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IActionPostProcessor extends WithPriority {
    /**
     * IAction 后处理器，便于其它插件对本插件进行修改
     * @param input 输入的配置
     * @param provider 解析这个 IAction 的 IActionProvider
     * @param action 通过 IActionProvider 解析出来的 IAction
     * @return 输出的 IAction
     */
    @Nullable IAction process(@NotNull Object input, @NotNull IActionProvider provider, @NotNull IAction action);

    default int priority() {
        return 1000;
    }
}
