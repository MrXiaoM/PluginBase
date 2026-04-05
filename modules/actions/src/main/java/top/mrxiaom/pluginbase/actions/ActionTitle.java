package top.mrxiaom.pluginbase.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.depend.PAPI;

import java.util.List;

public class ActionTitle implements IAction {
    public static final IActionProvider PROVIDER = input -> {
        if (input instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) input;
            if ("title".equals(section.getString("type"))) {
                String title = section.getString("title", "");
                String subtitle = section.getString("subtitle", "");
                int fadeIn = section.getInt("fade-in", 10);
                int stay = section.getInt("stay", 60);
                int fadeOut = section.getInt("fade-out", 10);
                return new ActionTitle(title, subtitle, fadeIn, stay, fadeOut);
            }
        } else {
            String s = String.valueOf(input);
            if (s.startsWith("[title]")) {
                String str = s.substring(7);
                String title, subtitle;
                int i = str.indexOf("\\n");
                if (i != -1) {
                    title = str.substring(0, i);
                    subtitle = str.substring(i + 2);
                } else {
                    title = "";
                    subtitle = str;
                }
                return new ActionTitle(title, subtitle, 10, 60, 10);
            }
        }
        return null;
    };
    private final String title;
    private final String subtitle;
    private final int fadeIn, stay, fadeOut;
    public ActionTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        if (player != null) {
            String title = PAPI.setPlaceholders(player, Pair.replace(this.title, replacements));
            String subtitle = PAPI.setPlaceholders(player, Pair.replace(this.subtitle, replacements));
            player.sendTitle(ColorHelper.parseColor(title), ColorHelper.parseColor(subtitle), fadeIn, stay, fadeOut);
        }
    }
}
