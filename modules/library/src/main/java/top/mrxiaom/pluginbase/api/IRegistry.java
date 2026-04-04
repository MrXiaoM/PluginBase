package top.mrxiaom.pluginbase.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Predicate;

/**
 * 简单的注册表接口
 * @param <T> 注册的类型
 */
public interface IRegistry<T> {
    /**
     * 注册项目到注册表中
     */
    void register(@NotNull T provider);

    /**
     * 从注册表中注销项目
     */
    boolean unregister(@NotNull T provider);

    /**
     * 从注册表中注销项目
     */
    boolean unregister(@NotNull Predicate<T> filter);

    /**
     * 从注册表中注销所有项目
     */
    void unregisterAll();

    /**
     * 获取所有项目
     */
    @NotNull @Unmodifiable List<T> all();
}
