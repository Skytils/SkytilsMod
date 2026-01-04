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
import gg.essential.gradle.util.noServerRunConfigs
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.security.MessageDigest

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("gg.essential.defaults")
    id("gg.essential.multi-version")
    signing
}

group = "gg.skytils"

val isLegacyFabric = project.platform.isFabric && project.platform.mcVersion == 10809

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.essential.gg/repository/maven-public/")
    maven("https://repo.essential.gg/repository/maven-releases/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.hypixel.net/repository/Hypixel/")
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
    if (isLegacyFabric) maven("https://repo.legacyfabric.net/repository/legacyfabric/")
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

loom {
    if (isLegacyFabric) {
        intermediaryUrl.set("https://repo.legacyfabric.net/repository/legacyfabric/net/legacyfabric/v2/intermediary/%1\$s/intermediary-%1\$s-v2.jar")
    }
    noServerRunConfigs()
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
                programArgs("--mixin", "mixins.skytils-init.json")
            }
        }
        remove(getByName("server"))
    }
    if (project.platform.isForge) {
        forge {
            mixinConfig("mixins.skytils.json", "mixins.skytils-events.json", "mixins.skytils-init.json")
        }
    }
    mixin {
        defaultRefmapName = "mixins.skytils.refmap.json"
    }
}

val include: Configuration = if (platform.isLegacyForge) {
    val config: Configuration by configurations.creating {
        configurations.implementation.get().extendsFrom(this)
    }
    config
} else {
    configurations.include.get()
}

val relocated: Configuration by configurations.creating

dependencies {
    if (platform.isForge) {
        include("gg.essential:loader-launchwrapper:1.2.3")
    } else {
        include(modRuntimeOnly("gg.essential:loader-fabric:1.2.5")!!)
        val fapiVersion = when (platform.mcVersion) {
            12105 -> "0.128.2+1.21.5"
            12110 -> "0.138.4+1.21.10"
            12111 -> "0.140.2+1.21.11"
            else -> error("No fabric api version configured")
        }
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion") {
            exclude(module = "fabric-content-registries-v0")
            exclude(module = "fabric-rendering-fluids-v1")
            exclude(module = "fabric-transfer-api-v1")
        }
    }
    modCompileOnly("gg.essential:essential-${if (platform.mcVersion >= 12006) "1.20.6-fabric" else if (!isLegacyFabric) platform.toString() else "${platform.mcVersionStr}-forge"}:17141+gd6f4cfd3a8") {
        exclude(module = "asm")
        exclude(module = "asm-commons")
        exclude(module = "asm-tree")
        exclude(module = "gson")
        exclude(module = "kotlinx-coroutines-core-jvm")
        exclude(module = "universalcraft-1.20.6-fabric")
    }
    include(implementation("gg.essential:vigilance:312") {
        isTransitive = false
    })
    modCompileOnly("gg.essential:universalcraft-${if (platform.mcVersion == 12110) "1.21.9-fabric" else platform}:446")
    relocated(implementation("gg.essential:elementa-unstable-layoutdsl:710") {
        excludeKotlin()
        exclude(module = "fabric-loader")
    })

    include(implementation("dev.dediamondpro:minemark-elementa:1.2.3") {
        excludeKotlin()
        exclude(module = "elementa-1.8.9-forge")
    })

    include(modImplementation("com.github.Skytils:AsmHelper:91ecc2bd9c") {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-coroutines-core")
    })

    relocated(implementation(platform(kotlin("bom")))!!)
    relocated(implementation(platform(ktor("bom", "2.3.13", addSuffix = false)))!!)

    relocated(implementation(ktor("serialization-kotlinx-json")) { excludeKotlin() })

    relocated(implementation("org.jetbrains.kotlinx:kotlinx-serialization-json") {
        version {
            strictly("[1.5.1,)")
            prefer("1.6.2")
        }
        excludeKotlin()
    })

    relocated(implementation(ktorClient("core")) { excludeKotlin() })
    relocated(implementation(ktorClient("cio")) { excludeKotlin() })
    relocated(implementation(ktorClient("content-negotiation")) { excludeKotlin() })
    relocated(implementation(ktorClient("encoding")) { excludeKotlin() })

    relocated(implementation(ktorServer("core")) { excludeKotlin() })
    relocated(implementation(ktorServer("cio")) { excludeKotlin() })
    relocated(implementation(ktorServer("content-negotiation")) { excludeKotlin() })
    relocated(implementation(ktorServer("compression")) { excludeKotlin() })
    relocated(implementation(ktorServer("cors")) { excludeKotlin() })
    relocated(implementation(ktorServer("conditional-headers")) { excludeKotlin() })
    relocated(implementation(ktorServer("auto-head-response")) { excludeKotlin() })
    relocated(implementation(ktorServer("default-headers")) { excludeKotlin() })
    relocated(implementation(ktorServer("host-common")) { excludeKotlin() })
    relocated(implementation(ktorServer("auth")) { excludeKotlin() })

    include(implementation("org.brotli:dec:0.1.2")!!)
    include(implementation("com.aayushatharva.brotli4j:brotli4j:1.18.0")!!)

    implementation(project(":events:$platform", configuration = "namedElements"))
    relocated(project(":events:$platform")) {
        excludeKotlin()
        exclude(module = "fabric-loader")
    }
    relocated(implementation(project(":vigilance")) {
        isTransitive = false
        excludeKotlin()
        exclude(module = "fabric-loader")
    })
    relocated(implementation("gg.skytils.hypixel.types:types") { excludeKotlin() })
    relocated(implementation("gg.skytils.skytilsws.shared:ws-shared") { excludeKotlin() })

    include(implementation("org.bouncycastle:bcpg-jdk18on:1.78.1") {
        exclude(module = "bcprov-jdk18on")
    })
    compileOnly("org.bouncycastle:bcprov-jdk18on:1.78.1")

    if (platform.isFabric && !isLegacyFabric) {
        modImplementation("org.incendo:cloud-fabric:2.0.0-beta.10")
        include("org.incendo:cloud-fabric:2.0.0-beta.10")
        include(implementation("org.incendo:cloud-annotations:2.0.0")!!)
        modLocalRuntime("net.fabricmc:fabric-language-kotlin:1.13.3+kotlin.2.1.21")
    }
    annotationProcessor(
        include(implementation("org.incendo:cloud-kotlin-coroutines-annotations:2.0.0") { excludeKotlin() })!!
    )
    include(implementation("org.incendo:cloud-kotlin-extensions:2.0.0") { excludeKotlin() })

    if (platform.isLegacyForge) {
        compileOnly("net.hypixel:mod-api-forge:1.0.1.2") {
            exclude(group = "me.djtheredstoner", module = "DevAuth-forge-legacy")
        }
        relocated(include(implementation("net.hypixel:mod-api-forge-tweaker:1.0.1.2")!!)!!)
    } else {
        modImplementation("net.hypixel:mod-api:1.0.1")
        include(modImplementation("maven.modrinth:hypixel-mod-api:1.0.1+build.1+mc1.21")!!)
    }

    val mixinExtrasVersion = "0.5.2"
    if (platform.isFabric) {
        include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${mixinExtrasVersion}")!!)!!)
    } else {
        relocated(implementation(annotationProcessor("io.github.llamalad7:mixinextras-common:${mixinExtrasVersion}")!!)!!)
    }
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

