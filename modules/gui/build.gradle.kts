
dependencies {
    applyLibraries("spigot-api", "compileOnly")
    applyLibraries("nbt-api", "compileOnly")
    applyLibraries("adventure", "compileOnly")
    implementation(project(":modules:library"))
    implementation(project(":modules:actions"))
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module that making inventory GUI",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/gui",
)
