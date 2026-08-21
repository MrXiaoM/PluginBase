rootProject.name = "PluginBase"

fun includeProjects(name: String) {
    val files = File(name.replace(':', '/')).listFiles() ?: return
    include(":$name")
    for (folder in files) {
        if (folder.isDirectory && File(folder, "build.gradle.kts").exists()) {
            include(":$name:${folder.name}")
        }
    }
}
includeProjects("modules")
includeProjects("modules:message:tests")
includeProjects("LibrariesResolver")
