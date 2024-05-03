/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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
    id("com.github.johnrengelman.shadow")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    idea
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"

    signing
}

group = "gg.skytils"

val isLegacyFabric = project.platform.isFabric && project.platform.mcVersion == 10809

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
    if (isLegacyFabric) maven("https://repo.legacyfabric.net/repository/legacyfabric/")
}

loom {
    if (isLegacyFabric) {
        intermediaryUrl.set("https://repo.legacyfabric.net/repository/legacyfabric/net/legacyfabric/v2/intermediary/%1\$s/intermediary-%1\$s-v2.jar")
    }
    silentMojangMappingsLicense()
    runConfigs {
        getByName("client") {
            isIdeConfigGenerated = true
            property("elementa.dev", "true")
            property("elementa.debug", "true")
            property("elementa.invalid_usage", "warn")
            property("asmhelper.verbose", "true")
            property("mixin.debug.verbose", "true")
            property("mixin.debug.export", "true")
            property("mixin.dumpTargetOnFailure", "true")

            if (project.platform.isLegacyForge) {
                property("fml.coreMods.load", "gg.skytils.skytilsmod.tweaker.SkytilsLoadingPlugin")
                property("legacy.debugClassLoading", "true")
                property("legacy.debugClassLoadingSave", "true")
                property("legacy.debugClassLoadingFiner", "true")
                programArgs("--tweakClass", "gg.skytils.skytilsmod.tweaker.SkytilsTweaker")
            }
            if (project.platform.isForge) {
                property("fml.debugAccessTransformer", "true")

                programArgs("--mixin", "mixins.skytils.json")
                programArgs("--mixin", "mixins.skytils-events.json")
            }
        }
        remove(getByName("server"))
    }
    if (project.platform.isForge) {
        forge {
            mixinConfig("mixins.skytils.json", "mixins.skytils-events.json")
        }
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
    if (platform.isForge) {
        shadowMe("gg.essential:loader-launchwrapper:1.2.2")
    } else {
        runtimeOnly("gg.essential:loader-fabric:1.0.0")
    }
    implementation("gg.essential:essential-${if (!isLegacyFabric) platform.toString() else "${platform.mcVersionStr}-forge"}:16425+g3a090c5c88") {
        exclude(module = "asm")
        exclude(module = "asm-commons")
        exclude(module = "asm-tree")
        exclude(module = "gson")
        exclude(module = "vigilance")
    }
    shadowMe("com.github.Skytils.Vigilance:vigilance-${if (!isLegacyFabric) if (platform.mcVersion >= 11801) "1.18.1-${platform.loaderStr}" else platform.toString() else "${platform.mcVersionStr}-forge"}:afb0909442") {
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

    shadowMe("gg.skytils:events")
    shadowMe("gg.skytils.hypixel.types:types")

    shadowMe(annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")!!)
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

sourceSets {
    main {
        output.setResourcesDir(kotlin.classesDirectory)
    }
}

val javaVersion = if (platform.isLegacyForge) JavaVersion.VERSION_1_8 else JavaVersion.VERSION_17

val enabledVersions = setOf(
    "1.8.9-forge",
    "1.20.4-fabric"
)

tasks {
    build {
        if (platform.mcVersionStr !in enabledVersions) {
            enabled = false
        }
    }
    processResources {
        dependsOn(compileJava)
        filesMatching("mcmod.info") {
            expand(mapOf(
                "version" to version,
                "mcversion" to platform.mcVersionStr
            ))
        }
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
            "META-INF/com.android.tools/**"
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
            jvmTarget = javaVersion.toString()
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
    if (platform.isLegacyForge) {
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
}

kotlin {
    jvmToolchain {
        check(this is JavaToolchainSpec)
        languageVersion.set(JavaLanguageVersion.of(javaVersion.asInt()))
    }
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

fun JavaVersion.asInt() = this.ordinal + 1