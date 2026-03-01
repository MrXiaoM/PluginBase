package top.mrxiaom.pluginbase.utils;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.ICommandDispatcher;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.pluginbase.utils.diapatcher.BukkitDispatcher;
import top.mrxiaom.pluginbase.utils.diapatcher.FoliaDispatcher;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.MatchResult;

/**
 * 大杂烩工具库，所有杂项方法都放在这里
 */
@SuppressWarnings({"unused"})
public class Util {
    public static Map<String, OfflinePlayer> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static Map<UUID, OfflinePlayer> playersByUUID = new TreeMap<>();
    private static ICommandDispatcher dispatcher;

    public static void init(BukkitPlugin plugin) {
        try {
            Bukkit.getServer().getClass().getDeclaredMethod("dispatchCmdAsync", CommandSender.class, String.class);
            dispatcher = new FoliaDispatcher(plugin.getScheduler());
        } catch (ReflectiveOperationException e) {
            dispatcher = BukkitDispatcher.INSTANCE;
        }
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

    public static void dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine) {
        dispatcher.dispatchCommand(sender, commandLine);
    }

    /**
     * 获取 Inventory 的 InventoryHolder 实例
     * @return 在 Folia 服务端，如果是 BlockInventoryHolder，会因为异步调用方块而报错。该报错会被捕捉，返回 <code>null</code>。
     */
    @Nullable
    public static InventoryHolder getHolder(@NotNull Inventory inv) {
        // 因为 Folia 非要找存在感，把日志打出来，所以需要增加额外判定
        try {
            // 这个 getLocation() 在 1.9 加入，所以只要这里报错，也可以放心调用 .getHolder()
            if (inv.getLocation() != null) return null;
        } catch (Throwable ignored) {
        }
        try {
            return inv.getHolder();
        } catch (Throwable ignored) { // fuck folia
            return null;
        }
    }

    /**
     * 遍历文件夹内的每个文件，包括子文件夹中的文件
     * @param folder 文件夹
     * @param suffix 传入 <code>reloadConfig</code> 中的文件路径字符串是否需要包含文件后缀名
     * @param reloadConfig 遍历逻辑
     */
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

    /**
     * 获取相对路径
     * @param parent 父目录
     * @param file 文件
     * @param suffix 返回值是否需要包含文件后缀名
     */
    public static String getRelationPath(File parent, File file, boolean suffix) {
        String parentPath = parent.getAbsolutePath();
        String path = file.getAbsolutePath();
        if (!path.startsWith(parentPath)) return suffix ? path : nameWithoutSuffix(path);
        String s = path.substring(parentPath.length()).replace("\\", "/");
        String relation = s.startsWith("/") ? s.substring(1) : s;
        return suffix ? relation : nameWithoutSuffix(relation);
    }

