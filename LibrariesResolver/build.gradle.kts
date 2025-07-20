plugins {
    java
    signing
    `maven-publish`
    id("com.gradleup.shadow")
}

dependencies {
    implementation("org.codehaus.plexus:plexus-utils:3.5.1")
    implementation("org.codehaus.plexus:plexus-interpolation:1.26")

    compileOnly(files("../libs/stub-rt.jar"))
    compileOnly("org.jetbrains:annotations:24.0.0")
}

val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
    withJavadocJar()
}
tasks {
    shadowJar {
        minimize()
        exclude("licenses/*", "META-INF/LICENSE*", "META-INF/NOTICE*")
        mapOf(
            "org.codehaus.plexus" to "plexus",
        ).forEach { (original, target) ->
            relocate(original, "top.mrxiaom.pluginbase.resolver.$target")
        }
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            links("https://hub.spigotmc.org/javadocs/spigot/")

            locale("zh_CN")
            encoding("UTF-8")
            docEncoding("UTF-8")
            addBooleanOption("keywords", true)
            addBooleanOption("Xdoclint:none", true)
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set(artifactId)
                description.set("MrXiaoM's Bukkit plugin libraries resolver")
                url.set("https://github.com/MrXiaoM/PluginBase")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/MrXiaoM/PluginBase/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        name.set("MrXiaoM")
                        email.set("mrxiaom@qq.com")
                    }
                }
                scm {
                    url.set("https://github.com/MrXiaoM/PluginBase")
                    connection.set("scm:git:https://github.com/MrXiaoM/PluginBase.git")
                    developerConnection.set("scm:git:https://github.com/MrXiaoM/PluginBase.git")
                }
            }
        }
    }
}
signing {
    val signingKey = rootProject.findProperty("signingKey")?.toString()
    val signingPassword = rootProject.findProperty("signingPassword")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications.getByName("maven"))
    }
}
