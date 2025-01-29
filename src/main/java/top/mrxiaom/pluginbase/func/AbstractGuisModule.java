package top.mrxiaom.pluginbase.func;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.gui.IModel;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGui;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class AbstractGuisModule<T extends BukkitPlugin, M extends IModel> extends AbstractModule<T> {
    public interface IModelProvider<P extends AbstractGuisModule<?, M>, M extends IModel> {
        @Nullable M load(P parent, ConfigurationSection config, String id);
    }
    private final String warningPrefix;
    protected Map<String, M> menus = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public AbstractGuisModule(BukkitPlugin plugin, String warningPrefix) {
        super(plugin);
        this.warningPrefix = warningPrefix;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        menus.clear();
    }

    public <P extends AbstractGuisModule<T, M>> void loadConfig(P parent, File file, String id, IModelProvider<P, M> provider) {
        if (menus.containsKey(id)) {
            warn(warningPrefix + "重复菜单 " + id);
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        M loaded = provider.load(parent, config, id);
        if (loaded != null) {
            menus.put(id, loaded);
        }
    }

    @NotNull
    public Set<String> keys(Permissible p) {
        if (p.isOp()) return keys();
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, M> entry : menus.entrySet()) {
            if (entry.getValue().hasPermission(p)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    @NotNull
    public Set<String> keys() {
        return menus.keySet();
    }

    @Nullable
    public M get(String id) {
        return menus.get(id);
    }

    public abstract class Gui implements IGui, InventoryHolder {
        protected Player player;
        protected M model;
        protected String title;
        protected char[] inventory;
        protected Inventory created;
        protected Map<Character, LoadedIcon> otherIcons;
        protected boolean legacy;
        protected Gui(@NotNull Player player, @NotNull M model) {
            this.player = player;
            this.model = model;
            this.title = model.title();
            this.inventory = model.inventory();
            this.otherIcons = model.otherIcons();
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return created;
        }

        public void updateInventory(BiConsumer<Integer, ItemStack> setItem) {
            Map<Character, Integer> appearMap = new HashMap<>();
            for (int i = 0; i < inventory.length; i++) {
                char id = inventory[i];
                if (id == ' ' || id == '　') {
                    setItem.accept(i, null);
                    continue;
                }
                int appearTimes = appearMap.getOrDefault(id, 0) + 1;
                appearMap.put(id, appearTimes);
                ItemStack item = model.applyMainIcon(this, player, id, i, appearTimes);
                if (item != null) {
                    setItem.accept(i, item);
                    continue;
                }
                LoadedIcon icon = otherIcons.get(id);
                if (icon != null) {
                    setItem.accept(i, icon.generateIcon(player));
                    continue;
                }
                setItem.accept(i, null);
            }
        }

        public void updateInventory(Inventory inv) {
            updateInventory(inv::setItem);
        }

        public void updateInventory(InventoryView view) {
            updateInventory(view::setItem);
            player.updateInventory();
        }

        @Override
        public Inventory newInventory() {
            Inventory inv = create(inventory.length, title);
            updateInventory(inv);
            return inv;
        }

        protected Inventory create(int size, String title) {
            return (created = Bukkit.createInventory(this, size, title));
        }

        public Character getClickedId(int slot) {
            return AbstractGuiModule.getClickedId(inventory, slot);
        }

        public int getAppearTimes(Character id, int slot) {
            return AbstractGuiModule.getAppearTimes(inventory, id, slot);
        }

        public void handleOtherClick(ClickType type, int slot) {
            Character id = getClickedId(slot);
            if (id != null) {
                LoadedIcon icon = otherIcons.get(id);
                if (icon != null) {
                    icon.click(player, type);
                }
            }
        }
    }
}
