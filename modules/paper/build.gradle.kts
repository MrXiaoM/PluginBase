
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    applyLibraries("adventure", "compileOnly")
    implementation(project(":modules:library"))
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core with Paper intelligence",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/paper",
)
