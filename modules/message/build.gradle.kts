
dependencies {
    applyLibraries("spigot-api", "compileOnly", "testImplementation")
    implementation(project(":modules:library"))

    applyLibraries("adventure", "compileOnly")
}

tasks.test {
    for (item in subprojects) {
        dependsOn(item.tasks.test)
    }
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module that parsing actions",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/actions",
)
