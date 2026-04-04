package top.mrxiaom.pluginbase.api;

public interface WithPriority {
    /**
     * 优先级数值，数值越小，越先匹配
     */
    default int getPriority() {
        return 1000;
    }
}
