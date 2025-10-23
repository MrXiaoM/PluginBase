plugins {
    java
    signing
    `maven-publish`
}

dependencies {
    compileOnly(files("../libs/stub-rt.jar"))
    compileOnly("org.jetbrains:annotations:24.0.0")
}

setupJava(8)
setupJavadoc(false)
setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin libraries resolver",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/LibrariesResolver",
)
