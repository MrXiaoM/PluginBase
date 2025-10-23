import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

fun Project.setupJava(targetJavaVersion: Int, withDocuments: Boolean = true) {
    extensions.configure<JavaPluginExtension> {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion)
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
        if (withDocuments) {
            withSourcesJar()
            withJavadocJar()
        }
    }
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

fun Project.setupJavadoc(addSpigotLink: Boolean = true, block: StandardJavadocDocletOptions.() -> Unit = {}) {
    tasks.getByName<Javadoc>("javadoc") {
        (options as StandardJavadocDocletOptions).apply {
            if (addSpigotLink) {
                links("https://hub.spigotmc.org/javadocs/spigot/")
            }

            locale("zh_CN")
            encoding("UTF-8")
            docEncoding("UTF-8")
            addBooleanOption("keywords", true)
            addBooleanOption("Xdoclint:none", true)

            block()
        }
    }
}

fun Project.setupPublishing(
    publishGroup: String? = null,
    publishName: String? = null,
    publishDesc: String = "MrXiaoM's Bukkit plugin basic core",
    sourceCodeUrl: String = "https://github.com/MrXiaoM/PluginBase"
) {
    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components.getByName("java"))
                groupId = publishGroup ?: project.group.toString()
                artifactId = publishName ?: project.name
                version = project.version.toString()

                pom {
                    name.set(artifactId)
                    description.set(publishDesc)
                    url.set(sourceCodeUrl)
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
                        url.set(sourceCodeUrl)
                        connection.set("scm:git:https://github.com/MrXiaoM/PluginBase.git")
                        developerConnection.set("scm:git:https://github.com/MrXiaoM/PluginBase.git")
                    }
                }
            }
        }
    }
    extensions.configure<SigningExtension> {
        val signingKey = findProperty("signingKey")?.toString()
        val signingPassword = findProperty("signingPassword")?.toString()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(extensions.getByType<PublishingExtension>().publications.getByName("maven"))
        }
    }
}
