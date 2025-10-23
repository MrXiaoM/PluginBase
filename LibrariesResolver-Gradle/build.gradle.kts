plugins {
    java
    groovy
    signing
    `maven-publish`
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains:annotations:24.0.0")
    implementation("com.google.guava:guava:21.0")
}

setupJava(8)
setupJavadoc(false)
setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin libraries resolver gradle pre-process logic",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/LibrariesResolver-Gradle",
)
