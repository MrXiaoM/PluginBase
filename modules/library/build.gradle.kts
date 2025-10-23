plugins {
    id("com.github.gmazzo.buildconfig")
    id("com.gradleup.shadow")
}

buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.pluginbase")

    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
}
repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        mavenContent { includeGroup("com.mojang") }
    }
}
@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.mojang:authlib:2.1.28")
    compileOnly(files("../../libs/stub-rt.jar"))

    compileOnly("net.kyori:adventure-api:4.21.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.21.0")
    compileOnly("de.tr7zw:item-nbt-api:2.15.0")
    compileOnly("com.github.technicallycoded:FoliaLib:0.4.4")

    compileOnly("org.jetbrains:annotations:24.0.0")
}

setupJava(8)
setupJavadoc()
setupPublishing(
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/library"
)
