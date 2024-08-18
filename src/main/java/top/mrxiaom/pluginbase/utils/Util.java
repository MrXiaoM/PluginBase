package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unused"})
public class Util {
    public static Map<String, OfflinePlayer> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static Map<UUID, OfflinePlayer> playersByUUID = new TreeMap<>();

    public static void init(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.getName() != null) {
                    players.put(player.getName(), player);
                    playersByUUID.put(player.getUniqueId(), player);
                }
            }
        });
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent e) {
                Player player = e.getPlayer();
                players.put(player.getName(), player);
                playersByUUID.put(player.getUniqueId(), player);
            }
        }, plugin);
        PAPI.init();
    }

    public static Integer getInt(ConfigurationSection section, String key, Integer def) {
        return section.contains(key) ? Integer.valueOf(section.getInt(key)) : def;
    }

    public static Material getItem(ConfigurationSection section, String key, Material def) {
        if (section == null) return def;
        return valueOr(Material.class, section.getString(key), def);
    }

    @CanIgnoreReturnValue
    public static boolean createNewFile(File file) throws IOException {
        return file.createNewFile();
    }

    public static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }
        return sw.toString();
    }

    @SafeVarargs
    public static void runCommands(Player player, List<String> list, Pair<String, Object>... replacements) {
        for (String s : ColorHelper.parseColor(PlaceholderAPI.setPlaceholders(player, list))) {
            for (Pair<String, Object> pair : replacements) {
                s = s.replace(pair.getKey(), String.valueOf(pair.getValue()));
            }
            if (s.startsWith("[console]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.substring(9).trim());
            }
            if (s.startsWith("[player]")) {
                Bukkit.dispatchCommand(player, s.substring(8).trim());
            }
            if (s.startsWith("[message]")) {
                player.sendMessage(s.substring(9).trim());
            }
        }
    }

    public static List<String> startsWith(String s, String... texts) {
        return startsWith(s, Lists.newArrayList(texts));
    }

    public static List<String> startsWith(String s, Iterable<String> texts) {
        List<String> list = new ArrayList<>();
        s = s.toLowerCase();
        for (String text : texts) {
            if (text.toLowerCase().startsWith(s)) list.add(text);
        }
        return list;
    }

    public static Location toLocation(String world, String loc) {
        Location l = null;
        try {
            String[] s = loc.split(",");
            double x = Double.parseDouble(s[0]);
            double y = Double.parseDouble(s[1]);
            double z = Double.parseDouble(s[2]);
            float yaw = s.length > 3 ? Float.parseFloat(s[3]) : 0;
            float pitch = s.length > 4 ? Float.parseFloat(s[4]) : 0;
            l = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        } catch (Throwable ignored) {
        }
        return l;
    }

    public static String fromLocation(Location loc) {
        return String.format("%.2f,%.2f,%.2f,%.2f,%.2f", loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public static Optional<OfflinePlayer> getOfflinePlayer(String name) {
        return Optional.ofNullable(players.get(name));
    }

    public static Optional<OfflinePlayer> getOfflinePlayer(UUID uuid) {
        return Optional.ofNullable(playersByUUID.get(uuid));
    }

    public static Optional<Player> getOnlinePlayer(String name) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) return Optional.of(player);
        }
        return Optional.empty();
    }

    public static Optional<Player> getOnlinePlayer(UUID uuid) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(uuid)) return Optional.of(player);
        }
        return Optional.empty();
    }

    public static List<Player> getOnlinePlayersByUUID(List<UUID> uuidList) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (uuidList.contains(player.getUniqueId())) players.add(player);
        }
        return players;
    }

    public static List<Player> getOnlinePlayersByName(List<String> nameList) {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(nameList);
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (names.contains(player.getName())) players.add(player);
        }
        return players;
    }

    public static Optional<Double> parseDouble(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseInt(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLong(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static <T extends Enum<?>> T valueOr(Class<T> c, String s, T def) {
        if (s == null) return def;
        for (T t : c.getEnumConstants()) {
            if (t.name().equalsIgnoreCase(s)) return t;
        }
        return def;
    }

    public static <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        List<T> temp = new ArrayList<>();
        for (T item : list) {
            temp.add(item);
            if (temp.size() == size) {
                result.add(temp);
                temp = new ArrayList<>();
            }
        }
        if (!temp.isEmpty()) result.add(temp);
        return result;
    }

    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static void split(Pattern regex, String s, Consumer<RegexResult> consumer) {
        int index = 0;
        Matcher m = regex.matcher(s);
        while (m.find()) {
            int first = m.start();
            int last = m.end();
            if (first > index) {
                consumer.accept(new RegexResult(null, s.substring(index, first)));
            }
            consumer.accept(new RegexResult(m.toMatchResult(), s.substring(first, last)));
            index = last;
        }
        if (index < s.length()) {
            consumer.accept(new RegexResult(null, s.substring(index)));
        }
    }

    public static <T> List<T> split(Pattern regex, String s, Function<RegexResult, T> transform) {
        List<T> list = new ArrayList<>();
        int index = 0;
        Matcher m = regex.matcher(s);
        while (m.find()) {
            int first = m.start();
            int last = m.end();
            if (first > index) {
                T value = transform.apply(new RegexResult(null, s.substring(index, first)));
                if (value != null) list.add(value);
            }
            T value = transform.apply(new RegexResult(m.toMatchResult(), s.substring(first, last)));
            if (value != null) list.add(value);
            index = last;
        }
        if (index < s.length()) {
            T value = transform.apply(new RegexResult(null, s.substring(index)));
            if (value != null) list.add(value);
        }
        return list;
    }

    public static double between(double num, double min, double max) {
        if (num < min) num = min;
        if (num > max) num = max;
        return num;
    }

    public static Set<Class<?>> getClasses(ClassLoader loader, String packageName, List<String> ignorePackages) {
        Set<Class<?>> classes = new TreeSet<>(Comparator.comparing(Class::getName));
        try {
            String name = packageName.replace(".", "/");
            Enumeration<URL> urls = loader.getResources(name);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    findAnnotatedClassesInDirectory(new File(url.toURI()), packageName, ignorePackages, classes);
                } else if ("jar".equals(protocol)) {
                    findAnnotatedClassesInJar(url, packageName, ignorePackages, classes);
                }
            }
        } catch (Exception ignored) {
        }
        return classes;
    }

    private static void findAnnotatedClassesInDirectory(File directory, String packageName, List<String> ignorePackages, Set<Class<?>> classes) {
        File[] files = directory.listFiles(file -> ((file.isFile() && file.getName().endsWith(".class")) || file.isDirectory()));
        for (String ignorePackage : ignorePackages) {
            if (packageName.startsWith(ignorePackage)) return;
        }
        if (files != null) for (File file : files) {
            if (file.isFile()) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                }
            } else if (file.isDirectory()) {
                findAnnotatedClassesInDirectory(file, packageName + "." + file.getName(), ignorePackages, classes);
            }
        }
    }

    private static void findAnnotatedClassesInJar(URL url, String packageName, List<String> ignorePackages, Set<Class<?>> classes) throws Exception {
        String packagePath = packageName.replace(".", "/");
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(packagePath) && name.endsWith(".class")) {
                String className = name.substring(0, name.length() - 6).replace("/", ".");
                boolean flag = false;
                for (String ignorePackage : ignorePackages) {
                    if (packageName.startsWith(ignorePackage)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) continue;
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                }
            }
        }
    }


    public static class RegexResult {
        public final MatchResult result;
        public final boolean isMatched;
        public final String text;

        public RegexResult(MatchResult result, String text) {
            this.result = result;
            this.isMatched = result != null;
            this.text = text;
        }
    }
}
