pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net/")
        maven("https://repo.essential.gg/repository/maven-public/")
        maven("https://repo.essential.gg/repository/maven-releases/")
        maven("https://jitpack.io") {
            mavenContent {
                includeGroupAndSubgroups("com.github")
            }
        }
    }
}

include(":events")
project(":events").apply {
    projectDir = file("events")
}
