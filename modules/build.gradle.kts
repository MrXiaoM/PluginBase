
subprojects {
    apply(plugin="java")
    apply(plugin="maven-publish")
    apply(plugin="signing")

    group = "top.mrxiaom.pluginbase"

    dependencies {
        add("compileOnly", rootProject.files("buildSrc/libs/stub-rt.jar"))
        add("compileOnly", "org.jetbrains:annotations:24.0.0")
    }

    setupJava(8)
    setupJavadoc()
    setupLibraries(
        key="spigot-api",
        "org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT",
    )
    setupLibraries(
        key="nbt-api",
        "de.tr7zw:item-nbt-api:2.15.0",
    )
    setupLibraries(
        key="adventure",
        "net.kyori:adventure-api:4.25.0",
        "net.kyori:adventure-text-serializer-legacy:4.4.1",
        "net.kyori:adventure-text-minimessage:4.25.0",
    )
}
