
dependencies {
    applyLibraries("spigot-api", "compileOnly")
    implementation(project(":modules:library"))
}

val java9 by sourceSets.creating {
    java {
        srcDirs("src/main/java9")
    }
}

tasks.named<JavaCompile>("compileJava9Java") {
    options.release.set(9)
    classpath += sourceSets.main.get().output
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Multi-Release" to "true")
    }
    into("META-INF/versions/9") {
        from(java9.output)
    }
}

setupPublishing(
    publishDesc = "MrXiaoM's Bukkit plugin basic core module contains some magic function",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/modules/magic",
)
