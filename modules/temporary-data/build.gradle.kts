
dependencies {
    applyLibraries("spigot-api", "compileOnly")
    implementation(project(":modules:library"))
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module that processing temporary data",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/temporary-data",
)
