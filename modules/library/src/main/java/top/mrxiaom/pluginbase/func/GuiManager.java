package top.mrxiaom.pluginbase.func;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.function.BiConsumer;

public class GuiManager extends AbstractPluginHolder<BukkitPlugin> implements Listener {
    BiConsumer<Player, IGuiHolder> disable = (player, gui) -> {
        try {
            player.sendTitle("§e请等等", "§f管理员正在更新插件", 10, 30, 10);
        } catch (Throwable ignored) {}
    };
    boolean disabled = false;
    public GuiManager(BukkitPlugin plugin) {
        super(plugin, true);
        registerEvents(this);
    }

    public void openGui(IGuiHolder gui) {
        if (disabled) return;
        Player player = gui.getPlayer();
        if (player == null) return;
        Inventory inv = gui.newInventory();
        if (inv != null) {
            if (Util.getHolder(inv) == gui) {
                player.openInventory(inv);
            } else {
                player.closeInventory();
                warn("试图为玩家 " + player.getName() + " 打开界面 " + gui.getClass().getName() + " 时，界面未设置 InventoryHolder 为自身实例");
            }
        } else if (!gui.allowNullInventory()) {
            warn("试图为玩家 " + player.getName() + " 打开界面 " + gui.getClass().getName() + " 时，程序返回了 null");
        }
    }

    public void onDisable() {
        disabled = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            IGuiHolder gui = getInventoryHolder(view.getTopInventory());
            if (gui != null) {
                gui.onClose(view);
                player.closeInventory();
                if (disable != null) disable.accept(player, gui);
            }
        }
    }

    public void setDisableAction(@Nullable BiConsumer<Player, IGuiHolder> consumer) {
        this.disable = consumer;
    }

    @Nullable
    public IGuiHolder getOpeningGui(Player player) {
        if (disabled) return null;
        return getInventoryHolder(player.getOpenInventory().getTopInventory());
    }

    public IGuiHolder getInventoryHolder(Inventory inv) {
        return getHolderAsGui(inv);
    }

    private IGuiHolder getHolderAsGui(Inventory inv) {
        InventoryHolder holder = Util.getHolder(inv);
        if (holder instanceof IGuiHolder) {
            return (IGuiHolder) holder;
        }
        return null;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (disabled) return;
        Player player = e.getPlayer();
        InventoryView view = player.getOpenInventory();
        IGuiHolder gui = getInventoryHolder(view.getTopInventory());
        if (gui != null) {
            gui.onClose(view);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (disabled || !(event.getWhoClicked() instanceof Player)) return;
        InventoryView view = event.getView();
        IGuiHolder gui = getInventoryHolder(view.getTopInventory());
        if (gui != null) {
            gui.onClick(
                    event.getAction(), event.getClick(),
                    event.getSlotType(), event.getRawSlot(),
                    event.getCurrentItem(), event.getCursor(),
                    event.getView(), event
            );
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (disabled || !(event.getWhoClicked() instanceof Player)) return;
        InventoryView view = event.getView();
        IGuiHolder gui = getInventoryHolder(view.getTopInventory());
        if (gui != null) {
            gui.onDrag(view, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (disabled || !(event.getPlayer() instanceof Player)) return;
        InventoryView view = event.getView();
        IGuiHolder gui = getInventoryHolder(view.getTopInventory());
        if (gui != null) {
            gui.onClose(view);
        }
    }

    public static GuiManager inst() {
        return instanceOf(GuiManager.class);
    }
}
