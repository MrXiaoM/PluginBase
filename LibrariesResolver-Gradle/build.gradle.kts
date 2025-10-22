plugins {
    java
    groovy
    signing
    `maven-publish`
}

group = "top.mrxiaom"

@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains:annotations:24.0.0")
    implementation("com.google.guava:guava:21.0")
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
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
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
                description.set("MrXiaoM's Bukkit plugin libraries resolver gradle pre-process logic")
                url.set("https://github.com/MrXiaoM/PluginBase/tree/main/LibrariesResolver-Gradle")
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
                    url.set("https://github.com/MrXiaoM/PluginBase/tree/main/LibrariesResolver-Gradle")
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
