plugins {
    id("java")
    id("com.gradleup.shadow")
}

val shadowLink = configurations.create("shadowLink")
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8-R0.1-SNAPSHOT")
    compileOnly("org.yaml:snakeyaml:2.2")
    shadowLink("org.yaml:snakeyaml:2.2")
}

tasks {
    shadowJar {
        configurations.add(shadowLink)
        relocate("org.yaml.snakeyaml", "top.mrxiaom.pluginbase.configuration.snakeyaml")
    }
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module that process configuration files",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/config",
)
