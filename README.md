# PluginBase

Minecraft 插件开发前置

## 简介

经过一年多 Minecraft 服务器插件的开发，我逐渐感觉到有一些步骤没有必要。  
于是我创建了插件模板，但这还不够，还是太麻烦了。  
这个库包含了我常用的工具类，以及惯用的设计结构。  
此外，我非常讨厌一个插件还要带上前置插件这种拖家带口行为，所以，这个前置是通过 shadow 打进插件 jar 里的。并且，以我设计的模块工作原理，必须要把这个依赖 shadow 打进 jar 并 relocate 到插件自己的包。

## 开始使用

详见 [MCIO Plugins 文档](https://plugins.mcio.dev/elopers/base/intro)。

插件模板生成器详见 https://bukkit.mcio.dev

## 正在使用 PluginBase 的插件

+ SweetRiceBase [闭源] NeoWorld 服务器基础玩法插件
+ SweetRiceTeam [闭源] NeoWorld 服务器副本组队插件
+ SweetRiceActivity [闭源] NeoWorld 服务器活动服机制
+ SweetGenerator [闭源] NeoWorld 服务器肉鸽地牢生成插件
+ SweetQuests [暂定闭源] NeoWorld 服务器剧情系统
+ [CraftItem](https://github.com/MrXiaoM/CraftItem) 物品锻造插件
+ [SweetWarehouse](https://github.com/MrXiaoM/SweetWarehouse) 云物品仓库插件
+ [SweetInventory](https://github.com/MrXiaoM/SweetInventory) 自定义菜单插件
+ [SweetAdaptiveShop](https://github.com/MrXiaoM/SweetAdaptiveShop) 动态价格商店插件
+ [SweetCheckout](https://github.com/MrXiaoM/SweetCheckout) 充值插件
+ [SweetRewards](https://github.com/MrXiaoM/SweetRewards) 累积点数插件
+ [SweetDrops](https://github.com/MrXiaoM/SweetDrops) 方块挖掘概率事件插件
+ [SweetFlight](https://github.com/MrXiaoM/SweetFlight) 限时飞行插件
+ [SweetMessages](https://github.com/MrXiaoM/SweetMessages) 基础消息插件
+ ...其它 [Sweet系列插件](https://github.com/stars/MrXiaoM/lists/%E3%82%B9%E3%82%A6%E3%82%A3%E3%83%BC%E3%83%88-minecraft-plugins)
