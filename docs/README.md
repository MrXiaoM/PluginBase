# 开发文档

通过这篇文档，来上手使用 PluginBase 进行 Minecraft 插件开发。

请按顺序阅读并操作。

1. 构建脚本 [build.gradle.kts](/docs/buildscript.md)
2. 插件元数据 [plugin.yml](/docs/plugin.yml.md)
3. 插件主类 [PluginMain.java](/docs/mainclass.md)
4. 一些工具 [ColorHelper, ItemStackUtil, Util 等等](/docs/utils.md)
5. (可选) 菜单 [GuiManager](/docs/gui.md)
6. (可选) 菜单配置文件 [AbstractGuiModule](/docs/gui.config.md)
7. (可选) Vault 经济 [IEconomy](/docs/vault.md)
8. (可选) HikariCP 数据库连接池 [IDatabase](/docs/database.md)
9. (可选) Adventure 在非 Paper 服务端的富文本支持 [AdventureUtil](/docs/adventure.md)
10. (可选) 本地化（语言文件）系统 [LanguageManager](/docs/language.md)

## shadow 去除不必要部分

适当地给插件减重，去除当前插件不需要的功能  
(部分功能仅能在 1.0.8 版本后去除，否则会出现一些问题)
```kotlin
tasks {
    // ...
    shadowJar {
        // ...
        // 避免匿名类没删除，最好加通配符*来匹配
        listOf(
            // 界面配置
            "top/mrxiaom/pluginbase/func/AbstractGui*",
            "top/mrxiaom/pluginbase/func/gui/*",
            // PAPI兼容 (注意: 界面配置依赖这个)
            "top/mrxiaom/pluginbase/utils/PAPI*",
            // ItemsAdder 支持
            "top/mrxiaom/pluginbase/utils/IA*",
            // 物品操作相关支持
            "top/mrxiaom/pluginbase/utils/ItemStackUtil*",
            // 界面菜单管理器
            "top/mrxiaom/pluginbase/func/GuiManager*",
            "top/mrxiaom/pluginbase/gui/*",
            // 多语言支持
            "top/mrxiaom/pluginbase/func/LanguageManager*",
            "top/mrxiaom/pluginbase/func/language/*",
            // Adventure (消息和物品) 支持
            "top/mrxiaom/pluginbase/utils/Adventure*",
            // BungeeCord 消息通道 ByteArrayDataOutput 消警告
            "top/mrxiaom/pluginbase/utils/Bytes*",
        ).forEach(this::exclude)
    }
    // ...
}
```
## shadow 部分 relocate

我在做 paper 与 spigot 兼容的时候发现，因为我要把 adventure 打包到插件里并 relocate 到我的包，那么如果我要调用 paper 的 `Bukkit.createInventory` 来在标题使用 adventure，那么就会因为包被 relocate 了而报错。

我的解决方法是，把 paper 的操作放到了一个单独的类 `PaperInventoryFactory`，里面有一个 `create(InventoryHolder, int, String)`，参数 String 是标题。  
执行之后，会调用 mini message 转换为 Component 对象，传给 paper 的 `Bukkit.createInventory`。只要修改 shadowJar 插件让这个 `PaperInventoryFactory` 类不会被 relocate 即可。  
我已经把 shadowJar 改好了，直接用就行：
```kotlin
// settings.gradle.kts
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("top.mrxiaom:shadow:7.1.3")
    }
}
```
shadow 7.1.2 的工具链有点老了，发布不到 Gradle Plugin Portal，升级工具链又不支持 gradle 7.x，干脆就发 Maven Central 了。反正这样添加起来也不麻烦，多几行代码不碍事，能解决问题就行。也测试了支持在 Gradle 8.5 跑，不求在太高的 Gradle 版本能用，目前能兼容 Java 21 就行。
```kotlin
// build.gradle.kts
plugins {
    // ...
    id("top.mrxiaom.shadow")
}
// ...
tasks {
    // ...
    shadowJar {
        // ...
        ignoreRelocations("org/example/plugin/utils/PaperInventoryFactory.class")
    }
}
```

我的需求比较简单，所以现用 String 通过 mini message 转 Component。如果复杂一点，可以用 `GsonComponentSerializer` 作桥梁，沟通打包到你插件里的 adventure 和 paper 的 adventure。

顺带一提，可以加 ProtocolLib、packetevents 之类的依赖来解决 spigot 不支持在容器标题使用 adventure 的问题。不过，要加依赖插件的东西就不写到这个框架里了。如有需要，请参见 [SweetMail 的实现](https://github.com/MrXiaoM/SweetMail/blob/58a6ae0c0db1cde1fa9751cd911f0f963b72e3cf/src/main/java/top/mrxiaom/sweetmail/depend/protocollib/PLComponentTitle.java)。
