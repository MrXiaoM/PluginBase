import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.*

plugins {
    java
    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "top.mrxiaom"
version = "1.3.9"

repositories {
    mavenCentral()
    if (Locale.getDefault().country == "CN") runCatching {
        val url = "https://maven.fastmirror.net/repositories/minecraft/"
        val conn = URL(url).openConnection().apply { connect() } as HttpURLConnection
        if (conn.responseCode == 200) maven(url)
        else {
            println("镜像仓库错误 (${conn.responseCode} ${conn.responseMessage})，不使用镜像")
        }
    }
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/") {
        mavenContent { includeGroup("com.mojang") }
    }

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://jitpack.io/")

    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.mojang:authlib:2.1.28")
    compileOnly(files("libs/stub-rt.jar"))

    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("de.tr7zw:item-nbt-api:2.14.1")
    compileOnly("com.github.technicallycoded:FoliaLib:0.4.4")

    implementation("org.jetbrains:annotations:21.0.0")
}
val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
    withJavadocJar()
}
tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            links("https://docs.oracle.com/javase/8/docs/api/")
            links("https://hub.spigotmc.org/javadocs/spigot/")

            locale("zh_CN")
            encoding("UTF-8")
            docEncoding("UTF-8")
            addBooleanOption("keywords", true)
            addBooleanOption("Xdoclint:none", true)
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()

            pom {
                name.set(artifactId)
                description.set("MrXiaoM's Bukkit plugin basic core")
                url.set("https://github.com/MrXiaoM/PluginBase")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/MrXiaoM/PluginBase/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        name.set("MrXiaoM")
                        email.set("mrxiaom@qq.com")
                    }
                }
                scm {
                    url.set("https://github.com/MrXiaoM/PluginBase")
                    connection.set("scm:git:https://github.com/MrXiaoM/PluginBase.git")
                    developerConnection.set("scm:git:https://github.com/MrXiaoM/PluginBase.git")
                }
            }
        }
    }
}
signing {
    val signingKey = findProperty("signingKey")?.toString()
    val signingPassword = findProperty("signingPassword")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications.getByName("maven"))
    }
}
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(findProperty("MAVEN_USERNAME")?.toString())
            password.set(findProperty("MAVEN_PASSWORD")?.toString())
        }
    }
}
