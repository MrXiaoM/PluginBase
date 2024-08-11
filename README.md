# PluginBase

Minecraft 插件开发前置

## 简介

经过一年多 Minecraft 服务器插件的开发，我逐渐感觉到有一些步骤没有必要。  
于是我创建了插件模板，但这还不够，还是太麻烦了。  
这个库包含了我常用的工具类，以及惯用的设计结构。  
此外，我非常讨厌一个插件还要带上前置插件这种拖家带口行为，所以，这个前置是通过 shadow 打进插件 jar 里的。并且，以我设计的模块工作原理，必须要把这个依赖 shadow 打进 jar 并 relocate 到插件自己的包。

## 开始使用

详见 [文档](/docs)

编写插件主类如下

> 如果你要重写 `onEnable`，请务必在里面加 `super.onEnable();` 以防模块无法正常初始化。`onDisable`, `onLoad`, `reloadConfig` 同理。

```java
import top.mrxiaom.pluginbase.AbstractPluginMain;

public class PluginMain extends AbstractPluginMain {
    public PluginMain() {
        super(options() // 模块设置
            .bungee(false) // 是否接收 BungeeCord 消息
            .database(false) // 是否启用数据库模块
        );
    }
    @Override
    protected void afterLoad() { // beforeLoad 同理，相对于 onLoad 而言
        // TODO: 插件加载
    }
    @Override
    protected void afterEnable() { // beforeEnable 同理，相对于 onEnable 而言
        // TODO: 插件启用
    }
    @Override
    protected void beforeDisable() { // afterDisable 同理，相对于 onDisable 而言
        // TODO: 插件卸载
    }
}
```

