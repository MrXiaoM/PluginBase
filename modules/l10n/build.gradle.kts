
dependencies {
    applyLibraries("spigot-api", "compileOnly")
    applyLibraries("adventure", "compileOnly")
    implementation(project(":modules:library"))
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module that processing localization",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/l10n",
)
