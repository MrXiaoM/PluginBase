package top.mrxiaom.pluginbase.func.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class PageGuide<T> {
    List<Pair<T, ItemStack>> contents = new ArrayList<>();
    List<Integer> slots = new ArrayList<>();
    List<Integer> prevPageSlots = new ArrayList<>();
    List<Integer> nextPageSlots = new ArrayList<>();
    ItemStack btnPrevPage, btnPrevPageCannot, btnNextPage, btnNextPageCannot;
    int maxPage;
    int page = 1;
    Transformer<T> itemTransformer = null;

    @FunctionalInterface
    public interface Transformer<T> {
        /**
         * 物品修改器
         * @param data 数据
         * @param item 原物品
         * @param slot 格子
         * @param contentIndex 在全部内容中的索引
         * @param pageIndex 在当前页中的索引
         * @param pageGuide 翻页导航实例
         * @return 修改后的物品
         */
        @Nullable
        ItemStack apply(T data, @Nullable ItemStack item, int slot, int contentIndex, int pageIndex, PageGuide<T> pageGuide);
    }

    /**
     * 根据格子索引获取图标附带数据
     * @return 图标附带数据。点击位置不属于页面内容，或者该处没有图标时返回 null
     */
    @Nullable
    public T get(int slot) {
        int index = slots.indexOf(slot);
        if (index < 0) return null;
        int i = getStartIndex() + index;
        if (i < 0 || i > contents.size()) return null;
        return contents.get(i).getKey();
    }

    /**
     * 更新物品栏界面
     */
    public void updateInventory(Inventory inv) {
        updateInventory(inv::setItem);
    }

    /**
     * 更新物品栏界面
     */
    public void updateInventory(InventoryView view) {
        updateInventory(view::setItem);
        HumanEntity player = view.getPlayer();
        if (player instanceof Player) {
            // 下一 tick 再发送玩家背包更新
            Bukkit.getScheduler().runTaskLater(BukkitPlugin.getInstance(), ((Player) player)::updateInventory, 1L);
        }
    }

    /**
     * 更新物品栏界面
     */
    public void updateInventory(BiConsumer<Integer, ItemStack> setItem) {
        for (int slot : prevPageSlots) {
            setItem.accept(slot, hasPrevPage() ? btnPrevPage : btnPrevPageCannot);
        }
        for (int slot : nextPageSlots) {
            setItem.accept(slot, hasNextPage() ? btnNextPage : btnNextPageCannot);
        }
        int startIndex = getStartIndex();
        for (int i = 0; i < slots.size(); i++) {
            int index = startIndex + i;
            int slot = slots.get(i);
            if (index < contents.size()) {
                Pair<T, ItemStack> pair = contents.get(index);
                ItemStack item = itemTransformer != null
                        ? itemTransformer.apply(pair.getKey(), pair.getValue(), slot, index, i, this)
                        : pair.getValue();
                setItem.accept(slot, item);
            } else {
                setItem.accept(slot, null);
            }
        }
    }

    /**
     * 处理翻页按钮点击
     * @return 返回 true 代表已处理
     */
    public boolean handlePageBtnClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slots.contains(slot)) return false;
        if (prevPageSlots.contains(slot) && hasPrevPage()) {
            event.setCancelled(true);
            prevPage();
            updateInventory(event.getView());
            return true;
        }
        if (nextPageSlots.contains(slot) && hasNextPage()) {
            event.setCancelled(true);
            nextPage();
            updateInventory(event.getView());
            return true;
        }
        return false;
    }

    /**
     * 设置页面内容显示物品转换器
     */
    public void setItemTransformer(Transformer<T> itemTransformer) {
        this.itemTransformer = itemTransformer;
    }

    /**
     * 获取页面内容起始索引
     */
    public int getStartIndex() {
        return (page - 1) * getPerPageSize();
    }

    /**
     * 获取每一页最多能放多少个图标
     */
    public int getPerPageSize() {
        return slots.size();
    }

    /**
     * 获取最大页数
     */
    public int getMaxPage() {
        return maxPage;
    }

    /**
     * 获取当前页数 (从1开始)
     */
    public int getPage() {
        return page;
    }

    /**
     * 重新计算最大页数。通常这不需要手动调用，进行 add, remove 等等操作之后会执行这个方法。
     */
    public void reCalcMaxPage() {
        if (slots.isEmpty() || contents.isEmpty()) {
            this.maxPage = 1;
        } else {
            this.maxPage = (int) Math.ceil((double) contents.size() / (double) slots.size());
        }
    }

    /**
     * 是否可以向下翻页
     */
    public boolean hasNextPage() {
        return getPage() < getMaxPage();
    }

    /**
     * 向下翻页，但不会更新页面，请在这之后执行 updateInventory
     * @see PageGuide#updateInventory
     */
    public void nextPage() {
        if (hasNextPage()) page++;
    }

    /**
     * 是否可以向上翻页
     */
    public boolean hasPrevPage() {
        return getPage() > 1;
    }

    /**
     * 向上翻页，但不会更新页面，请在这之后执行 updateInventory
     * @see PageGuide#updateInventory
     */
    public void prevPage() {
        if (hasPrevPage()) page--;
    }

    /**
     * @see PageGuide#setContentSlots(List)
     */
    public void setContentSlots(int... slots) {
        List<Integer> list = new ArrayList<>();
        for (int slot : slots) {
            list.add(slot);
        }
        setContentSlots(list);
    }

    /**
     * 设置物品栏中哪些格子放置页面内容
     */
    public void setContentSlots(List<Integer> slots) {
        this.slots = slots;
        this.reCalcMaxPage();
    }

    /**
     * 添加图标到页面内容
     * @param data 图标附带数据
     * @param display 图标显示物品
     */
    public void add(T data, ItemStack display) {
        contents.add(Pair.of(data, display));
        this.reCalcMaxPage();
    }

    /**
     * 页面中是否有附带特定数据的图标
     */
    public boolean contains(T data) {
        for (Pair<T, ItemStack> content : contents) {
            if (content.getKey().equals(data)) return true;
        }
        return false;
    }

    /**
     * 移除页面中有附带特定数据的图标
     */
    public void remove(T data) {
        contents.removeIf(it -> it.getKey().equals(data));
        this.reCalcMaxPage();
    }

    /**
     * 清空页面图标
     */
    public void clear() {
        contents.clear();
        this.reCalcMaxPage();
    }

    /**
     * 获取页面所有内容
     */
    public List<Pair<T, ItemStack>> getContents() {
        return Collections.unmodifiableList(contents);
    }

    /**
     * 设置向上翻页按钮
     * @param prevPage 可以翻页时显示的图标
     * @param cannot 不能翻页时显示的图标
     * @param slots 图标显示在哪些格子里
     */
    public void setupPrevPageButton(ItemStack prevPage, ItemStack cannot, int... slots) {
        this.btnPrevPage = prevPage;
        this.btnPrevPageCannot = cannot;
        prevPageSlots.clear();
        for (int slot : slots) {
            prevPageSlots.add(slot);
        }
    }

    /**
     * 设置向下翻页按钮
     * @param nextPage 可以翻页时显示的图标
     * @param cannot 不能翻页时显示的图标
     * @param slots 图标显示在哪些格子里
     */
    public void setupNextPageButton(ItemStack nextPage, ItemStack cannot, int... slots) {
        this.btnNextPage = nextPage;
        this.btnNextPageCannot = cannot;
        nextPageSlots.clear();
        for (int slot : slots) {
            nextPageSlots.add(slot);
        }
    }
}
