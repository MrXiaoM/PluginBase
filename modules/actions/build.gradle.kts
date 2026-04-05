repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    applyLibraries("adventure", "compileOnly")
    implementation(project(":modules:library"))

    compileOnly("com.destroystokyo.paper:paper-api:1.15.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:26.1")
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module that parsing actions",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/actions",
)
