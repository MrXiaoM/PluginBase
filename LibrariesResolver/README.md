# Libraries Resolver

这个子项目的代码来自 [apache/maven](https://github.com/apache/maven) 和 [apache/maven-resolver](https://github.com/apache/maven-resolver) 等，进行一些简化、清理代码，用于下载依赖库。

## 使用

获取版本：https://central.sonatype.com/artifact/top.mrxiaom/LibrariesResolver/versions  
(LibrariesResolver 与 PluginBase 版本号同步)
```kotlin
dependencies {
    implementation("top.mrxiaom:LibrariesResolver:$VERSION:all") { isTransitive = false }
}
```

```java
public static void resolveLibraries(Logger logger) {
    // 下载的依赖存在哪里
    File librariesDir = new File("libraries");
    // 创建依赖解决器，可以指定仓库，默认使用 华为云镜像 + Maven Central 官方
    DefaultLibraryResolver resolver = new DefaultLibraryResolver(logger, librariesDir);
    
    // 添加依赖声明
    resolver.addLibrary("net.kyori:adventure-text-minimessage:4.21.0");
    
    // 执行获取依赖
    List<URL> libraries = resolver.doResolve();
    for (URL url : libraries) {
        logger.info("找到依赖: " + url.toString());
    }
    // TODO: 将 libraries 添加到 URLClassLoader 中即可
}
```

推荐与 `buildconfig` 一同使用，同步构建脚本与运行时插件的依赖信息。

```kotlin
plugins {
    id("java")
    id("com.github.gmazzo.buildconfig") version "5.6.7"
}

val libraries = arrayListOf<String>()
fun DependencyHandlerScope.library(dependencyNotation: String) {
    compileOnly(dependencyNotation)
    libraries.add(dependencyNotation)
}

dependencies {
    library("net.kyori:adventure-api:4.22.0")
    library("net.kyori:adventure-platform-bukkit:4.4.0")
    library("net.kyori:adventure-text-serializer-gson:4.22.0")
    library("net.kyori:adventure-text-minimessage:4.22.0")
    implementation("top.mrxiaom:LibrariesResolver:$VERSION:all") { isTransitive = false }
}

buildConfig {
    className("BuildConstants")
    packageName("com.example.your.plugin")

    val librariesVararg = libraries.joinToString(", ") { "\"$it\"" }
    
    // buildConfigField("String", "VERSION", "\"${project.version}\"")
    // buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
    buildConfigField("String[]", "LIBRARIES", "new String[] { $librariesVararg }")
}
```

```java
package com.example.your.plugin;

import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.utils.ClassLoaderWrapper;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.logging.Logger;

/**
 * 插件主类
 */
public class ExamplePlugin extends JavaPlugin {
    private final ClassLoaderWrapper classLoader;
    public ExamplePlugin() throws Exception {
        this.classLoader = new ClassLoaderWrapper((URLClassLoader) getClassLoader());

        Logger logger = this.getLogger();
        logger.info("正在检查依赖库状态");
        File librariesDir = new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(logger, librariesDir);
        // 也可以使用其它仓库。为了精简本项目大小，不支持需要身份验证的仓库，只支持公开仓库
        // List<RemoteRepository> repositories = Arrays.asList(
        //         new RemoteRepository.Builder("papermc", "default", "https://repo.papermc.io/repository/maven-public").build(),
        //         new RemoteRepository.Builder("codemc", "default", "https://repo.codemc.io/repository/maven-public").build()
        // );
        // DefaultLibraryResolver resolver = new DefaultLibraryResolver(logger, librariesDir, repositories);

        resolver.addLibrary(BuildConstants.LIBRARIES);

        List<URL> libraries = resolver.doResolve();
        logger.info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            this.classLoader.addURL(library);
        }
    }
}
```

## 注意事项

请将需要使用依赖库的类的引用，给放到其它类，而不是放在插件主类。以免在依赖开始下载之前，因为找不到类报错导致无法正常初始化。

## 修改明细

使用了以下模块中的代码，为了方便统一管理，代码已 relocate 到 `top.mrxiaom.pluginbase.resolver` 包下，所有源文件的 LICENSE 在文件头部保留。

### apache/maven
> https://github.com/apache/maven/tree/maven-3.9.6

基于 `3.9.6` 修改 (其中的 `.mdo` 已预编译为 `.java`)
+ `maven-artifact`
+ `maven-builder-support`
+ `maven-model-builder`
+ `maven-model`
+ `maven-resolver-provider`

### apache/maven-resolver
> https://github.com/apache/maven-resolver/tree/33d385e93633fe8dfb170f4ea29c14ab7053ba7e

基于 `1.9.17-SNAPSHOT` 修改 (33d385e93633fe8dfb170f4ea29c14ab7053ba7e)
+ `maven-resolver-api`
+ `maven-resolver-connector-basic`
+ `maven-resolver-impl`
+ `maven-resolver-named-locks`
+ `maven-resolver-spi`
+ `maven-resolver-transport-http`
+ `maven-resolver-util`

### apache/httpcomponents-client
> https://github.com/apache/httpcomponents-client/tree/rel/v4.5.14

基于 `4.5.14` 修改
+ `httpclient`

### apache/httpcomponents-core
> https://github.com/apache/httpcomponents-core/tree/rel/v4.4.16

基于 `4.4.16` 修改
+ `httpcore`

## 主要修改
由于这个子模块用于内嵌在插件中作为依赖下载工具，所以尽可能仅保留下载依赖功能
+ 将 HTTP PUT 请求删除，下载依赖用不到
+ 将大部分用于兼容的 `deprecated` 内容删除
+ 删除一些未引用、不常用的类
+ 将仓库登录验证删除，仅支持公共仓库
+ 移除调试日志，不依赖 `SLF4J` 和 `commons-logging`
+ 移除对 `javax.inject` 的依赖
+ 移除 `httpclient` 的 Cookie 等未使用功能
+ 移除 apache 的 commons 系列工具库依赖，仅添加本项目所需部分源码

由于网络请求改起来还是太麻烦了，暂时还是基于 apache 的 httpclient 进行精简，凑合着用。
