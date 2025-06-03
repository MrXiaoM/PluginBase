[<< 返回开发文档](README.md)

# 菜单

可以应对多种复杂场景的箱子菜单组件。

## 新建菜单

以 GuiExample 为例

```java
public class GuiExample implements IGui {
    Player player;
    public GuiExample(Player player) {
        this.player = player;
    }
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, 9, "示例菜单");
        inv.setItem(4, ItemStackUtil.buildItem(
                Material.DIAMOND,
                "&e你好",
                "&7点我领取一组钻石"
        ));
        return null;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        if (slot == 4) {
            if (!click.isShiftClick() && click.isLeftClick()) {
                ItemStackUtil.giveItemToPlayer(player, new ItemStack(Material.DIAMOND, 64));
                player.closeInventory();
            }
        }
    }
}
```

只需要执行 `new GuiExample(player).open();` 即可为玩家打开菜单。

## 动态更新图标

Bukkit 自带的还是有点难用，暂未设计合适的接口，敬请期待。

## 翻页菜单

详见 [PageGuide](https://github.com/MrXiaoM/PluginBase/blob/main/src/main/java/top/mrxiaom/pluginbase/func/gui/PageGuide.java)

```java
public class GuiExample implements IGui {
    Player player;
    PageGuide<String> pageGuide = new PageGuide<>();
    public GuiExample(Player player) {
        this.player = player;
    }
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, 9, "示例菜单");

        pageGuide.setupPrevPageButton(
                ItemStackUtil.buildItem(Material.LIME_STAINED_GLASS_PANE, "&a上一页"),
                ItemStackUtil.buildItem(Material.RED_STAINED_GLASS_PANE, "&c上一页", "&7没有上一页了"),
                0); // “上一页”在第 1 格
        pageGuide.setupNextPageButton(
                ItemStackUtil.buildItem(Material.LIME_STAINED_GLASS_PANE, "&a下一页"),
                ItemStackUtil.buildItem(Material.RED_STAINED_GLASS_PANE, "&c下一页", "&7没有下一页了"),
                3); // “下一页”在第 4 格
        pageGuide.setContentSlots(1, 2); // 内容在 2、3 格
        // 添加数据和显示物品
        pageGuide.add("钻石", new ItemStack(Material.DIAMOND));
        pageGuide.add("金锭", new ItemStack(Material.GOLD_INGOT));
        pageGuide.add("铁锭", new ItemStack(Material.IRON_INGOT));
        pageGuide.add("煤炭", new ItemStack(Material.COAL));
        pageGuide.add("石头", new ItemStack(Material.STONE));
        // 更新到 Inventory
        pageGuide.updateInventory(inv);
        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        // 处理翻页按钮点击
        if (pageGuide.handlePageBtnClick(event)) return;
        event.setCancelled(true);
        // 获取点击的图标携带的数据 (前面调用 add 那里的第一个参数)
        String data = pageGuide.get(slot);
        if (data != null) {
            player.sendMessage("点击的图标所携带的自定义数据: " + data);
        }
    }
}
```
