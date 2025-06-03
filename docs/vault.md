[<< 返回开发文档](README.md)

# Vault 经济

要开启 Vault 经济支持，在主类构造函数中启用

```java
public class PluginMain extends BukkitPlugin {
    public PluginMain() {
        super(options() // ...
                .economy(EnumEconomy.VAULT)
                // ...
        );
    }
}
```

需要使用 Vault 接口获取/设置金币时，使用如下方法
```java
public class PluginMain extends BukkitPlugin {
    void foo() { // 使用示例
        IEconomy economy = options.economy(); // 未启用 economy 时为 null，请记得一定要启用
        OfflinePlayer player = Bukkit.getOfflinePlayer("LittleCatX");
        double money = economy.get(player);
        if (economy.has(player, 100.0)) {
            economy.takeMoney(player, 100.0);
        }
        economy.giveMoney(player, 50.0);
    }
}
```
