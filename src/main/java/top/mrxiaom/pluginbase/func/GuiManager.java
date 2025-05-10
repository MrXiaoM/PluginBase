package top.mrxiaom.pluginbase.func;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.gui.IGui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class GuiManager extends AbstractPluginHolder<BukkitPlugin> implements Listener {
    final Map<UUID, IGui> playersGui = new HashMap<>();
    BiConsumer<Player, IGui> disable = (player, gui) -> {
        try {
            player.sendTitle("§e请等等", "§f管理员正在更新插件", 10, 30, 10);
        } catch (Throwable ignored) {}
    };
    boolean disabled = false;
    public GuiManager(BukkitPlugin plugin) {
        super(plugin, true);
        registerEvents(this);
    }

    public void openGui(IGui gui) {
        if (disabled) return;
        Player player = gui.getPlayer();
        if (player == null) return;
        player.closeInventory();
        playersGui.put(player.getUniqueId(), gui);
        Inventory inv = gui.newInventory();
        if (inv != null) {
            player.openInventory(inv);
        } else if (!gui.allowNullInventory()) {
            warn("试图为玩家 " + player.getName() + " 打开界面 " + gui.getClass().getName() + " 时，程序返回了 null");
        }
    }

    public void onDisable() {
        disabled = true;
        List<Map.Entry<UUID, IGui>> entries = Lists.newArrayList(playersGui.entrySet());
        for (Map.Entry<UUID, IGui> entry : entries) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            entry.getValue().onClose(player.getOpenInventory());
            playersGui.remove(entry.getKey());
            player.closeInventory();
            if (disable != null) disable.accept(player, entry.getValue());
        }
    }

    public void setDisableAction(@Nullable BiConsumer<Player, IGui> consumer) {
        this.disable = consumer;
    }

    @Nullable
    public IGui getOpeningGui(Player player) {
        if (disabled) return null;
        return playersGui.get(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (disabled) return;
        Player player = e.getPlayer();
        IGui remove = playersGui.remove(player.getUniqueId());
        if (remove != null) {
            remove.onClose(player.getOpenInventory());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (disabled || !(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (playersGui.containsKey(player.getUniqueId())) {
            playersGui.get(player.getUniqueId()).onClick(event.getAction(), event.getClick(), event.getSlotType(),
                    event.getRawSlot(), event.getCurrentItem(), event.getCursor(), event.getView(), event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (disabled || !(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (playersGui.containsKey(player.getUniqueId())) {
            playersGui.get(player.getUniqueId()).onDrag(event.getView(), event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (disabled || !(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        IGui remove = playersGui.remove(player.getUniqueId());
        if (remove != null) {
            remove.onClose(event.getView());
        }
    }

    public static GuiManager inst() {
        return instanceOf(GuiManager.class);
    }
}
