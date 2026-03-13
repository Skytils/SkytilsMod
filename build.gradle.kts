import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.security.MessageDigest

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.egt.defaults)
    alias(libs.plugins.egt.loom)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    signing
}

repositories {
    mavenCentral()
    maven("https://repo.essential.gg/repository/maven-public/")
    maven("https://repo.essential.gg/repository/maven-releases/")
    maven("https://repo.hypixel.net/repository/Hypixel")
    maven("https://jitpack.io") {
        mavenContent {
            includeGroupAndSubgroups("com.github")
        }
    }
    maven("https://maven.dediamondpro.dev/releases") {
        mavenContent {
            includeGroup("dev.dediamondpro")
        }
    }
}

val relocated: Configuration by configurations.creating

dependencies {
    modImplementation(libs.flk)
    include(modRuntimeOnly("gg.essential:loader-fabric:1.2.3")!!)
    // hack to return non null dependency
    include(implementation(libs.elementa.asProvider().get())!!)
    relocated(implementation(libs.elementa.layoutdsl.get())!!)
    include(implementation(libs.vigilance.get())!!)
    include(modImplementation(libs.universalcraft.get())!!)
    implementation(project(":events", configuration = "namedElements"))
    relocated(project(":events")) {
        isTransitive = false
    }
    include(implementation(libs.hypixelmodapi.get())!!)
    include(modImplementation(libs.cloud.fabric.get())!!)
    include(implementation(libs.cloud.annotaitons.get())!!)
    modImplementation(libs.bundles.fabricapi)
    include(modImplementation(libs.partnermodintegration.get())!!)
    include(implementation("org.brotli:dec:0.1.2")!!)
    include(implementation("io.ktor:ktor-client-core-jvm:2.3.13")!!)
    include(implementation("io.ktor:ktor-client-cio-jvm:2.3.13")!!)
    include(implementation("io.ktor:ktor-client-content-negotiation-jvm:2.3.13")!!)
    include(implementation("io.ktor:ktor-client-encoding-jvm:2.3.13")!!)
    include(implementation("io.ktor:ktor-client-websockets-jvm:2.3.13")!!)
    include(implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.13")!!)
    include(implementation("io.ktor:ktor-serialization-kotlinx-jvm:2.3.13")!!)
    include(implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf-jvm:1.9.0")!!)
    include(implementation("io.github.llamalad7:mixinextras-fabric:0.5.0-rc.1")!!)
    annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.5.0-rc.1")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to project.version,
                "flk" to ">=${libs.versions.flk.get()}"
            ))
        }
    }

    remapJar {
        archiveBaseName.set(shadowJar.flatMap(ShadowJar::getArchiveBaseName))
        inputFile.set(shadowJar.flatMap(ShadowJar::getArchiveFile))
        doLast {
            MessageDigest.getInstance("SHA-256").digest(archiveFile.get().asFile.readBytes())
                .let {
                    println("SHA-256: " + it.joinToString(separator = "") { "%02x".format(it) }.uppercase())
                }
        }
    }

    shadowJar {
        archiveBaseName.set(loom.minecraftVersion.map { "Skytils-$it" })
        configurations = listOf(relocated)

        relocate("gg.essential.elementa.unstable", "gg.skytils.elementa.unstable")

        exclude(
            "META-INF/maven/**",
        )
    }

    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(tasks["remapJar"])
    }
}
