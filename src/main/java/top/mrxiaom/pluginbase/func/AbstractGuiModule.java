package top.mrxiaom.pluginbase.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.utils.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * 单个菜单配置的抽象模块
 */
public abstract class AbstractGuiModule<T extends BukkitPlugin> extends AbstractModule<T> {
    protected File file;
    protected String guiTitle;
    protected char[] guiInventory;
    private final String mainIconsKey, otherIconsKey;
    protected final Map<Character, LoadedIcon> otherIcons = new HashMap<>();
    public AbstractGuiModule(BukkitPlugin plugin, File file) {
        this(plugin, file, "main-icons", "other-icons");
    }
    public AbstractGuiModule(BukkitPlugin plugin, File file, @Nullable String mainIconsKey, @Nullable String otherIconsKey) {
        super(plugin);
        this.file = file;
        this.mainIconsKey = mainIconsKey;
        this.otherIconsKey = otherIconsKey;
    }

    protected abstract String warningPrefix();

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        YamlConfiguration config = Util.load(file);
        guiTitle = config.getString("title", "");
        guiInventory = getInventory(config, "inventory");
        reloadMenuConfig(config);
        if (mainIconsKey != null) {
            ConfigurationSection section = config.getConfigurationSection(mainIconsKey);
            if (section != null) for (String key : section.getKeys(false)) {
                LoadedIcon icon = LoadedIcon.load(section, key);
                loadMainIcon(section, key, icon);
            }
        }
        if (otherIconsKey != null) {
            otherIcons.clear();
            ConfigurationSection section = config.getConfigurationSection(otherIconsKey);
            if (section != null) for (String key : section.getKeys(false)) {
                LoadedIcon icon = LoadedIcon.load(section, key);
                if (key.length() != 1) {
                    warn(warningPrefix() + " 其它图标 " + key + " 的图标ID过长，请改成单个字符");
                    continue;
                }
                otherIcons.put(key.charAt(0), icon);
            }
        }
    }
    protected void reloadMenuConfig(YamlConfiguration config) {
    }
    protected abstract void loadMainIcon(ConfigurationSection section, String id, LoadedIcon icon);
    @Nullable
    protected ItemStack applyMainIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes) {
        return null;
    }
    @Nullable
    protected ItemStack applyMainIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes, AtomicBoolean ignore) {
        return applyMainIcon(instance, player, id, index, appearTimes);
    }
    @Nullable
    protected ItemStack applyOtherIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes, LoadedIcon icon) {
        return icon.generateIcon(player);
    }

    public static char[] getInventory(ConfigurationSection config, String key) {
        return String.join("", config.getStringList(key)).toCharArray();
    }

    public abstract class Gui implements IGuiHolder {
        protected Player player;
        protected String title;
        protected char[] inventory;
        protected Inventory created;
        protected Gui(Player player, String title, char[] inventory) {
            this.player = player;
            this.title = title;
            this.inventory = inventory;
        }

        @Override
        public Player getPlayer() {
            return player;
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
                AtomicBoolean ignore = new AtomicBoolean(false);
                ItemStack item = applyMainIcon(this, player, id, i, appearTimes, ignore);
                if (ignore.get()) continue;
                if (item != null) {
                    setItem.accept(i, item);
                    continue;
                }
                LoadedIcon icon = otherIcons.get(id);
                if (icon != null) {
                    setItem.accept(i, applyOtherIcon(this, player, id, i, appearTimes, icon));
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
            Util.submitInvUpdate(player);
        }

        @Override
        public Inventory newInventory() {
            this.created = create(inventory.length, title);
            updateInventory(created);
            return created;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return created;
        }

        protected Inventory create(int size, String title) {
            return plugin.createInventory(this, size, title);
        }

        public Character getClickedId(int slot) {
            return AbstractGuiModule.getClickedId(inventory, slot);
        }

        public int getAppearTimes(Character id, int slot) {
            return AbstractGuiModule.getAppearTimes(inventory, id, slot);
        }

        public void handleOtherClick(ClickType type, int slot) {
            handleOtherClick(type, getClickedId(slot));
        }

        public void handleOtherClick(ClickType type, Character id) {
            if (id != null) {
                LoadedIcon icon = otherIcons.get(id);
                if (icon != null) {
                    icon.click(player, type);
                }
            }
        }
    }

    @Nullable
    public static Character getClickedId(char[] inventory, int slot) {
        if (slot >= 0 && slot < inventory.length) {
            return inventory[slot];
        } else {
            return null;
        }
    }

    public static int getAppearTimes(char[] inventory, Character id, int slot) {
        int appearTimes = 0;
        for (int i = 0; i < inventory.length; i++) {
            if (id.equals(inventory[i])) {
                appearTimes++;
            }
            if (i == slot) break;
        }
        return appearTimes;
    }
}
