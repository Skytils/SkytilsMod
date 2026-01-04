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
import gg.essential.gradle.util.noRunConfigs
import org.apache.tools.ant.filters.FixCrLfFilter

plugins {
    kotlin("jvm")
    id("gg.essential.defaults")
    id("gg.essential.multi-version")
}

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
    if (project.platform.isFabric && project.platform.mcVersion == 10809) maven("https://repo.legacyfabric.net/repository/legacyfabric/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0-beta.1")!!)
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

group = "gg.skytils.events"

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(platform.javaVersion.asInt()))
}

loom.mixin {
    defaultRefmapName = "mixins.skytils-events.refmap.json"
}

loom.noRunConfigs()

tasks.processResources {
    filesMatching("**/*.json") {
        filter(FixCrLfFilter::class, "eol" to FixCrLfFilter.CrLf.newInstance("lf"))
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

fun JavaVersion.asInt() = this.ordinal + 1