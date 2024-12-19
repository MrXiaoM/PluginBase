[<< 返回开发文档](README.md)

# 菜单配置文件

强大的用户可自定义菜单配置系统

## 新建配置

详见 [AbstractGuiModule](https://github.com/MrXiaoM/PluginBase/blob/main/src/main/java/top/mrxiaom/pluginbase/func/AbstractGuiModule.java)。

做得有点太庞大了，没空写更完整的示例，大体使用方法如下，其它的自行研究。

```java

@AutoRegister
public class GuiExample extends AbstractGuiModule {
    LoadedIcon testIcon;
    int testIconExtraConfig;
    public GuiExample(BukkitPlugin plugin) {
        super(plugin, new File(plugin.getDataFolder(), "example.yml")); // 界面配置文件
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!file.exists()) { // 配置文件不存在时保存
            plugin.saveResource("example.yml", file);
        }
        super.reloadConfig(cfg);
    }

    @Override
    protected String warningPrefix() {
        return "[example.yml]"; // 加载出错时日志显示的前缀
    }

    @Override
    protected void clearMainIcons() { // 主要图标清空配置
        testIcon = null;
        testIconExtraConfig = 0;
    }

    @Override
    protected void loadMainIcon(ConfigurationSection section, String id, LoadedIcon loadedIcon) {
        switch (id) { // 主要图标 (有特殊配置) 的加载
            case "测":
                testIcon = loadedIcon;
                testIconExtraConfig = section.getInt(id + ".extra", 0);
                break;
        }
    }

    @Override
    protected ItemStack applyMainIcon(Player player, char id, int index, int appearTimes) {
        switch (id) { // 主要图标添加到界面
            case '测':
                return testIcon.generateIcon(player);
        }
        return null;
    }

    public static GuiExample inst() {
        return instanceOf(GuiExample.class);
    }

    public static Impl create(Player player) {
        GuiExample self = inst();
        return self.new Impl(player, self.guiTitle, self.guiInventory);
    }

    public class Impl extends Gui {
        protected Impl(Player player, String title, char[] inventory) {
            super(player, title, inventory);
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType,
                            int slot, ItemStack currentItem, ItemStack cursor,
                            InventoryView view, InventoryClickEvent event) {
            Character id = getClickedId(slot);
            if (id != null) {
                if (id == '测') {
                    player.sendMessage("点击了测试按钮，extra=" + testIconExtraConfig);
                    // 要是你想，可以 test.click(player, click); 给主要图标处理点击动作
                }
                // 处理额外图标点击
                LoadedIcon icon = otherIcons.get(id);
                if (icon != null) {
                    icon.click(player, click);
                }
            }
        }
    }
}
```
打开界面很简单
```java
void foo() {
    GuiExample.create(player).open();
}
```
界面配置文件的自定义程度非常高
```yaml
title: '界面标题'
# 界面布局，一个字符代表一个图标，空格(全角半角都行)代表没有图标
# 最好以每一行9个字符的形式书写
# 全部字符数量一定要为9的倍数。
inventory:
  - '框框框框框框框框框'
  - '框　　　测　　　框'
  - '框框框框框框框框框'
# 主要图标，在上面代码中提到了
main-icons:
  测:
    material: STONE
    display: '测试图标'
    lore:
      - '描述'
# 额外图标
other-icons:
  框:
    material: WHITE_STAINED_GLASS_PANE
    display: '&f'
  # 以下为完整示例
  例:
    material: GRASS_BLOCK
    display: '物品名'
    lore:
    - '物品Lore'
    - '物品名和Lore都支持PAPI变量，'
    - '如果你在插件主类开启了 adventure 的话，还支持 mini message'
    # 开启 glow 会让物品出现附魔光泽
    glow: true
    # CustomModelData，不解释
    custom-model-data: 10001
    # 以下内容只能在「额外图标」中使用，除非你在 onClick 点击里面写了 testIcon.click(player, click) 来触发点击 
    # 左键点击执行操作，以下均支持使用 PAPI 变量
    left-click-commands:
    - '[console]后台执行'
    - '[player]玩家执行'
    - '[message]聊天提示'
    # 右键点击执行操作
    right-click-commands: []
    # Shift+左键点击执行操作
    shift-left-click-commands: []
    # Shift+右键点击执行操作
    shift-right-click-commands: []
    # 鼠标悬停按Q键执行操作
    drop-commands: []
```

## 图标 tag

如果你想给额外图标增加一些额外数据，可以通过 `LoadedIcon.registerTagProvider` 注册 tag 提供器，从菜单配置读取并赋值到图标的 `tag` 字段，你可以将这个 `tag` 字段当作 C# WinForm 控件的 `Tag` 属性来使用，仅用于存储开发者自定义的数据。

## 自定义点击操作

你可以通过 `AbstractGuiModule.registerActionProvider` 来给额外图标的`点击执行操作`功能增加更多你自己定义的类型，当前自带以下类型
+ [ActionActionBar](/src/main/java/top/mrxiaom/pluginbase/func/gui/actions/ActionActionBar.java) `[actionbar]物品栏上方提示（需要启用Adventure）`
+ [ActionConsole](/src/main/java/top/mrxiaom/pluginbase/func/gui/actions/ActionConsole.java) `[console]后台执行`
+ [ActionPlayer](/src/main/java/top/mrxiaom/pluginbase/func/gui/actions/ActionPlayer.java) `[player]玩家执行`
+ [ActionMessageAdventure](/src/main/java/top/mrxiaom/pluginbase/func/gui/actions/ActionMessageAdventure.java) `[message]聊天提示（需要启用Adventure）`
+ [ActionMessage](/src/main/java/top/mrxiaom/pluginbase/func/gui/actions/ActionMessage.java) `[message]聊天提示`
+ [ActionClose](/src/main/java/top/mrxiaom/pluginbase/func/gui/actions/ActionClose.java) `[close]` (关闭界面)

如果需要添加类型，请记得要通过 `PAPI.setPlaceholders(player, s)` 来处理 PlaceholderAPI 变量。
