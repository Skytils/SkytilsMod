/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
import net.minecraftforge.gradle.user.IReobfuscator
import net.minecraftforge.gradle.user.ReobfMappingType.SEARGE
import net.minecraftforge.gradle.user.TaskSingleReobf
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("net.minecraftforge.gradle.forge") version "6f5327"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.spongepowered.mixin") version "d5f9873d60"
    java
}

version = "1.0.8-pre1"
group = "skytils.skytilsmod"

minecraft {
    version = "1.8.9-11.15.1.2318-1.8.9"
    runDir = "run"
    mappings = "stable_22"
    makeObfSourceJar = false
    isGitVersion = false
    clientJvmArgs + arrayOf(
        "-Dfml.coreMods.load=skytils.skytilsmod.tweaker.SkytilsLoadingPlugin",
        "-Delementa.dev=true",
        "-Delementa.debug=true",
        "-Dasmhelper.verbose=true"
    )
    clientRunArgs + arrayOf(
        "--tweakClass skytils.skytilsmod.tweaker.SkytilsTweaker",
        "--mixin mixins.skytils.json"
    )
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-releases/")
    maven("https://jitpack.io")
}

val shadowMe: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")

    shadowMe("gg.essential:loader-launchwrapper:1.1.2")
    implementation("gg.essential:essential-1.8.9-forge:1541") {
        exclude(module = "asm")
        exclude(module = "asm-commons")
        exclude(module = "asm-tree")
        exclude(module = "gson")
    }

    shadowMe("org.apache.httpcomponents.client5:httpclient5:5.1.1")
    shadowMe("com.github.Skytils:Hylin:a9899c8c03") {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-coroutines-core")
    }
    shadowMe("com.github.Skytils:AsmHelper:91ecc2bd9c") {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-coroutines-core")
    }
}

mixin {
    disableRefMapWarning = true
    defaultObfuscationEnv = searge
    add(sourceSets.main.get(), "mixins.skytils.refmap.json")
}

sourceSets {
    main {
        ext["refmap"] = "mixins.skytils.refmap.json"
        output.setResourcesDir(file("${buildDir}/classes/kotlin/main"))
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("mcversion", project.minecraft.version)

        filesMatching("mcmod.info") {
            expand(mapOf("version" to project.version, "mcversion" to project.minecraft.version))
        }
    }
    named<Jar>("jar") {
        archiveBaseName.set("Skytils")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "SkytilsInstallerFrame",
                    "FMLCorePlugin" to "skytils.skytilsmod.tweaker.SkytilsLoadingPlugin",
                    "FMLCorePluginContainsFMLMod" to true,
                    "ForceLoadAsMod" to true,
                    "MixinConfigs" to "mixins.skytils.json",
                    "ModSide" to "CLIENT",
                    "TweakClass" to "skytils.skytilsmod.tweaker.SkytilsTweaker",
                    "TweakOrder" to "0"
                )
            )
        }
        enabled = false
    }
    named<ShadowJar>("shadowJar") {
        archiveFileName.set(jar.get().archiveFileName)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowMe)

        relocate("org.apache.hc", "skytils.apacheorg.hc")
        relocate("org.apache.commons.codec", "skytils.apacheorg.commons.codec")
        relocate("dev.falsehonesty.asmhelper", "skytils.asmhelper")

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
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
    named<TaskSingleReobf>("reobfJar") {
        dependsOn(shadowJar)
    }
}

configure<NamedDomainObjectContainer<IReobfuscator>> {
    create("shadowJar") {
        mappingType = SEARGE
        classpath = sourceSets.main.get().compileClasspath
    }
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
