/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.security.MessageDigest

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.kyori.blossom") version "2.1.0"
    id("io.github.juuxel.loom-vineflower") version "1.11.0"
    id("gg.essential.loom") version "1.3.12"
    id("gg.essential.defaults") version "0.3.0"
    idea
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"

    signing
}

version = "1.9.7"
group = "gg.skytils"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.sk1er.club/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-releases/")
    maven("https://jitpack.io") {
        mavenContent {
            includeGroupAndSubgroups("com.github")
        }
    }
}

vineflower {
    toolVersion.set("1.9.3")
}

loom {
    silentMojangMappingsLicense()
    runConfigs {
        getByName("client") {
            isIdeConfigGenerated = true
            property("fml.coreMods.load", "gg.skytils.skytilsmod.tweaker.SkytilsLoadingPlugin")
            property("elementa.dev", "true")
            property("elementa.debug", "true")
            property("elementa.invalid_usage", "warn")
            property("asmhelper.verbose", "true")
            property("mixin.debug.verbose", "true")
            property("mixin.debug.export", "true")
            property("mixin.dumpTargetOnFailure", "true")
            property("fml.debugAccessTransformer", "true")
            property("legacy.debugClassLoading", "true")
            property("legacy.debugClassLoadingSave", "true")
            property("legacy.debugClassLoadingFiner", "true")
            programArgs("--tweakClass", "gg.skytils.skytilsmod.tweaker.SkytilsTweaker")
            programArgs("--mixin", "mixins.skytils.json")
            programArgs("--mixin", "mixins.skytils-events.json")
        }
        remove(getByName("server"))
    }
    forge {
        mixinConfig("mixins.skytils.json", "mixins.skytils-events.json")
    }
    mixin {
        defaultRefmapName = "mixins.skytils.refmap.json"
    }
}

val shadowMe: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val shadowMeMod: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

dependencies {
    shadowMe("gg.essential:loader-launchwrapper:1.2.2")
    implementation("gg.essential:essential-1.8.9-forge:16425+g3a090c5c88") {
        exclude(module = "asm")
        exclude(module = "asm-commons")
        exclude(module = "asm-tree")
        exclude(module = "gson")
        exclude(module = "vigilance")
    }
    shadowMe("com.github.Skytils.Vigilance:vigilance-1.8.9-forge:afb0909442") {
        isTransitive = false
    }

    shadowMeMod("com.github.Skytils:AsmHelper:91ecc2bd9c") {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-coroutines-core")
    }

    shadowMe(platform(kotlin("bom")))
    shadowMe(platform(ktor("bom", "2.3.9", addSuffix = false)))

    shadowMe(ktor("serialization-kotlinx-json"))

    shadowMe("org.jetbrains.kotlinx:kotlinx-serialization-json") {
        version {
            strictly("[1.5.1,)")
            prefer("1.6.2")
        }
    }

    shadowMe(ktorClient("core"))
    shadowMe(ktorClient("cio"))
    shadowMe(ktorClient("content-negotiation"))
    shadowMe(ktorClient("encoding"))

    shadowMe(ktorServer("core"))
    shadowMe(ktorServer("cio"))
    shadowMe(ktorServer("content-negotiation"))
    shadowMe(ktorServer("compression"))
    shadowMe(ktorServer("cors"))
    shadowMe(ktorServer("conditional-headers"))
    shadowMe(ktorServer("auto-head-response"))
    shadowMe(ktorServer("default-headers"))
    shadowMe(ktorServer("host-common"))
    shadowMe(ktorServer("auth"))

    shadowMe("org.brotli:dec:0.1.2")
    shadowMe("com.aayushatharva.brotli4j:brotli4j:1.16.0")

    shadowMe(project(":events"))
    shadowMe(project(":hypixel-api:types"))

    shadowMe("org.bouncycastle:bcpg-jdk18on:1.78.1") {
        exclude(module = "bcprov-jdk18on")
    }
    compileOnly("org.bouncycastle:bcprov-jdk18on:1.78.1")

    shadowMe(annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")!!)
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

sourceSets {
    main {
        output.setResourcesDir(kotlin.classesDirectory)
        blossom {
            javaSources {
                property("version", project.version.toString())
            }
            resources {
                property("version", project.version.toString())
                property("mcversion", "1.8.9")
            }
        }
    }
}

tasks {
    processResources {
        dependsOn(compileJava)
    }
    named<Jar>("jar") {
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "SkytilsInstallerFrame",
                    "FMLCorePlugin" to "gg.skytils.skytilsmod.tweaker.SkytilsLoadingPlugin",
                    "FMLCorePluginContainsFMLMod" to true,
                    "ForceLoadAsMod" to true,
                    "MixinConfigs" to "mixins.skytils.json,mixins.skytils-events.json",
                    "ModSide" to "CLIENT",
                    "ModType" to "FML",
                    "TweakClass" to "gg.skytils.skytilsmod.tweaker.SkytilsTweaker",
                    "TweakOrder" to "0"
                )
            )
        }
        dependsOn(shadowJar)
        enabled = false
    }
    named<RemapJarTask>("remapJar") {
        archiveBaseName.set("Skytils")
        inputFile.set(shadowJar.get().archiveFile)
        doLast {
            MessageDigest.getInstance("SHA-256").digest(archiveFile.get().asFile.readBytes())
                .let {
                    println("SHA-256: " + it.joinToString(separator = "") { "%02x".format(it) }.uppercase())
                }
        }
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("Skytils")
        archiveClassifier.set("dev")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowMe, shadowMeMod)

        relocate("dev.falsehonesty.asmhelper", "gg.skytils.asmhelper")
        relocate("com.llamalad7.mixinextras", "gg.skytils.mixinextras")
        relocate("io.ktor", "gg.skytils.ktor")
        relocate("kotlinx.serialization", "gg.skytils.ktx-serialization")
        relocate("kotlinx.coroutines", "gg.skytils.ktx-coroutines")
        relocate("gg.essential.vigilance", "gg.skytils.vigilance")

        exclude(
            "**/LICENSE_MixinExtras",
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
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs =
                listOf(
                    /*"-opt-in=kotlin.RequiresOptIn", */
                    "-Xjvm-default=all",
                    //"-Xjdk-release=1.8",
                    "-Xbackend-threads=0",
                    /*"-Xuse-k2"*/
                )
            languageVersion = "1.9"
        }
        kotlinDaemonJvmArguments.set(
            listOf(
                "-Xmx2G",
                "-Dkotlin.enableCacheBuilding=true",
                "-Dkotlin.useParallelTasks=true",
                "-Dkotlin.enableFastIncremental=true",
                //"-Xbackend-threads=0"
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
    jvmToolchain(8)
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(tasks["remapJar"])
    }
}

/**
 * Builds the dependency notation for the named Ktor [module] at the given [version].
 *
 * @param module simple name of the Ktor module, for example "client-core".
 * @param version optional desired version, unspecified if null.
 */
fun DependencyHandler.ktor(module: String, version: String? = null, addSuffix: Boolean = true) =
    "io.ktor:ktor-$module${if (addSuffix) "-jvm" else ""}${version?.let { ":$version" } ?: ""}"

fun DependencyHandler.ktorClient(module: String, version: String? = null) = ktor("client-${module}", version)

fun DependencyHandler.ktorServer(module: String, version: String? = null) = ktor("server-${module}", version)