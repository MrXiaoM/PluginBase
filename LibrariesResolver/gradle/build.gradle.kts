import com.github.gmazzo.buildconfig.generators.BuildConfigJavaGenerator
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

plugins {
    java
    groovy
    signing
    `maven-publish`
    id("com.github.gmazzo.buildconfig")
}
rootProject.subprojects.forEach { subproject ->
    if (subproject.path != project.path) {
        evaluationDependsOn(subproject.path)
    }
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains:annotations:24.0.0")
    implementation("com.google.guava:guava:21.0")
}

setupJava(8)
setupJavadoc(false)
setupPublishing(
    publishName = "LibrariesResolver-Gradle",
    publishDesc = "MrXiaoM's Bukkit plugin libraries resolver gradle pre-process logic",
    sourceCodeUrl = "https://github.com/MrXiaoM/PluginBase/tree/main/LibrariesResolver/gradle",
)

buildConfig {
    generator(object : BuildConfigJavaGenerator() {
        override fun adaptSpec(spec: TypeSpec): TypeSpec {
            if (spec.name == "BuildConstants") return spec
            return spec.toBuilder().apply {
                methodSpecs.removeIf { it.isConstructor && it.hasModifier(Modifier.PRIVATE) }
                fieldSpecs.toList().also { fieldSpecs.clear() }.forEach {
                    val field = it.toBuilder()
                    field.modifiers.remove(Modifier.STATIC)
                    addField(field.build())
                }
            }.build()
        }
    })

    fun name(proj: Project): String {
        return buildString {
            var upper = false
            for ((_, ch) in proj.name.withIndex()) {
                if (ch == '-' || ch == '.') {
                    upper = true
                    continue
                }
                if (upper) {
                    append(ch.uppercase())
                    upper = false
                } else {
                    append(ch)
                }
            }
        }
    }
    fun dependency(proj: Project): String? {
        val publishing = proj.extensions.getByType<PublishingExtension>()
        val publication = publishing.publications.filterIsInstance<MavenPublication>().firstOrNull() ?: return null
        return "${publication.groupId}:${publication.artifactId}:${publication.version}"
    }

    forClass("top.mrxiaom.gradle", "LibrariesResolver") {
        for (proj in project(":LibrariesResolver").subprojects) {
            if (proj == project) continue
            val dep = dependency(proj) ?: continue
            buildConfigField("String", name(proj), "\"$dep\"")
        }
    }
    forClass("top.mrxiaom.gradle", "PluginBase") {
        for (proj in project(":modules").subprojects) {
            val dep = dependency(proj) ?: continue
            buildConfigField("String", name(proj), "\"$dep\"")
        }
    }
}
