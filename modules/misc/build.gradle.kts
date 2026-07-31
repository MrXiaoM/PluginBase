
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT")
    implementation(project(":modules:library"))
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module contains some misc utils",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/misc",
)
