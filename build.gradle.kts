import moe.karla.maven.publishing.MavenPublishingExtension.PublishingType

plugins {
    id("moe.karla.maven-publishing")
    id("com.github.gmazzo.buildconfig") version "5.6.7" apply false
    id("com.gradleup.shadow") version "8.3.0" apply false
}

allprojects {
    group = "top.mrxiaom"
    version = "1.6.7"

    repositories {
        mavenCentral()
        maven("https://repo.codemc.io/repository/maven-public/")

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.helpch.at/releases/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://jitpack.io/")
    }
}

mavenPublishing {
    publishingType = PublishingType.AUTOMATIC
}
