
subprojects {
    val shouldPublish = !path.contains("tests")
    apply(plugin="java")
    if (shouldPublish) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }

    group = project.jitpackGroup ?: "${rootProject.group}.pluginbase"

    dependencies {
        add("compileOnly", rootProject.files("buildSrc/libs/stub-rt.jar"))
        add("compileOnly", "org.jetbrains:annotations:24.0.0")
    }

    setupJava(8)
    if (shouldPublish) {
        setupJavadoc()
    }
    setupLibraries(
        key="spigot-api",
        "org.spigotmc:spigot-api:1.21.2-R0.1-SNAPSHOT",
    )
    setupLibraries(
        key="nbt-api",
        "de.tr7zw:item-nbt-api:2.16.0",
    )
    setupLibraries(
        key="adventure",
        "net.kyori:adventure-api:4.25.0",
        "net.kyori:adventure-text-serializer-gson:4.25.0",
        "net.kyori:adventure-text-minimessage:4.25.0",
    )
    configurations.named("compileOnly").configure {
        exclude(group="org.jspecify", module="jspecify")
    }
}
