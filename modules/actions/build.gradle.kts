
dependencies {
    applyLibraries("spigot-api", "compileOnly")
    applyLibraries("adventure", "compileOnly")
    implementation(project(":modules:library"))
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module that parsing actions",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/actions",
)
