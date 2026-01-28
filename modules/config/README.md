## config

这个模块的基础代码来自 spigot 源码的一部分，基于 `1.20.4` 版本进行修改，原始代码位于 `org.bukkit.configuration`。

计划最低兼容到 `Spigot 1.8`。

这个模块对配置读写系统进行了一些便于开发的修改，并统一插件配置读写在各个平台的工作逻辑。
+ 支持且默认以 UTF-8 编码读写文件
+ 添加 `getSectionList` 用于代替 `getMapList`
+ 序列化系统依然使用 Bukkit 原有接口
+ ...等等更多变更
