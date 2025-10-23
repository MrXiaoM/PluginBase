package top.mrxiaom.pluginbase.economy;

public enum EnumEconomy {
    /**
     * 默认选项，不使用经济插件
     */
    NONE,
    /**
     * 使用 Vault 挂钩的经济插件
     */
    VAULT,
    /**
     * 使用自定义经济插件，通过重写插件主类的 initCustomEconomy 来实现
     */
    CUSTOM
}
