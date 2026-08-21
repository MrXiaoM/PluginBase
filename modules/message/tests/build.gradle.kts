subprojects {
    dependencies {
        add("implementation", project(":modules:library"))
        add("implementation", project(":modules:message"))
        add("testImplementation", platform("org.junit:junit-bom:6.1.3"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")

        if (name != "shared") {
            add("implementation", project(":modules:message:tests:shared"))
        }
    }
}
