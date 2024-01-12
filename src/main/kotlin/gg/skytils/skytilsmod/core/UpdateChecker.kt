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
package gg.skytils.skytilsmod.core

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.client
import gg.skytils.skytilsmod.gui.RequestUpdateGui
import gg.skytils.skytilsmod.gui.UpdateGui
import gg.skytils.skytilsmod.utils.GithubRelease
import gg.skytils.skytilsmod.utils.Utils
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.util.Util
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import java.awt.Desktop
import java.io.File

object UpdateChecker {
    val updateGetter = UpdateGetter()
    val updateAsset
        get() = updateGetter.updateObj!!.assets.first { it.name.endsWith(".jar") }
    val updateDownloadURL: String
        get() = updateAsset.downloadUrl

    fun getJarNameFromUrl(url: String): String {
        return url.split(Regex("/")).last()
    }

    fun scheduleCopyUpdateAtShutdown(jarName: String) {
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                println("Attempting to apply Skytils update.")
                val oldJar = Skytils.jarFile
                if (oldJar == null || !oldJar.exists() || oldJar.isDirectory) {
                    println("Old jar file not found.")
                    return@Thread
                }
                println("Copying updated jar to mods.")
                val newJar = File(File(Skytils.modDir, "updates"), jarName)
                println("Copying to mod folder")
                val nameNoExtension = jarName.substringBeforeLast(".")
                val newExtension = jarName.substringAfterLast(".")
                val newLocation = File(
                    oldJar.parent,
                    "${if (oldJar.name.startsWith("!")) "!" else ""}${nameNoExtension}${if (oldJar.endsWith(".temp.jar") && newExtension == oldJar.extension) ".temp.jar" else ".$newExtension"}"
                )
                newLocation.createNewFile()
                newJar.copyTo(newLocation, true)
                newJar.delete()
                if (oldJar.delete()) {
                    println("successfully deleted the files. skipping install tasks")
                    return@Thread
                }
                println("Running delete task")
                val taskFile = File(File(Skytils.modDir, "updates"), "tasks").listFiles()?.last()
                if (taskFile == null) {
                    println("Task doesn't exist")
                    return@Thread
                }
                val runtime = Utils.getJavaRuntime()
                if (Util.getOSType() == Util.EnumOS.OSX) {
                    val sipStatus = Runtime.getRuntime().exec("csrutil status")
                    sipStatus.waitFor()
                    if (!sipStatus.inputStream.use { it.bufferedReader().readText() }
                            .contains("System Integrity Protection status: disabled.")) {
                        println("SIP is NOT disabled, opening Finder.")
                        Desktop.getDesktop().open(oldJar.parentFile)
                        return@Thread
                    }
                }
                println("Using runtime $runtime")
                Runtime.getRuntime().exec("\"$runtime\" -jar \"${taskFile.absolutePath}\" delete \"${oldJar.absolutePath}\"")
                println("Successfully applied Skytils update.")
            } catch (ex: Throwable) {
                println("Failed to apply Skytils Update.")
                ex.printStackTrace()
            }
        })
    }

    fun downloadDeleteTask() {
        Skytils.IO.launch {
            println("Checking for Skytils install task...")
            val taskDir = File(File(Skytils.modDir, "updates"), "tasks")
            // TODO Make this dynamic and fetch latest one or something
            val url =
                "https://github.com/Skytils/SkytilsMod-Data/releases/download/files/SkytilsInstaller-1.2.0.jar"
            val taskFile = File(taskDir, getJarNameFromUrl(url))
            if (taskDir.mkdirs() || withContext(Dispatchers.IO) {
                    taskFile.createNewFile()
                }) {
                println("Downloading Skytils delete task.")
                val req = client.get(url)
                if (req.status != HttpStatusCode.OK) {
                    println("Downloading delete task failed!")
                } else {
                    println("Writing Skytils delete task.")
                    req.bodyAsChannel().copyAndClose(taskFile.writeChannel())
                    println("Delete task successfully downloaded!")
                }
            }
        }
    }

    init {
        try {
            Skytils.IO.launch {
                updateGetter.run()
            }
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiOpen(e: GuiOpenEvent) {
        if (e.gui !is GuiMainMenu) return
        if (updateGetter.updateObj == null) return
        if (UpdateGui.complete) return
        Skytils.displayScreen = RequestUpdateGui()
    }

    class UpdateGetter {
        @Volatile
        var updateObj: GithubRelease? = null

        suspend fun run() {
            println("Checking for updates...")
            val latestRelease = when (Skytils.config.updateChannel) {
                2 -> client.get(
                    "https://api.github.com/repos/Skytils/SkytilsMod/releases/latest"
                ).body()

                1 -> client.get(
                    "https://api.github.com/repos/Skytils/SkytilsMod/releases"
                ).body<List<GithubRelease>>().maxBy { SkytilsVersion(it.tagName.substringAfter("v")) }

                else -> return println("Update Channel set as none")
            }
            val latestTag = latestRelease.tagName
            val currentTag = Skytils.VERSION.substringBefore("-dev")

            val currentVersion = SkytilsVersion(currentTag)
            val latestVersion = SkytilsVersion(latestTag.substringAfter("v"))
            if (currentVersion < latestVersion) {
                updateObj = latestRelease
            }
        }
    }

    class SkytilsVersion(val versionString: String) : Comparable<SkytilsVersion> {

        companion object {
            val regex = Regex("^(?<version>[\\d.]+)-?(?<type>\\D+)?(?<typever>\\d+\\.?\\d*)?\$")
        }

        private val matched by lazy { regex.find(versionString) }
        val isSafe by lazy { matched != null }

        val version by lazy { matched!!.groups["version"]!!.value }
        val versionArtifact by lazy { DefaultArtifactVersion(matched!!.groups["version"]!!.value) }
        val specialVersionType by lazy {
            val typeString = matched!!.groups["type"]?.value ?: return@lazy UpdateType.RELEASE

            return@lazy UpdateType.entries.find { typeString == it.prefix } ?: UpdateType.UNKNOWN
        }
        val specialVersion by lazy {
            if (specialVersionType == UpdateType.RELEASE) return@lazy null
            return@lazy matched!!.groups["typever"]?.value?.toDoubleOrNull()
        }

        private val stringForm by lazy {
            "SkytilsVersion(versionString='$versionString', isSafe=$isSafe, version='$version', versionArtifact=$versionArtifact, specialVersionType=$specialVersionType, specialVersion=$specialVersion)"
        }

        override fun compareTo(other: SkytilsVersion): Int {
            if (!isSafe) return Int.MAX_VALUE
            if (!other.isSafe) return Int.MIN_VALUE
            return if (versionArtifact.compareTo(other.versionArtifact) == 0) {
                if (specialVersionType.ordinal == other.specialVersionType.ordinal) {
                    (specialVersion ?: 0.0).compareTo(other.specialVersion ?: 0.0)
                } else other.specialVersionType.ordinal - specialVersionType.ordinal
            } else versionArtifact.compareTo(other.versionArtifact)
        }

        override fun toString(): String = stringForm

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SkytilsVersion) return false

            return versionString == other.versionString
        }

        override fun hashCode(): Int {
            return versionString.hashCode()
        }
    }

    enum class UpdateType(val prefix: String) {
        UNKNOWN("unknown"),
        RELEASE(""),
        RELEASECANDIDATE("RC"),
        PRERELEASE("pre"),
    }
}