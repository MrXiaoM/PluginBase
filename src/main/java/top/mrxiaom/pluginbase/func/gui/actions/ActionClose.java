package top.mrxiaom.pluginbase.func.gui.actions;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.Pair;

public class ActionClose implements IAction {
    public static final IActionProvider PROVIDER;
    public static final ActionClose INSTANCE;
    static {
        INSTANCE = new ActionClose();
        PROVIDER = s -> s.equals("[close]") || s.equals("close") ? INSTANCE : null;
    }
    private ActionClose() {
    }

    @Override
    public void run(Player player, Pair<String, Object>[] replacements) {
        player.closeInventory();
    }
}
