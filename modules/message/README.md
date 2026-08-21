## sparrow message

这是 [Xiao-MoMi](https://github.com/Xiao-MoMi) 的 sparrow message 的 fork，从原本的 Java 21 + Adventure 5.0 兼容到 Java 8 + Adventure 4.11.0，并与 PluginBase 接口进行兼容，添加更多便于使用的方法。

基线为 MiniMessage，保留原有许可证 MIT License。

实现注册自定义标签时，参考 [CraftEngineTags](https://github.com/Xiao-MoMi/craft-engine/blob/main/core/src/main/java/net/momirealms/craftengine/core/plugin/text/minimessage/CraftEngineTags.java) 相关代码以获得最佳性能。

> 将会逐步抛弃默认 MiniMessage 实现，目前框架内的实现仍是通用方法，有待优化。
