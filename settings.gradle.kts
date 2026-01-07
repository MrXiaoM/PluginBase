rootProject.name = "PluginBase"

fun includeProjects(name: String) {
    val files = File(name).listFiles() ?: return
    include(":$name")
    for (folder in files) {
        if (folder.name == "sql") continue
        if (folder.isDirectory && File(folder, "build.gradle.kts").exists()) {
            include(":$name:${folder.name}")
        }
    }
}
includeProjects("modules")
includeProjects("LibrariesResolver")
