subprojects {
    dependencies {
        add("implementation", project(":modules:library"))
        add("testImplementation", platform("org.junit:junit-bom:6.1.3"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")

        if (name != "shared") {
            add("implementation", project(":modules:library:tests:shared"))
        }
    }
}