sourceSets {
    main {
        output.setResourcesDir(kotlin.classesDirectory)
    }
}

val enabledVersions = setOf(
    "1.8.9-forge",
    "1.21.5-fabric"
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
                    "MixinConfigs" to "mixins.skytils.json,mixins.skytils-events.json,mixins.skytils-init.json",
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
        archiveBaseName.set(shadowJar.flatMap(ShadowJar::getArchiveBaseName))
        inputFile.set(shadowJar.flatMap(ShadowJar::getArchiveFile))
        doLast {
            MessageDigest.getInstance("SHA-256").digest(archiveFile.get().asFile.readBytes())
                .let {
                    println("SHA-256: " + it.joinToString(separator = "") { "%02x".format(it) }.uppercase())
                }
        }
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("Skytils-${platform}")
        archiveClassifier.set("dev")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = if (platform.isLegacyForge) listOf(include, relocated) else listOf(relocated)

        relocate("dev.falsehonesty.asmhelper", "gg.skytils.asmhelper")
        relocate("io.ktor", "gg.skytils.ktor")
        relocate("kotlinx.serialization", "gg.skytils.ktx-serialization")
        relocate("gg.essential.elementa.unstable", "gg.skytils.elementa.unstable")
        relocate("net.hypixel.modapi.tweaker", "gg.skytils.hypixel-net.modapi.tweaker")

        if (platform.isLegacyForge) {
            relocate("com.llamalad7.mixinextras", "gg.skytils.mixinextras")
            relocate("kotlinx.coroutines", "gg.skytils.ktx-coroutines")
        } else {
            exclude("kotlinx/coroutines/**")
        }

        exclude(
            "**/LICENSE_MixinExtras",
            "**/LICENSE.md",
            "**/LICENSE.txt",
            "**/LICENSE",
            "**/NOTICE",
            "**/NOTICE.txt",
            "dummyThing",
            "**/module-info.class",
            "META-INF/proguard/**",
            "META-INF/maven/**",
            "META-INF/versions/**",
            "META-INF/com.android.tools/**"
        )
        if (platform.isFabric) {
            exclude("**/mcmod.info")
        } else {
            exclude("**/fabric.mod.json")
        }
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
            jvmTarget = platform.javaVersion.toString()
            freeCompilerArgs =
                listOf(
                    /*"-opt-in=kotlin.RequiresOptIn", */
                    "-Xjvm-default=all",
                    //"-Xjdk-release=1.8",
                    // "-Xbackend-threads=0",
                    /*"-Xuse-k2"*/
                )
            languageVersion = "2.0"
            apiVersion = "2.0"
        }
        kotlinDaemonJvmArguments.set(
            listOf(
                "-Xmx2G",
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
        languageVersion.set(JavaLanguageVersion.of(platform.javaVersion.asInt()))
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

fun <T : ModuleDependency> T.excludeKotlin(): T {
    exclude(group = "org.jetbrains.kotlin")
    // exclude(module = "kotlinx-coroutines-core")
    return this
}