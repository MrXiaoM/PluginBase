plugins {
    id("com.github.gmazzo.buildconfig")
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
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.mojang:authlib:2.1.28")
    compileOnly("net.md-5:bungeecord-chat:1.21-R0.5-SNAPSHOT")

    // "4.11.0", "4.17.0", "4.25.0", "5.2.0"
    val testAdventureVersion: String? = "4.11.0"
    testAdventureVersion?.also {
        testRuntimeOnly("net.kyori:adventure-api:$it")
        testRuntimeOnly("net.kyori:adventure-text-serializer-gson:$it")
        testRuntimeOnly("net.kyori:adventure-text-minimessage:$it")
    }

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    applyLibraries("adventure", "compileOnly", "testCompileOnly")
    applyLibraries("nbt-api", "compileOnly")
}

tasks.test {
    useJUnitPlatform()
}

setupPublishing(
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/library"
)
