[<< 返回开发文档](README.md)

# 一些工具

PluginBase 附带了一些好用的工具

## ColorHelper

用于替换彩色字、十六进制颜色、渐变色

用法为 `ColorHelper.parseColor("");`

## ItemStackUtil

物品管理器，可以做到
+ 序列化、反序列化物品
+ 快速构造一个物品
+ 以特定格式解析物品  
`itemsadder-物品名` 使用 ItemsAdder 物品，如 `itemsadder-example`  
`物品#CMD` 使用原版物品并添加 CustomModelData，如 `DIAMOND_SWORD#10001`
+ 快捷修改物品名、Lore
+ 快捷获取附魔书物品 (同时避免把附魔存错位置)
+ 快捷给予玩家物品，背包满时掉落到玩家附近

等

## PAPI

PlaceholderAPI 兼容接口，在服务器安装了 PlaceholderAPI 时正常替换变量，反之直接替换 `%player_name%` 为玩家名

## Util

大杂烩工具集合

+ 将 stackTrace 打印到字符串，跟 kotlin 用法类似
+ 快捷执行命令列表，支持 PAPI 变量  
`[console]` 开头为管理员命令  
`[player]` 开头为玩家命令  
`[message]` 开头为聊天框输出消息
+ Location 的快捷序列化与反序列化，格式为 `x,y,z,yaw,pitch`，保留两位小数
+ 提前储存玩家列表，快捷高效获取 OfflinePlayer (离线玩家) 与 Player (在线玩家) 实例，避免同步频繁获取离线玩家时卡服
+ 字符串反序列化为 `Optional<数字类型>` (`double`, `float`, `long`, `int`)
+ 字符串反序列化为枚举 带默认值 (`valueOr(Material.class, "PAPER", null)` 为读取 `Material.PAPER` (不分大小写)，读不到就返回默认值 `null`)
+ 列表分割 `chunk(List<T>, int)`，将一个列表分割成多个长度一样的子列表，与 kotlin 用法类似，最后一个列表可能长度不够
+ 检查类是否存在 `isPresent(String)`
+ 快捷正则表达式分割字符串 `split(Pattern, String, Consumer<RegexResult>)`，在 ColorHelper 中有使用示例

等
