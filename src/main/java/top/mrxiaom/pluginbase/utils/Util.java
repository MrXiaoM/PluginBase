package top.mrxiaom.pluginbase.utils;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

@SuppressWarnings({"unused"})
public class Util {
    public static Map<String, OfflinePlayer> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static Map<UUID, OfflinePlayer> playersByUUID = new TreeMap<>();

    public static void init(BukkitPlugin plugin) {
        plugin.getScheduler().runTaskAsync(() -> {
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
        try {
            PAPI.init();
        } catch (Throwable ignored) {
        }
        try {
            SkullsUtil.init();
        } catch (Throwable ignored) {
        }
        try {
            RegistryConverter.init();
        } catch (Throwable ignored) {
        }
        if (plugin.options.adventure()) {
            AdventureUtil.init(plugin);
        }
    }

    public static void reloadFolder(File folder, boolean suffix, BiConsumer<String, File> reloadConfig) {
        reloadFolder(folder, null, suffix, reloadConfig);
    }

    private static void reloadFolder(File root, File folder, boolean suffix, BiConsumer<String, File> reloadConfig) {
        File[] files = (folder == null ? root : folder).listFiles();
        if (files != null) for (File file : files) {
            if (file.isDirectory()) {
                if (!new File(file, ".ignore").exists()) {
                    reloadFolder(root, file, suffix, reloadConfig);
                }
                continue;
            }
            String id = getRelationPath(root, file, suffix);
            reloadConfig.accept(id, file);
        }
    }

    public static String getRelationPath(File parent, File file, boolean suffix) {
        String parentPath = parent.getAbsolutePath();
        String path = file.getAbsolutePath();
        if (!path.startsWith(parentPath)) return suffix ? path : nameWithoutSuffix(path);
        String s = path.substring(parentPath.length()).replace("\\", "/");
        String relation = s.startsWith("/") ? s.substring(1) : s;
        return suffix ? relation : nameWithoutSuffix(relation);
    }

    public static String nameWithoutSuffix(String s) {
        int index = s.lastIndexOf('.');
        return index <= 0 ? s : s.substring(0, index);
    }

    @NotNull
    public static List<ConfigurationSection> getSectionList(ConfigurationSection parent, String key) {
        List<ConfigurationSection> list = new ArrayList<>();
        List<Map<?, ?>> rawList = parent.getMapList(key);
        for (Map<?, ?> map : rawList) {
            MemoryConfiguration section = new MemoryConfiguration();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String sectionKey = entry.getKey().toString();
                section.set(sectionKey, processValue(section, sectionKey, entry.getValue()));
            }
            list.add(section);
        }
        return list;
    }

    private static Object processValue(ConfigurationSection parent, String key, Object value) {
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            ConfigurationSection section;
            if (parent == null || key == null) { // 兼容 List
                section = new MemoryConfiguration();
            } else { // 兼容 Map
                section = parent.createSection(key);
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String mapKey = entry.getKey().toString();
                section.set(mapKey, processValue(section, mapKey, entry.getValue()));
            }
            return section;
        }
        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            List<Object> result = new ArrayList<>();
            for (Object object : list) {
                result.add(processValue(null, null, object));
            }
            return result;
        }
        return value;
    }

    public static Double getPercentAsDouble(ConfigurationSection section, String key, Double def) {
        return getPercentAsDouble(section.getString(key, null), def);
    }

    public static Float getPercentAsFloat(ConfigurationSection section, String key, Float def) {
        return getPercentAsFloat(section.getString(key, null), def);
    }

    public static Double getPercentAsDouble(String s, Double def) {
        if (s == null) return def;
        try {
            if (s.endsWith("%")) {
                String str = s.substring(0, s.length() - 1);
                double value = Double.parseDouble(str);
                return value / 100.0;
            } else {
                return Double.parseDouble(s);
            }
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static Float getPercentAsFloat(String s, Float def) {
        if (s == null) return def;
        try {
            if (s.endsWith("%")) {
                String str = s.substring(0, s.length() - 1);
                float value = Float.parseFloat(str);
                return value / 100.0f;
            } else {
                return Float.parseFloat(s);
            }
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static Double getDouble(ConfigurationSection section, String key, Double def) {
        return section.contains(key) && section.isDouble(key) ? Double.valueOf(section.getDouble(key)) : def;
    }

    public static Integer getInt(ConfigurationSection section, String key, Integer def) {
        return section.contains(key) && section.isInt(key) ? Integer.valueOf(section.getInt(key)) : def;
    }

    public static Material getItem(ConfigurationSection section, String key, Material def) {
        if (section == null) return def;
        return valueOr(Material.class, section.getString(key), def);
    }

    public static List<String> split(String string, String spliter) {
        return split(string, spliter, 0);
    }

    public static List<String> split(String string, String spliter, int limit) {
        int oldIndex = 0;
        int length = spliter.length();
        if (length == 0) throw new IllegalArgumentException("spliter can't be empty!");
        int count = limit - 1;
        boolean unlimited = limit <= 0;
        List<String> list = new ArrayList<>();
        while (unlimited || list.size() < count) {
            int i = string.indexOf(spliter, oldIndex);
            if (i >= 0) {
                if (oldIndex == i) {
                    list.add("");
                } else {
                    list.add(string.substring(oldIndex, i));
                }
                oldIndex = i + length;
            } else {
                break;
            }
        }
        list.add(string.substring(oldIndex));
        return list;
    }

    public static List<String> split(String string, char spliter) {
        return split(string, spliter, 0);
    }

    public static List<String> split(String string, char spliter, int limit) {
        int oldIndex = 0;
        int count = limit - 1;
        boolean unlimited = limit <= 0;
        List<String> list = new ArrayList<>();
        while (unlimited || list.size() < count) {
            int i = string.indexOf(spliter, oldIndex);
            if (i >= 0) {
                if (oldIndex == i) {
                    list.add("");
                } else {
                    list.add(string.substring(oldIndex, i));
                }
                oldIndex = i + 1;
            } else {
                break;
            }
        }
        list.add(string.substring(oldIndex));
        return list;
    }

    public static boolean createNewFile(File file) throws IOException {
        return file.createNewFile();
    }
    @SuppressWarnings("UnusedReturnValue")
    public static boolean mkdirs(File file) {
        return file.mkdirs();
    }

    public static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }
        return sw.toString();
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public static void submitInvUpdate(Player player) {
        player.updateInventory();
    }

    @Deprecated
    @SafeVarargs
    public static void runCommands(Player player, List<String> list, Pair<String, Object>... replacements) {
        List<IAction> actions = loadActions(list);
        List<Pair<String, Object>> args = Lists.newArrayList(replacements);
        for (IAction action : actions) {
            action.run(player, args);
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

    public static boolean isMovedLoc(PlayerMoveEvent e) {
        return isMovedLoc(e.getFrom(), e.getTo());
    }

    public static boolean isMovedLoc(Location loc1, Location loc2) {
        return loc1 != null && loc2 != null && (loc1.getX() != loc2.getX() || loc1.getZ() != loc2.getZ());
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

    public static List<Player> getOnlinePlayersByUUID(Collection<UUID> uuidList) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (uuidList.contains(player.getUniqueId())) players.add(player);
        }
        return players;
    }

    public static List<Player> getOnlinePlayersByName(Collection<String> nameList) {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(nameList);
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (names.contains(player.getName())) players.add(player);
        }
        return players;
    }

    public static Optional<Float> parseFloat(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Float.parseFloat(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> T valueOr(Class<T> type, String s, T def) {
        if (s == null || s.isEmpty()) return def;
        if (type.isEnum()) {
            for (T t : type.getEnumConstants()) {
                if (((Enum) t).name().equalsIgnoreCase(s)) return t;
            }
        } else {
            Registry<?> registry = RegistryConverter.fromType(type);
            Keyed matched = registry.match(s);
            if (/*matched != null && */type.isInstance(matched)) {
                return (T) matched;
            }
        }
        return def;
    }

    public static <T> T valueOrNull(Class<T> type, String... s) {
        for (String str : s) {
            T value = valueOr(type, str, null);
            if (value != null) return value;
        }
        return null;
    }

    @NotNull
    public static <K, V> V getOrPut(Map<K, V> map, K key, Function<K, V> creator) {
        V value = map.get(key);
        if (value != null) return value;
        V newValue = creator.apply(key);
        map.put(key, newValue);
        return newValue;
    }

    @NotNull
    public static <K, V> V getOrPut(Map<K, V> map, K key, Supplier<V> creator) {
        V value = map.get(key);
        if (value != null) return value;
        V newValue = creator.get();
        map.put(key, newValue);
        return newValue;
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
        if (files != null) for (File file : files) {
            if (file.isFile()) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                for (String ignorePackage : ignorePackages) {
                    if (className.startsWith(ignorePackage)) return;
                }
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
                    if (className.startsWith(ignorePackage)) {
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
