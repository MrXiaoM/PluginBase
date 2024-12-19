[<< 返回开发文档](README.md)

# 构建脚本

依赖于 Gradle

首先是 wrapper 配置 `gradle/wrapper/gradle-wrapper.properties`  
使用 7.3.3 足矣
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-7.3.3-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

编写构建脚本 build.gradle.kts 如下
```kotlin
plugins {
    java
    `maven-publish`
    id ("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "top.mrxiaom"
version = "1.0.0-SNAPSHOT"

var shadowGroup = "top.mrxiaom.yourplugin.utils"
val targetJavaVersion = 8

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") 
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/") // NMS
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    // NMS: https://repo.rosewooddev.io/service/rest/repository/browse/public/org/spigotmc/spigot/
    compileOnly("org.spigotmc:spigot:1.20")

    implementation("top.mrxiaom:PluginBase:1+") // 使用 1.x 最新版依赖库
    implementation("org.jetbrains:annotations:21.0.0")
    // implementation("com.zaxxer:HikariCP:4.0.3") // 数据库连接池
}
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks {
    shadowJar {
        archiveClassifier.set("")
        mapOf(
            "org.intellij.lang.annotations" to "annotations.intellij",
            "org.jetbrains.annotations" to "annotations.jetbrains",
            // 一定要将 PluginBase 依赖给 relocate 到你的插件包中
            "top.mrxiaom.pluginbase" to "base",
            // "com.zaxxer.hikari" to "hikari",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()

            artifact(tasks.shadowJar)
        }
    }
}
```
