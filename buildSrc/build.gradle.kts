plugins {
    java
    `java-gradle-plugin`
    `kotlin-dsl`
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
kotlin {
    jvmToolchain(8)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(gradleApi())
    api("com.google.code.gson:gson:2.10.1")

    api("org.apache.httpcomponents:httpclient:4.5.13")
    api("org.apache.httpcomponents:httpmime:4.5.13")

    api("com.google.guava:guava:21.0")
}

gradlePlugin {
    testSourceSets(sourceSets.test.get())

    plugins {
        register("maven-publishing") {
            id = "moe.karla.maven-publishing"
            implementationClass = "moe.karla.maven.publishing.MavenPublishingPlugin"

            displayName = "Maven Central Publishing"
            description = "Publishing your software to Maven Central"
        }
    }
}
