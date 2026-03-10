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
    maven("https://repo.hypixel.net/repository/Hypixel")
}

val relocated: Configuration by configurations.creating

dependencies {
    modImplementation(libs.flk)
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
    modImplementation("net.fabricmc.fabric-api:fabric-rendering-v1")
    include(modImplementation(libs.partnermodintegration.get())!!)
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