    /**
     * 获取不含文件后缀名的路径
     * @param s 路径使用 <code>/</code> 作为分隔符，不支持 Windows 的 <code>\</code> 分隔符
     */
    public static String nameWithoutSuffix(String s) {
        Path path = Paths.get(s);
        Path fileName = path.getFileName();
        if (fileName == null) {
            return s;
        }
        String fileNameStr = fileName.toString();
        int lastDotIndex = fileNameStr.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            return s;
        }
        String fileNameWithoutExt = fileNameStr.substring(0, lastDotIndex);
        Path parentDir = path.getParent();
        if (parentDir == null) {
            return fileNameWithoutExt;
        } else {
            return parentDir.resolve(fileNameWithoutExt).toString();
        }
    }

    /**
     * @see File#createNewFile()
     */
    public static boolean createNewFile(File file) throws IOException {
        return file.createNewFile();
    }

    /**
     * @see File#mkdirs()
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean mkdirs(File file) {
        return file.mkdirs();
    }

    /**
     * 将 Throwable 的堆栈信息打印到字符串
     */
    public static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }
        return sw.toString();
    }

    /**
     * @see Player#updateInventory()
     */
    @SuppressWarnings({"UnstableApiUsage"})
    public static void submitInvUpdate(Player player) {
        player.updateInventory();
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

    /**
     * 获取已缓存的离线玩家数据
     * @param name 玩家名
     */
    public static Optional<OfflinePlayer> getOfflinePlayer(String name) {
        return Optional.ofNullable(players.get(name));
    }

    /**
     * 获取已缓存的离线玩家数据
     * @param uuid 玩家UUID
     */
    public static Optional<OfflinePlayer> getOfflinePlayer(UUID uuid) {
        return Optional.ofNullable(playersByUUID.get(uuid));
    }

    /**
     * 获取在线玩家
     * @param name 玩家名
     */
    public static Optional<Player> getOnlinePlayer(String name) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) return Optional.of(player);
        }
        return Optional.empty();
    }

    /**
     * 获取在线玩家
     * @param uuid 玩家UUID
     */
    public static Optional<Player> getOnlinePlayer(UUID uuid) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(uuid)) return Optional.of(player);
        }
        return Optional.empty();
    }

    /**
     * 获取在线玩家列表
     * @param uuidList 玩家UUID列表
     */
    public static List<Player> getOnlinePlayersByUUID(Collection<UUID> uuidList) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (uuidList.contains(player.getUniqueId())) players.add(player);
        }
        return players;
    }

    /**
     * 获取在线玩家列表
     * @param nameList 玩家名列表
     */
    public static List<Player> getOnlinePlayersByName(Collection<String> nameList) {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(nameList);
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (names.contains(player.getName())) players.add(player);
        }
        return players;
    }

    /**
     * 将字符串转换为单精度浮点数
     */
    public static Optional<Float> parseFloat(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Float.parseFloat(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 将字符串转换为双精度浮点数
     */
    public static Optional<Double> parseDouble(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 将字符串转换为整数
     */
    public static Optional<Integer> parseInt(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 将字符串转换为长整数
     */
    public static Optional<Long> parseLong(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 通过 enum values 或者 Bukkit Registry 读取值
     * @param type 类型
     * @param s 输入的字符串
     * @param def 默认值
     */
    @SuppressWarnings({"rawtypes"})
    public static <T> T valueOr(Class<T> type, String s, T def) {
        if (s == null || s.isEmpty()) return def;
        if (type.isEnum()) {
            for (T t : type.getEnumConstants()) {
                if (((Enum) t).name().equalsIgnoreCase(s)) return t;
            }
        } else {
            try {
                T t = RegistryUtils.fromType(type, s);
                if (t != null) {
                    return t;
                }
            } catch (LinkageError ignored) {
            }
        }
        return def;
    }

    /**
     * 通过 enum values 或者 Bukkit Registry，输入多个结果，读取任意一个存在的值
     * @param type 类型
     * @param s 输入的多个字符串
     */
    public static <T> T valueOrNull(Class<T> type, String... s) {
        if (s.length == 0) return null;
        if (s.length == 1) return valueOr(type, s[0], null);
        for (String str : s) {
            T value = valueOr(type, str, null);
            if (value != null) return value;
        }
        return null;
    }

    /**
     * 解析字符串为物品类型
     * @see Util#valueOr(Class, String, Object)
     * @param s 输入的字符串
     * @return 物品类型，如果找不到将返回 <code>null</code>
     */
    @Nullable
    public static Material parseMaterial(String s) {
        return valueOr(Material.class, s, null);
    }

    /**
     * 解析字符串为音效
     * @see Util#valueOr(Class, String, Object)
     * @param s 输入的字符串
     * @return 音效，如果找不到将返回 <code>null</code>
     */
    @Nullable
    public static Sound parseSound(String s) {
        return valueOr(Sound.class, s, null);
    }

    /**
     * 解析字符串为附魔类型
     * @see Util#valueOr(Class, String, Object)
     * @param s 输入的字符串
     * @return 附魔类型，如果找不到将返回 <code>null</code>
     */
    @Nullable
    public static Enchantment parseEnchant(String s) {
        return valueOr(Enchantment.class, s, null);
    }

    /**
     * 解析字符串为药水效果类型
     * @see Util#valueOr(Class, String, Object)
     * @param s 输入的字符串
     * @return 药水效果类型，如果找不到将返回 <code>null</code>
     */
    @Nullable
    public static PotionEffectType parsePotion(String s) {
        return valueOr(PotionEffectType.class, s, null);
    }

    /**
     * 检查类是否存在
     */
    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * 将数值限制在指定范围内
     * @param num 数值
     * @param min 最小值
     * @param max 最大值
     */
    public static int between(int num, int min, int max) {
        if (num < min) num = min;
        if (num > max) num = max;
        return num;
    }

    /**
     * 将数值限制在指定范围内
     * @param num 数值
     * @param min 最小值
     * @param max 最大值
     */
    public static double between(double num, double min, double max) {
        if (num < min) num = min;
        if (num > max) num = max;
        return num;
    }

    /**
     * 获取类列表，加载时出错的类忽略
     * @param loader 类加载器
     * @param packageName 起始包名
     * @param ignorePackages 忽略的包名
     */
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
                    return classes;
                }
                if ("jar".equals(protocol)) {
                    findAnnotatedClassesInJar(url, packageName, ignorePackages, classes);
                    return classes;
                }
            }
        } catch (Exception ignored) {
        }
        return classes;
    }

    public static Set<Class<?>> getClasses(File jarFile, String packageName, List<String> ignorePackages) {
        Set<Class<?>> classes = new TreeSet<>(Comparator.comparing(Class::getName));
        try (JarFile jar = new JarFile(jarFile)) {
            findAnnotatedClassesInJar(jar, packageName, ignorePackages, classes);
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
                } catch (ClassNotFoundException | LinkageError ignored) {
                }
            } else if (file.isDirectory()) {
                findAnnotatedClassesInDirectory(file, packageName + "." + file.getName(), ignorePackages, classes);
            }
        }
    }

    private static void findAnnotatedClassesInJar(URL url, String packageName, List<String> ignorePackages, Set<Class<?>> classes) throws Exception {
        try (JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile()) {
            findAnnotatedClassesInJar(jar, packageName, ignorePackages, classes);
        }
    }

    private static void findAnnotatedClassesInJar(JarFile jar, String packageName, List<String> ignorePackages, Set<Class<?>> classes) {
        String packagePath = packageName.replace(".", "/");
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
                } catch (ClassNotFoundException | LinkageError ignored) {
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
