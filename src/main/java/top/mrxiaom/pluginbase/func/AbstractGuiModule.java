package top.mrxiaom.pluginbase.func;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractGuiModule<T extends BukkitPlugin> extends AbstractModule<T> {
    protected final File file;
    protected String guiTitle;
    protected char[] guiInventory;
    protected final Map<Character, LoadedIcon> otherIcons = new HashMap<>();
    public AbstractGuiModule(BukkitPlugin plugin, File file) {
        super(plugin);
        this.file = file;
    }

    protected abstract String warningPrefix();

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        guiTitle = config.getString("title", "");
        guiInventory = getInventory(config, "inventory");
        clearMainIcons();
        otherIcons.clear();
        ConfigurationSection section = config.getConfigurationSection("main-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            LoadedIcon icon = LoadedIcon.load(section, key);
            loadMainIcon(section, key, icon);
        }
        section = config.getConfigurationSection("other-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            LoadedIcon icon = LoadedIcon.load(section, key);
            if (key.length() != 1) {
                warn(warningPrefix() + " 其它图标 " + key + " 的图标ID过长，请改成单个字符");
                continue;
            }
            otherIcons.put(key.charAt(0), icon);
        }
    }

    protected abstract void clearMainIcons();
    protected abstract void loadMainIcon(ConfigurationSection section, String id, LoadedIcon icon);
    protected abstract ItemStack applyMainIcon(char id, int index, int appearTimes);

    public static char[] getInventory(MemorySection config, String key) {
        return String.join("", config.getStringList(key)).toCharArray();
    }

    public abstract class Gui implements IGui {
        protected Player player;
        protected String title;
        protected char[] inventory;
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
                int appearTimes = appearMap.getOrDefault(id, 0) + 1;
                appearMap.put(id, appearTimes);
                ItemStack item = applyMainIcon(id, i, appearTimes);
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
            Inventory inv = Bukkit.createInventory(null, inventory.length, title);
            updateInventory(inv);
            return inv;
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