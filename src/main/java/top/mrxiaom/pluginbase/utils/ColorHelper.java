package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.BukkitPlugin;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static top.mrxiaom.pluginbase.utils.Util.split;

/**
 * 祖传的颜色代码处理器，更推荐使用 <code>AdventureUtil</code> 代替
 * @see AdventureUtil
 */
public class ColorHelper {
    private static final Pattern startWithColor = Pattern.compile("^(&[LMNKOlmnko])+");
    private static final Pattern gradientPattern = Pattern.compile("\\{(#[ABCDEFabcdef0123456789]{6}):(#[ABCDEFabcdef0123456789]{6}):(.*?)}");
    private static final Pattern hexPattern = Pattern.compile("&(#[ABCDEFabcdef0123456789]{6})");
    private static final Pattern translatePattern = Pattern.compile("<translate:(.*?)>");
    private static boolean old = false;

    /**
     * 替换消息中的颜色字符并发送给用户，如果插件启用了 adventure 选项，则会转而调用
     * <pre><code>AdventureUtil.sendMessage(sender, s);</code></pre>
     * @param sender 用户
     * @param s 要发送的消息
     */
    public static void parseAndSend(CommandSender sender, String s) {
        if (BukkitPlugin.getInstance().options.adventure()) {
            AdventureUtil.sendMessage(sender, s);
            return;
        }
        if (old && !(sender instanceof Player)) {
            sender.sendMessage(parseColor(s));
            return;
        }
        TextComponent builder = new TextComponent("");
        split(translatePattern, parseColor(s), regexResult -> {
            if (!regexResult.isMatched) {
                builder.addExtra(new TextComponent(regexResult.text));
            } else {
                TranslatableComponent translatable = new TranslatableComponent(regexResult.result.group(1));
                builder.addExtra(translatable);
            }
        });
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(builder);
            return;
        }
        try {
            sender.spigot().sendMessage(builder);
        } catch (NoSuchMethodError ignored) {
            old = true;
            sender.sendMessage(parseColor(s));
        }
    }

    /**
     * 替换十六进制颜色、渐变色为旧版样式代码 <code>§x</code>，以及替换 <code>&</code> 样式代码
     * @see ColorHelper#parseHexText(String)
     * @see ColorHelper#parseGradientText(String)
     */
    public static List<String> parseColor(List<String> s) {
        return Lists.newArrayList(parseColor(String.join("\n", s)).split("\n"));
    }

    /**
     * 替换十六进制颜色、渐变色为旧版样式代码 <code>§x</code>，以及替换 <code>&</code> 样式代码
     * @see ColorHelper#parseHexText(String)
     * @see ColorHelper#parseGradientText(String)
     */
    public static String parseColor(String s) {
        String fin = parseHexText(s);
        fin = parseGradientText(fin);
        return fin.replace("&", "§");
    }

    /**
     * 替换十六进制颜色为旧版样式代码 <code>§x</code>
     */
    public static String parseHexText(String s) {
        return String.join("", split(hexPattern, s, regexResult -> {
            if (!regexResult.isMatched) return regexResult.text;
            String hex = regexResult.text.substring(1);
            return parseHex(hex);
        }));
    }

    /**
     * 替换渐变色为旧版样式代码 <code>§x</code><br>
     * 格式为 <code>{#颜色1:#颜色2:渐变文字内容}</code>
     */
    public static String parseGradientText(String s) {
        return String.join("", split(gradientPattern, s, regexResult -> {
            if (!regexResult.isMatched) return regexResult.text;
            String[] args = regexResult.text.substring(1, regexResult.text.length() - 1).split(":", 3);
            String extra = "";
            Matcher m = startWithColor.matcher(args[2]);
            if (m.find()) {
                extra = ChatColor.translateAlternateColorCodes('&', m.group());
            }
            return parseGradient(m.replaceAll(""), extra, args[0], args[1]);
        }));
    }

    /**
     * 生成 Minecraft 1.16+ 渐变颜色文字
     *
     * @param s           字符串
     * @param extraFormat 额外样式
     * @param startHex    开始颜色 (#XXXXXX)
     * @param endHex      结束颜色 (#XXXXXX)
     * @return 渐变文字
     */
    public static String parseGradient(String s, String extraFormat, String startHex, String endHex) {
        s = s.replaceAll("[&§].", "");
        int color1 = hex(startHex);
        int color2 = hex(endHex);
        int[] colors = createGradient(color1, color2, s.length());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < colors.length; i++) {
            result.append(hexToMc(colors[i])).append(extraFormat).append(s.charAt(i));
        }
        return result.toString();
    }

    /**
     * 生成 Minecraft 1.16+ 16进制颜色代码
     *
     * @param hex 16进制颜色 (#XXXXXX)
     * @return 颜色代码
     */
    public static String parseHex(String hex) {
        StringBuilder result = new StringBuilder("§x");
        for (char c : hex.substring(1, hex.length() - 1).toLowerCase().toCharArray()) {
            result.append('§').append(c);
        }
        result.append("§F");
        return result.toString();
    }

    /**
     * 创建渐变色数组
     * @param startHex 起始颜色
     * @param endHex 终止颜色
     * @param step 行进步数，即色带分割份数
     * @return 颜色数组，每个 <code>int</code> 都是一个颜色，数组大小为 <code>step</code>
     */
    public static int[] createGradient(int startHex, int endHex, int step) {
        if (step == 1) return new int[]{startHex};

        int[] colors = new int[step];
        int[] start = hexToRGB(startHex);
        int[] end = hexToRGB(endHex);

        int stepR = (end[0] - start[0]) / (step - 1);
        int stepG = (end[1] - start[1]) / (step - 1);
        int stepB = (end[2] - start[2]) / (step - 1);

        for (int i = 0; i < step; i++) {
            colors[i] = rgbToHex(
                    start[0] + stepR * i,
                    start[1] + stepG * i,
                    start[2] + stepB * i
            );
        }
        return colors;
    }

    /**
     * 将整数转换为 Minecraft 1.16+ 的 16 进制颜色代码
     * @see ColorHelper#hex(int)
     * @see ColorHelper#parseHex(String)
     */
    public static String hexToMc(int hex) {
        return parseHex(hex(hex));
    }

    /**
     * 将 <code>#FFFFFF</code> 格式的字符串转换为整数
     */
    public static int hex(String hex) {
        return Integer.parseInt(hex.substring(1), 16);
    }

    /**
     * 将整数转换为 <code>#FFFFFF</code> 格式的字符串
     */
    public static String hex(int hex) {
        return "#" + String.format("%06x", hex);
    }

    /**
     * 将整数转换为 RGB 数组
     * <pre><code>
     * int r = arr[0];
     * int g = arr[1];
     * int b = arr[2];
     * </code></pre>
     */
    public static int[] hexToRGB(int hex) {
        return new int[]{
                (hex >> 16) & 0xff,
                (hex >> 8) & 0xff,
                hex & 0xff
        };
    }

    /**
     * 将 RGB 转换为整数，以便转为 <code>#FFFFFF</code> 格式的字符串
     * @see ColorHelper#hex(int)
     */
    public static int rgbToHex(int r, int g, int b) {
        return (r << 16) + (g << 8) + b;
    }
}
