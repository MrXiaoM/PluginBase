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

暂未设计合适的接口，敬请期待。以现在的接口也能做，麻烦一点而已。
