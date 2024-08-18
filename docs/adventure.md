# Adventure

Adventure 是一个截止当前最好用、最广泛使用的 Minecraft 文本组件处理库，文档地址[在这里](https://docs.advntr.dev/getting-started.html)。

我们做了一点小小的封装，使其变得更方便使用。

# 添加到构建脚本
```kotlin
// ...
dependencies {
    // ...
    implementation("net.kyori:adventure-api:4.17.0") // 接口
    implementation("net.kyori:adventure-platform-bukkit:4.3.4") // bukkit 平台适配器
    implementation("net.kyori:adventure-text-minimessage:4.17.0") // mini message 支持
    // 以上三个缺一不可
    implementation("de.tr7zw:item-nbt-api:2.13.2") // 可选: 如果需要在物品名使用自定义文本组件，需要加 nbt api 依赖
}
// 通过 shadow 打进 jar 里
// 看个人喜好，在 plugin.yml 声明下载依赖也行
tasks {
    shadowJar {
        // ...
        mapOf(
            // ...
            "de.tr7zw.changeme.nbtapi" to "nbtapi", // 同上 可选
            "net.kyori" to "kyori", // 必选
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
}
// ...
```

# 在主类添加 Adventure 支持
```java
public class PluginMain extends BukkitPlugin {
    // 1. 在插件主类构造函数启用 adventure 支持
    public PluginMain() {
        super(options() // ...
                .adventure(true)
                // ...
        );
    }
}
```

# 完成

已经好了，你已经为你的插件启用 adventure 支持了，并且无需 Paper 服务端也可以使用。  
以下展示几个使用示例
```java
void foo() {
    Player player = Bukkit.getOnlinePlayers().iterator().next();
    // 以下均支持 mini message 用法，且过时方法 & 和 § 会被自动转换为 mini message 用法
    AdventureUtil.sendMessage(player, "<click:run_command:/spawn>点这里</click>返回主城");
    AdventureUtil.sendActionBar(player, "&e&l这是一条显示在物品栏上方的消息");
    // 淡入时间/持续时间/淡出时间 的单位是 ticks
    AdventureUtil.sendTitle(player, "<yellow>当心", "闪避判定成功", 10, 30, 10);
    
    // 以下示例需要添加 item-nbt-api 依赖才能支持
    ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
    AdventureItemStack.setItemDisplayName(item, "<aqua><b>示例剑");
    AdventureItemStack.setItemLore(item,
            "<gray>可以传入可变长度参数",
            "<gray>也可以传入字符串列表 List<String>",
            "<gray>可在物品中使用 mini message 有很多玩法",
            "<gray>比如显示原版物品名，像下面这样",
            // 注: getTranslationKey 是 org.bukkit.Translatable 的方法，
            // 而 Translatable 在 Bukkit 1.19.3 才加入。更低的版本使用这个方法
            // 会报错，想这么玩的话，最好找个更好的办法获取物品的翻译键。
            "<yellow><lang:" + item.getTranslationKey() + ">",
            "<gray>在这个示例中，上面那行会显示为黄色的 钻石剑",
            "<gray>并且会随玩家客户端语言变化而变化");

    ItemStack item2 = AdventureItemStack.buildItem(
            Material.IRON_SWORD,
            "<gray>铁剑",
            "<white>一把普通的铁剑");
}
```
