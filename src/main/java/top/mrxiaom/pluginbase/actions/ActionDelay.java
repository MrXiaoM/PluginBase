package top.mrxiaom.pluginbase.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.List;

public class ActionDelay implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[delay]")) {
            return Util.parseInt(s.substring(7)).map(ActionDelay::new).orElse(null);
        }
        if (s.startsWith("delay:")) {
            return Util.parseInt(s.substring(6)).map(ActionDelay::new).orElse(null);
        }
        return null;
    };
    public final long delay;
    public ActionDelay(long delay) {
        this.delay = delay;
    }

    @Override
    public long delayAfterRun() {
        return delay;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        // do nothing
    }
}
