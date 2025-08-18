plugins {
    java
    signing
    `maven-publish`
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.21.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.21.0")
    implementation(rootProject)
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
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set(artifactId)
                description.set("MrXiaoM's Bukkit plugin basic core with Paper intelligence")
                url.set("https://github.com/MrXiaoM/PluginBase/tree/main/Paper")
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
                    url.set("https://github.com/MrXiaoM/PluginBase/tree/main/Paper")
                    connection.set("scm:git:https://github.com/MrXiaoM/PluginBase.git")
                    developerConnection.set("scm:git:https://github.com/MrXiaoM/PluginBase.git")
                }
            }
        }
    }
}
signing {
    val signingKey = rootProject.findProperty("signingKey")?.toString()
    val signingPassword = rootProject.findProperty("signingPassword")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications.getByName("maven"))
    }
}
