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
    applyLibraries("spigot-api", "compileOnly", "testImplementation")
    
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.mojang:authlib:2.1.28")

    applyLibraries("adventure", "compileOnly")
    applyLibraries("nbt-api", "compileOnly")
    compileOnly("com.github.technicallycoded:FoliaLib:0.4.4")

}

setupPublishing(
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/library"
)
