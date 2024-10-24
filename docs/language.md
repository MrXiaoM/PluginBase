[<< 返回开发文档](README.md)

# 本地化系统

尽可能方便地制作本地化配置。

## 新建枚举类

你可以将所有文本都堆在一个枚举类里，也可以不同类型的文本放到不同的枚举类。这里以不同类型文本放到不同枚举类举例。

```java
package top.mrxiaom.example.messages;

import com.google.common.collect.Lists;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import java.util.List;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

// 1. 可选参数，作为配置键的前缀。
//    这个注解可加可不加
@Language(prefix = "errors.")
public enum Errors {
    // 5. 爱加什么就加什么，枚举构造函数的参数是文本的默认值，
    // 在本地化配置文件中找不到相应文本时使用默认值。
    
    // 枚举名称将会作为配置键的一部分:
    // 双下划线 (__) 会替换成点 (.)
    // 单下划线 (_) 会替换成横杠 (-)
    ARGUMENTS__NOT_A_NUMBER("请输入一个数字"),
    ARGUMENTS__INTERNAL("出现了一个内部错误 (%s)，请联系服务器管理员"),
    PLAYER__NOT_ONLINE("玩家 %player% 不在线 (或不存在)"),
    LIST("可以用多个参数", "代表list"),
    LIST_2(Lists.newArrayList("用 List", "也是可以的"));
    
    // 2. 新建三个构造函数
    Errors(String defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Errors(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Errors(List<String> defaultValue) {
        holder = wrap(this, defaultValue);
    }
    // 4. 添加字段 holder 以及它的 getter
    private final LanguageEnumAutoHolder<Errors> holder;
    public LanguageEnumAutoHolder<Errors> holder() {
        return holder;
    }
}
```

用这个枚举类生成的默认本地化文件长这样

```yaml
errors:
  arguments:
    not-a-number: '请输入一个数字'
    internal: '出现了一个内部错误 (%s)，请联系服务器管理员'
  player:
    not-online: '玩家 %player% 不在线 (或不存在)'
  list:
    - '可以用多个参数'
    - '代表list'
  list-2:
    - '用 List'
    - '也是可以的'
```

## 配置本地化管理器

在主类 `beforeEnable` 进行配置。

```java
public class PluginMain extends BukkitPlugin {
    // ...

    @Override
    protected void beforeEnable() {
        getLanguageManager()
                // 1. 设置本地化配置文件路径
                .setLangFile("messages.yml")
                // 2. 注册枚举类到本地化管理器
                .register(Errors.class, Errors::holder);
    }
}
```

## 使用

通过枚举类中的 holder 来访问本地化文本值。  
如果你觉得访问要带个 `holder()` 不爽，给枚举类加个 ` implements IHolderAccessor` 即可。

```java
import top.mrxiaom.pluginbase.utils.Pair;

void foo() {
    // 直接取文本
    String s = Errors.ARGUMENTS__NOT_A_NUMBER.holder().str();
    // 如果默认类型是文本，会按换行符分隔，转换成 List
    List<String> list = Errors.ARGUMENTS__NOT_A_NUMBER.holder().list();
    // 直接取 List
    List<String> list1 = Errors.LIST.holder().list();
    // 如果默认类型是 List，会以换行符连接各元素，转换成文本
    String s1 = Errors.LIST.holder().str();
    // str 和 list 都可以加不定长度参数数组，用于 String.format 的参数，比如
    String s2 = Errors.ARGUMENTS__INTERNAL.str("错误原因");
    // str 和 list 还可以带不定长度的 Pair 数组，用于替换文本中的内容，比如
    String s3 = Errors.PLAYER__NOT_ONLINE.holder().str(Pair.of("%player%", "玩家名"));
    // 除此之外，还有 t 和 tm
    // t 是使用 ColorHelper 转换颜色字符后发送
    // tm 是使用 MiniMessage 格式发送
    // 两个方法均支持无额外参数，以及上述提到的 String.format、Pair替换
    // 两个方法返回值均为 true，可以非常优雅地用于 onCommand 执行命令结尾
    CommandSender sender = Bukkit.getConsoleSender();
    Errors.ARGUMENTS__NOT_A_NUMBER.holder().t(sender);
    Errors.ARGUMENTS__INTERNAL.holder().t(sender, "错误原因");
    Errors.PLAYER__NOT_ONLINE.holder().tm(sender, Pair.of("%player%", "玩家名"));
}
```
