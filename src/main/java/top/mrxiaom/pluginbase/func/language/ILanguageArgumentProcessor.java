package top.mrxiaom.pluginbase.func.language;

import org.jetbrains.annotations.Nullable;

public interface ILanguageArgumentProcessor {
    /**
     * 对输入参数进行转换
     * @param holder 要替换变量的 holder
     * @param key 参数键（不使用 Pair 而是使用 String.format 时为 null）
     * @param value 输入的参数值
     * @return 转换后的参数
     */
    Object execute(AbstractLanguageHolder holder, @Nullable String key, Object value);
}
