package top.mrxiaom.pluginbase.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ActionActionBar implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[actionbar]")) {
            return new ActionActionBar(s.substring(11));
        }
        if (s.startsWith("actionbar:")) {
            return new ActionActionBar(s.substring(10));
        }
        return null;
    };
    public final String message;
    public ActionActionBar(String message) {
        this.message = message;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        if (player != null) {
            String s = Pair.replace(message, replacements);
            AdventureUtil.sendActionBar(player, PAPI.setPlaceholders(player, s));
        }
    }
}
