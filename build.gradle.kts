/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.pack200.java.Pack200Adapter
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.security.MessageDigest

plugins {
    kotlin("jvm") version "1.6.20-RC"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    java
    idea
    signing
}

version = "1.2.0"
group = "skytils.skytilsmod"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.sk1er.club/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-releases/")
    maven("https://jitpack.io")
}

loom {
    // sup weef overflow how's it going
    silentMojangMappingsLicense()
    launchConfigs {
        getByName("client") {
            property("fml.coreMods.load", "skytils.skytilsmod.tweaker.SkytilsLoadingPlugin")
            property("elementa.dev", "true")
            property("elementa.debug", "true")
            property("elementa.invalid_usage", "warn")
            property("asmhelper.verbose", "true")
            property("mixin.debug.verbose", "true")
            property("mixin.debug.export", "true")
            property("mixin.dumpTargetOnFailure", "true")
            property("legacy.debugClassLoading", "true")
            property("legacy.debugClassLoadingSave", "true")
            property("legacy.debugClassLoadingFiner", "true")
            arg("--tweakClass", "skytils.skytilsmod.tweaker.SkytilsTweaker")
            arg("--mixin", "mixins.skytils.json")
        }
    }
    runConfigs {
        getByName("client") {
            isIdeConfigGenerated = true
        }
    }
    forge {
        pack200Provider.set(Pack200Adapter())
        mixinConfig("mixins.skytils.json")
    }
    mixin {
        defaultRefmapName.set("mixins.skytils.refmap.json")
    }
}

val shadowMe: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val shadowMeMod: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowMe("gg.essential:loader-launchwrapper:1.1.3")
    implementation("gg.essential:essential-1.8.9-forge:2163") {
        exclude(module = "asm")
        exclude(module = "asm-commons")
        exclude(module = "asm-tree")
        exclude(module = "gson")
    }

    shadowMe("org.apache.httpcomponents.client5:httpclient5:5.1.3")
    shadowMeMod("com.github.Skytils:Hylin:04665adf92") {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-coroutines-core")
    }
    shadowMeMod("com.github.Skytils:AsmHelper:91ecc2bd9c") {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-coroutines-core")
    }

    shadowMe("com.github.LlamaLad7:MixinExtras:0.0.6")
    annotationProcessor("com.github.LlamaLad7:MixinExtras:0.0.6")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

sourceSets {
    main {
        output.setResourcesDir(file("${buildDir}/classes/kotlin/main"))
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("mcversion", "1.8.9")

        filesMatching("mcmod.info") {
            expand(mapOf("version" to project.version, "mcversion" to "1.8.9"))
        }
        dependsOn(compileJava)
    }
    named<Jar>("jar") {
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "SkytilsInstallerFrame",
                    "FMLCorePlugin" to "skytils.skytilsmod.tweaker.SkytilsLoadingPlugin",
                    "FMLCorePluginContainsFMLMod" to true,
                    "ForceLoadAsMod" to true,
                    "MixinConfigs" to "mixins.skytils.json",
                    "ModSide" to "CLIENT",
                    "ModType" to "FML",
                    "TweakClass" to "skytils.skytilsmod.tweaker.SkytilsTweaker",
                    "TweakOrder" to "0"
                )
            )
        }
        dependsOn(shadowJar)
        enabled = false
    }
    named<RemapJarTask>("remapJar") {
        archiveBaseName.set("Skytils")
        input.set(shadowJar.get().archiveFile)
        doLast {
            MessageDigest.getInstance("SHA-256").digest(archiveFile.get().asFile.readBytes())
                .let {
                    println("SHA-256: " + it.joinToString(separator = "") { "%02x".format(it) }.toUpperCase())
                }
        }
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("Skytils")
        archiveClassifier.set("dev")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowMe, shadowMeMod)

        relocate("org.apache.hc", "skytils.apacheorg.hc")
        relocate("org.apache.commons.codec", "skytils.apacheorg.codec")
        relocate("dev.falsehonesty.asmhelper", "skytils.asmhelper")
        relocate("com.llamalad7.mixinextras", "skytils.mixinextras")

        exclude(
            "**/LICENSE.md",
            "**/LICENSE.txt",
            "**/LICENSE",
            "**/NOTICE",
            "**/NOTICE.txt",
            "pack.mcmeta",
            "dummyThing",
            "**/module-info.class",
            "META-INF/proguard/**",
            "META-INF/maven/**",
            "META-INF/versions/**",
            "META-INF/com.android.tools/**",
            "fabric.mod.json"
        )
        mergeServiceFiles()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
//        options.release.set(8)
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs =
                listOf("-opt-in=kotlin.RequiresOptIn", "-Xjvm-default=all", "-Xrelease=8", "-Xbackend-threads=0")
            languageVersion = "1.6"
        }
        kotlinDaemonJvmArguments.set(
            listOf(
                "-Xmx2G",
                "-Dkotlin.enableCacheBuilding=true",
                "-Dkotlin.useParallelTasks=true",
                "-Dkotlin.enableFastIncremental=true",
                //               "-Xbackend-threads=0"
            )
        )
    }
    register<Delete>("deleteClassloader") {
        delete(
            "${project.projectDir}/run/CLASSLOADER_TEMP",
            "${project.projectDir}/run/CLASSLOADER_TEMP1",
            "${project.projectDir}/run/CLASSLOADER_TEMP2",
            "${project.projectDir}/run/CLASSLOADER_TEMP3",
            "${project.projectDir}/run/CLASSLOADER_TEMP4",
            "${project.projectDir}/run/CLASSLOADER_TEMP5",
            "${project.projectDir}/run/CLASSLOADER_TEMP6",
            "${project.projectDir}/run/CLASSLOADER_TEMP7",
            "${project.projectDir}/run/CLASSLOADER_TEMP8",
            "${project.projectDir}/run/CLASSLOADER_TEMP9",
            "${project.projectDir}/run/CLASSLOADER_TEMP10"
        )
    }
}

kotlin {
    jvmToolchain {
        check(this is JavaToolchainSpec)
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(tasks["remapJar"])
    }
}