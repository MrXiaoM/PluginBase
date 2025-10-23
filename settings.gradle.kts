rootProject.name = "PluginBase"

include(":LibrariesResolver")
include(":LibrariesResolver-Gradle")
include(":LibrariesResolver-Lite")
include(":modules")

File("modules").listFiles()?.forEach { folder ->
    if (folder.isDirectory && File(folder, "build.gradle.kts").exists()) {
        include(":modules:${folder.name}")
    }
}
