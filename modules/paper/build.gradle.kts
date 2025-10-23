
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    applyLibraries("adventure", "compileOnly")
    implementation(project(":modules:library"))
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core with Paper intelligence",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/paper",
)
