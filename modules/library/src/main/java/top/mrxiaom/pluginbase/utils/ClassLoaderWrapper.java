package top.mrxiaom.pluginbase.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * URLClassLoader 的包装器
 */
public class ClassLoaderWrapper {
    public static final boolean isSupportLibraryLoader = supportLibraryLoader();
    private static boolean supportLibraryLoader() {
        try {
            Class.forName("org.bukkit.plugin.java.LibraryLoader");
            Class<?> desc = Class.forName("org.bukkit.plugin.PluginDescriptionFile");
            desc.getDeclaredMethod("getLibraries");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
    public static ClassLoaderWrapper resolve(URLClassLoader pluginClassLoader) {
        return isSupportLibraryLoader
                ? new ClassLoaderWrapper(findLibraryLoader(pluginClassLoader))
                : new ClassLoaderWrapper(pluginClassLoader);
    }
    public static URLClassLoader findLibraryLoader(URLClassLoader pluginClassLoader) {
        URLClassLoader classLoader = findLibraryLoaderOrNull(pluginClassLoader);
        return classLoader == null ? pluginClassLoader : classLoader;
    }
    public static URLClassLoader findLibraryLoaderOrNull(URLClassLoader pluginClassLoader) {
        try {
            Class<?> type = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
            if (type.isInstance(pluginClassLoader)) {
                Object classLoader = getLibraryLoader(type, pluginClassLoader);
                if (classLoader instanceof URLClassLoader) {
                    return (URLClassLoader) classLoader;
                }
            }
            Class<?> oldType = Class.forName("org.bukkit.plugin.java.JavaClassLoader");
            if (oldType.isInstance(pluginClassLoader)) {
                Object classLoader = getLibraryLoader(oldType, pluginClassLoader);
                if (classLoader instanceof URLClassLoader) {
                    return (URLClassLoader) classLoader;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }
    private static Object getLibraryLoader(Class<?> type, Object instance) throws ReflectiveOperationException {
        Field field = type.getDeclaredField("libraryLoader");
        field.setAccessible(true);
        return field.get(instance);
    }
    @FunctionalInterface
    public interface DelegateAddURL {
        void run(URL url) throws Exception;
    }
    private final URLClassLoader classLoader;
    private final DelegateAddURL addURL;
    private boolean supported;
    public ClassLoaderWrapper(URLClassLoader classLoader) {
        this.classLoader = classLoader;
        this.addURL = defineAddURLMethod();
    }

    @SuppressWarnings({"unchecked"})
    private DelegateAddURL defineAddURLMethod() {
        try {
            // 反射方法，直接调用 addURL
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            supported = true;
            return url -> method.invoke(classLoader, url);
        } catch (Exception ignored) {
        }
        try {
            // unsafe 方法，拿到 URLClassPath 的 urls 和 path
            // 模仿 JDK 源码执行 addURL
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Field fieldUCP = URLClassLoader.class.getDeclaredField("ucp");
            Object ucp = unsafe.getObject(classLoader, unsafe.objectFieldOffset(fieldUCP));
            Class<?> clazz = ucp.getClass();
            Field fieldUrls = defineUrlsField(clazz);
            Field fieldPath = clazz.getDeclaredField("path");
            if (fieldUrls != null) {
                Collection<URL> urls = (Collection<URL>) unsafe.getObject(ucp, unsafe.objectFieldOffset(fieldUrls));
                Collection<URL> path = (Collection<URL>) unsafe.getObject(ucp, unsafe.objectFieldOffset(fieldPath));
                supported = true;
                return url -> {
                    synchronized (urls) {
                        urls.add(url);
                        path.add(url);
                    }
                };
            }
        } catch (Throwable ignored) {
        }
        supported = false;
        return url -> {
            throw new UnsupportedOperationException("当前环境不支持 addURL");
        };
    }
    private static Field defineUrlsField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("unopenedUrls")) {
                return field;
            }
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("urls")) {
                return field;
            }
        }
        return null;
    }

    /**
     * 获取原始 ClassLoader
     */
    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 向 ClassLoader 中添加新的库
     */
    public void addURL(URL url) throws Exception {
        addURL.run(url);
    }

    /**
     * 包装器的相关操作是否受支持
     */
    public boolean isSupported() {
        return supported;
    }
}
