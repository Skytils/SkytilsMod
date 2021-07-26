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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import net.minecraftforge.gradle.user.IReobfuscator
import net.minecraftforge.gradle.user.TaskSingleReobf
import net.minecraftforge.gradle.user.ReobfMappingType.SEARGE

plugins {
    id("net.minecraftforge.gradle.forge") version "6f5327"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("org.spongepowered.mixin") version "d5f9873d60"
}

version = "1.0-pre19.1"
group = "skytils.skytilsmod"

minecraft {
    version = "1.8.9-11.15.1.2318-1.8.9"
    runDir = "run"
    mappings = "stable_22"
    makeObfSourceJar = false
    isGitVersion = false
    clientJvmArgs.addAll(setOf(
        "-Dfml.coreMods.load=skytils.skytilsmod.tweaker.SkytilsLoadingPlugin",
        "-Delementa.dev=true",
        "-Delementa.debug=true"
    ))
    clientRunArgs.addAll(setOf(
        "--tweakClass org.spongepowered.asm.launch.MixinTweaker",
        "--mixin mixins.skytils.json"
    ))
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    maven { url = uri("https://repo.sk1er.club/repository/maven-public/") }
    maven { url = uri("https://repo.sk1er.club/repository/maven-releases/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }

    annotationProcessor("org.spongepowered:mixin:0.7.11-SNAPSHOT")

    implementation("gg.essential:vigilance-1.8.9-forge:154") {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib-jdk8")
    }

    implementation("org.apache.httpcomponents.client5:httpclient5:5.1")
    implementation("com.github.Skytils:Hylin:6e070f7fde") {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlinx-coroutines-core")
    }

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
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
                    "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                    "TweakOrder" to "0"
                )
            )
        }
        enabled = false
    }
    named<ShadowJar>("shadowJar") {
        archiveFileName.set(jar.get().archiveFileName)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        relocate("gg.essential", "skytils.essentialgg")
        relocate("kotlinx.coroutines", "skytils.kotlinx.coroutines")
        relocate("org.apache.hc", "skytils.apacheorg.hc")
        relocate("org.apache.commons.codec", "skytils.apacheorg.commons.codec")

        exclude("**/LICENSE.md")
        exclude("**/LICENSE.txt")
        exclude("**/LICENSE")
        exclude("**/NOTICE")
        exclude("**/NOTICE.txt")
        exclude("pack.mcmeta")
        exclude("dummyThing")
        exclude("**/module-info.class")

        exclude("META-INF/proguard/**")
        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")
        exclude("META-INF/com.android.tools/**")

        exclude("fabric.mod.json")
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