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

val targetJavaVersion = 11
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains:annotations:24.0.0")
    implementation("com.google.guava:guava:21.0")
}

setupJava(targetJavaVersion)
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
    fun com.github.gmazzo.buildconfig.BuildConfigClassSpec.field(name: String, value: String) {
        buildConfigField("String", name, "\"$value\"")
    }

    forClass("top.mrxiaom.gradle", "LibrariesResolver") {
        for (proj in project(":LibrariesResolver").subprojects) {
            if (proj == project) continue
            val dep = dependency(proj) ?: continue
            field(name(proj), dep)
        }
    }
    forClass("top.mrxiaom.gradle", "PluginBase") {
        for (proj in project(":modules").subprojects) {
            val dep = dependency(proj) ?: continue
            field(name(proj), "\"$dep\"")
        }
        field("VERSION", project.version.toString())
    }
    forClass("top.mrxiaom.gradle", "Depend") {
        field("annotations", "org.jetbrains:annotations:24.0.0")
        field("HikariCP", "com.zaxxer:HikariCP:4.0.3")
        field("EvalEx", "top.mrxiaom:EvalEx-j8:3.4.0")
        field("nbtapi", "de.tr7zw:item-nbt-api:2.15.7")
    }
}
