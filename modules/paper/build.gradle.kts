
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.21.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.21.0")
    implementation(project(":modules:library"))
}

setupJava(8)
setupJavadoc()
setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core with Paper intelligence",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/paper",
)
