package top.mrxiaom.pluginbase.func.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public interface ITagProvider {
    /**
     * Tag 提供器，返回 null 代表不匹配此 Tag
     */
    @Nullable
    Object provide(ConfigurationSection section);

    /**
     * 处理优先级，数字越小越先处理
     */
    default int priority() {
        return 1000;
    }
}
