import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer

setupJava(25)

val jmhVersion = "1.37"
val sourceSets = extensions.getByType<SourceSetContainer>()

dependencies {
    applyLibraries("spigot-api", "compileOnly", "testImplementation")

    val v = "5.2.0"
    compileOnly("net.kyori:adventure-api:${v}")
    compileOnly("net.kyori:adventure-text-serializer-gson:${v}")
    compileOnly("net.kyori:adventure-text-minimessage:${v}")
    testRuntimeOnly("net.kyori:adventure-api:${v}")
    testRuntimeOnly("net.kyori:adventure-text-serializer-gson:${v}")
    testRuntimeOnly("net.kyori:adventure-text-minimessage:${v}")

    applyLibraries("adventure", "compileOnly", "testCompileOnly")
    applyLibraries("nbt-api", "compileOnly")

    implementation("org.openjdk.jmh:jmh-core:${jmhVersion}")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:${jmhVersion}")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

val jmh by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "运行 SparrowMiniMessage 与 DefaultMiniMessage 的 JMH 性能基准。"
    dependsOn(tasks.named("classes"))
    classpath = sourceSets.named("main").get().runtimeClasspath
    mainClass.set("org.openjdk.jmh.Main")

    val resultFile = layout.buildDirectory.file("jmh/results.json")
    outputs.file(resultFile)

    doFirst {
        resultFile.get().asFile.parentFile.mkdirs()
        args = listOf(
            providers.gradleProperty("jmhInclude").orElse(".*").get(),
            "-wi", providers.gradleProperty("jmhWarmupIterations").orElse("5").get(),
            "-i", providers.gradleProperty("jmhMeasurementIterations").orElse("5").get(),
            "-f", providers.gradleProperty("jmhForks").orElse("3").get(),
            "-t", providers.gradleProperty("jmhThreads").orElse("1").get(),
            "-rf", "json",
            "-rff", resultFile.get().asFile.absolutePath,
            "-prof", providers.gradleProperty("jmhProfiler").orElse("gc").get(),
            "-foe", "true",
        )
    }
}
