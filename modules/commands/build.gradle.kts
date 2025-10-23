
dependencies {
    applyLibraries("spigot-api", "compileOnly")
    implementation(project(":modules:library"))
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module that handling commands",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/commands",
)
