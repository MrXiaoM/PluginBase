setupJava(21)

dependencies {
    applyLibraries("spigot-api", "compileOnly", "testImplementation")

    val v = "4.25.0"
    testRuntimeOnly("net.kyori:adventure-api:${v}")
    testRuntimeOnly("net.kyori:adventure-text-serializer-gson:${v}")
    testRuntimeOnly("net.kyori:adventure-text-minimessage:${v}")

    applyLibraries("adventure", "compileOnly", "testCompileOnly")
    applyLibraries("nbt-api", "compileOnly")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}
