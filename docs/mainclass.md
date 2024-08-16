# 插件主类

这里示例的主类是 top.mrxiaom.example.PluginMain

```java
package top.mrxiaom.example;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mrxiaom.pluginbase.BukkitPlugin;

public class PluginMain extends BukkitPlugin {
    // 1. 首先，让主类继承 BukkitPlugin，加一个无参的构造函数，
    //    然后把插件的参数写到 super(); 里
    public PluginMain() {
        super(options() // 框架选项
                .bungee(false)
                .database(false)
                // 更改自动注册模块的包名，如果不填，则使用主类所在包名
                .scanPackage("top.mrxiaom.example")
        );
    }
    
    // 2. 添加 afterEnable()
    //    除此之外，还可以添加 beforeEnable afterEnable (相对于 onEnable 而言)
    //    beforeLoad afterLoad (相对于 onLoad 而言)
    //    beforeDisable afterDisable (相对于 onDisable 而言)
    //    而 onLoad onEnable onDisable 不推荐使用，请把它们留给 PluginBase 使用，
    //    加这六个方法也是为了避免开发者猪鼻忘记加 super.onEnable(); 之类的导致无法正常初始化
    @Override
    protected void afterEnable() {
        // 如果需要，在这里注册事件
        // 当然，更推荐通过注册 module 顺便注册事件，而不是在 onEnable 里注册事件，这样更好管理
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent e) {
                e.getPlayer().sendMessage("Hello world!");
            }
        }, this);

        getLogger().info("Example 插件加载完成!");
    }

    // 3. 注册模块 (如何新建模块请看下一部分)
    @Override
    protected void beforeEnable() {
        // 在 beforeEnable, beforeLoad, afterLoad 时可以注册模块
        // 注册的模块会统一在 beforeEnable 之后初始化
        // 在统一初始化之后，再进行注册模块操作与直接 new 没啥区别，不推荐在初始化后注册模块，不如直接 new
        
        registerModules(ExampleModule.class);
        
        // beforeLoad 之后，框架会扫描主类所在包下面的所有类，
        // 寻找带有 @AutoRegister 的 AbstractModule 并自动注册模块
        // 更推荐使用自动注册而非手动注册
    }
}
```

# 固定泛型

可以不固定，固定了泛型类型，可以方便之后继承和调用。

```java
// 自觉替换 PluginMain 为插件主类
package top.mrxiaom.example.func;

import top.mrxiaom.example.PluginMain;

public abstract class AbstractModule extends top.mrxiaom.pluginbase.func.AbstractModule<SweetRiceBase> {
    public AbstractModule(SweetRiceBase plugin) {
        super(plugin);
    }
}
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<PluginMain> {
    public AbstractPluginHolder(PluginMain plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(PluginMain plugin, boolean register) {
        super(plugin, register);
    }
}
```

# 新建模块

模块是便于开发者快速寻找自己写的功能在哪而设计的，它可以很好地分开与连接各个功能。

以下是主类示例中提到的 ExampleModule 示例

```java
package top.mrxiaom.example.modules;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.example.func.AbstractModule;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ColorHelper;

@AutoRegister // 自动注册
@AutoRegister(requirePlugins = {"AuthMe"}) // 自动注册 (并要求服务端已安装某些插件)
public class ExampleModule extends AbstractModule implements Listener {
    // 1. 首先，让模块类继承 AbstractModule，加一个 只有一个主类参数 的构造函数
    public ExampleModule(BukkitPlugin plugin) {
        super(plugin);
        // 如果想注册监听器，就写上这句
        registerEvents();
        // 如果想接收处理 BungeeCord 消息，就写上这句
        registerBungee();
        // 如果想注册命令，请看下一部分的示例
        // registerCommand("example", this);
    }

    private String message;
    
    // 2. 模块中可以写上 reloadConfig，在调用主类的 reloadConfig 时，
    //    这个方法会被调用，本方法的参数 config 即为读取 config.yml 的内容
    @Override
    public void reloadConfig(MemoryConfiguration config) {
        message = config.getString("example.message", "Hello World");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // 自带工具 ColorHelper，除了彩色字外，可以生成渐变字，并且在 1.16 以下不支持十六进制颜色的版本中显示为白色
        // 渐变色格式 {#FFFFFF:#000000:这是渐变文字正文，前面的是颜色}
        // 十六进制颜色格式 &#FFFFFF
        String colored = ColorHelper.parseColor(message);
        e.getPlayer().sendMessage(colored);
    }

    // 3. (可选) 加上这个方法，外面就可以通过 ExampleModule.inst()
    //    来获取 ExampleModule 的实例了
    public static ExampleModule inst() {
        return instanceOf(ExampleModule.class);
    }
}
```

# 注册命令

首先，在 `plugin.yml` 中添加命令，在上一篇文档中已说明

然后，添加一个模块，这里以 ExampleCommand 作为示例

```java
public class ExampleCommand extends AbstractModule implements CommandExecutor, TabCompleter {
    // 1. 首先，让模块类继承 AbstractModule，加一个 只有一个主类参数 的构造函数
    //    然后让模块类实现 CommandExecutor 和 TabCompleter (不想写Tab补全可以不加TabCompleter)
    public ExampleCommand(BukkitPlugin plugin) {
        super(plugin);
        // 2. 注册命令，后面就跟 Bukkit 接口一样了
        registerCommand("example", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("bing! bang! boom!");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
```

命令部分仍有很大的改进空间，计划会在之后优化出一套命令系统。
