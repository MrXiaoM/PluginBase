subprojects {
    apply(plugin="java")
    apply(plugin="maven-publish")
    apply(plugin="signing")

    group = "top.mrxiaom"

    dependencies {
        add("compileOnly", rootProject.files("libs/stub-rt.jar"))
        add("compileOnly", "org.jetbrains:annotations:24.0.0")
    }

    setupJava(8)
    setupJavadoc(false)
}
