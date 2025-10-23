plugins {
    java
    signing
    `maven-publish`
    id("com.github.gmazzo.buildconfig")
}

dependencies {
    compileOnly(files("../libs/stub-rt.jar"))
    compileOnly("org.jetbrains:annotations:24.0.0")
}
buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.pluginbase.resolver")

    buildConfigField("String", "VERSION", "\"${version}\"")
}

setupJava(8)
setupJavadoc(false)
setupPublishing(
    publishName = "LibrariesResolver-Lite",
    publishDesc = "MrXiaoM's Bukkit plugin libraries resolver, but a more simple variant",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/LibrariesResolver/lite",
)
