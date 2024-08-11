plugins {
    java
    `maven-publish`
}

group = "top.mrxiaom"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")

    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.zaxxer:HikariCP:4.0.3")

    implementation("org.jetbrains:annotations:21.0.0")
}
val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }
    register<Jar>("javadocJar") {
        dependsOn(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc.get().destinationDir)
    }
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            links("https://docs.oracle.com/javase/8/docs/api/")
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
            artifactId = rootProject.name
            version = project.version.toString()

            artifact(tasks.getByName("javadocJar"))
        }
    }
}