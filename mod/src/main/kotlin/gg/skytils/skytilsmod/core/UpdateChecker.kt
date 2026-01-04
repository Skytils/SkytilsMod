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

import com.sun.jna.Platform
import gg.essential.universal.UDesktop
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.screen.ScreenOpenEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Reference
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.client
import gg.skytils.skytilsmod.gui.updater.RequestUpdateGui
import gg.skytils.skytilsmod.gui.updater.UpdateGui
import gg.skytils.skytilsmod.utils.ModrinthVersion
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
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.MinecraftVersion
import java.io.File
import java.io.PrintStream
import java.nio.file.StandardCopyOption
import kotlin.io.path.*

//#if FABRIC
import net.fabricmc.loader.api.Version
//#else
//$$ import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
//#endif

object UpdateChecker : EventSubscriber {
    val updateGetter = UpdateGetter()
    val updateAsset
        get() = updateGetter.updateObj!!.files.find { it.primary }
    val updateDownloadURL: String
        get() = updateAsset!!.url

    val currentTag = Skytils.VERSION.substringBefore("-dev")
    val currentVersion = SkytilsVersion(currentTag)

    fun getJarNameFromUrl(url: String): String {
        return url.split(Regex("/")).last()
    }

    fun scheduleCopyUpdateAtShutdown(jarName: String) {
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                val logFile = File(Skytils.modDir, "updates/latest.log")
                logFile.writeBytes(byteArrayOf())
                PrintStream(logFile.outputStream(), true).use { logPrintStream ->
                    fun log(message: Any?) {
                        println(message)
                        logPrintStream.println(message)
                    }

                    fun logStackTrace(e: Throwable) {
                        e.printStackTrace()
                        e.printStackTrace(logPrintStream)
                    }

                    try {
                        log("${System.currentTimeMillis()} - Attempting to apply Skytils update.")
                        log("Attempting to apply Skytils update.")
                        val oldJar = Skytils.jarFile
                        if (oldJar == null || !oldJar.exists() || oldJar.isDirectory) {
                            log("Old jar file not found.")
                            return@Thread
                        }
                        log("Copying updated jar to mods.")
                        val newJar = Skytils.modDir.toPath().resolve("updates").resolve(jarName)
                        log("Copying to mod folder")
                        val nameNoExtension = jarName.substringBeforeLast(".")
                        val newExtension = jarName.substringAfterLast(".")
                        val newLocation = Path(
                            oldJar.parent,
                            "${if (oldJar.name.startsWith("!")) "!" else ""}${nameNoExtension}${if (oldJar.endsWith(".temp.jar") && newExtension == oldJar.extension) ".temp.jar" else ".$newExtension"}"
                        )

                        log("New location exists? ${newLocation.exists()}")

                        runCatching {
                            // these options are platform-dependent
                            newJar.moveTo(newLocation, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
                        }.onFailure {
                            logStackTrace(it)
                            log("Atomic move failed. Falling back to non-atomic move.")
                            runCatching {
                                newJar.moveTo(newLocation, StandardCopyOption.REPLACE_EXISTING)
                            }.onFailure {
                                logStackTrace(it)
                                log("Move failed. Falling back to copy.")
                                newJar.copyTo(newLocation, overwrite = true)
                                log("Copy successful.")
                            }
                        }
                        if (oldJar.delete()) {
                            log("successfully deleted the files. skipping install tasks")
                            return@Thread
                        }
                        log("Running delete task")
                        val taskFile = File(File(Skytils.modDir, "updates"), "tasks").listFiles()?.last()
                        if (taskFile == null) {
                            log("Task doesn't exist")
                            return@Thread
                        }
                        val runtime = Utils.getJavaRuntime()

                        if (Platform.isMac()) {
                            val sipStatus = Runtime.getRuntime().exec(arrayOf("csrutil", "status"))
                            sipStatus.waitFor()
                            if (!sipStatus.inputStream.use { it.bufferedReader().readText() }
                                    .contains("System Integrity Protection status: disabled.")) {
                                log("SIP is NOT disabled, opening Finder just in case.")
                                if (ProcessBuilder("open", "-R", oldJar.absolutePath).start().waitFor() != 0) {
                                    log("Failed to use Finder reveal, falling back to Desktop.")
                                    UDesktop.open(oldJar.parentFile)
                                }
                            }
                        }
                        log("Using runtime $runtime")
                        // I think all JVM implementations will auto-escape
                        ProcessBuilder(runtime, "-jar", taskFile.absolutePath, "delete", oldJar.absolutePath)
                            .redirectErrorStream(true)
                            .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
                            .start()
                        log("Successfully launched Skytils update task.")
                    } catch (ex: Throwable) {
                        log("Failed to apply Skytils Update.")
                        logStackTrace(ex)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
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

    fun onGuiOpen(e: ScreenOpenEvent) {
        if (e.screen !is TitleScreen) return
        if (updateGetter.updateObj == null) return
        if (UpdateGui.complete.get()) return
        Skytils.displayScreen = RequestUpdateGui()
    }

    class UpdateGetter {
        @Volatile
        var updateObj: ModrinthVersion? = null

        suspend fun run() {
            println("Checking for updates...")
            val targetChannel = when (Skytils.config.updateChannel) {
                3 -> "alpha"
                2 -> "release"
                1 -> "beta"
                else -> return println("Update Channel set as none")
            }

            val allVersions: List<ModrinthVersion> = client.get("https://data.skytils.gg/updateInfo.json").body()

            val allowedVersions = allVersions.filter { it.versionType >= targetChannel }

            val validUpdateVersions = allowedVersions.filter { "fabric" in it.loaders && MinecraftVersion.create().name in it.gameVersions }

            val latestRelease = validUpdateVersions.maxBy { Version.parse(it.versionNumber.substringAfter("v")) }
            val latestVersion = Version.parse(latestRelease.versionNumber)

            val currentVersion = Version.parse(Skytils.VERSION)

            val isUpdateAvailable = currentVersion < latestVersion
            if (isUpdateAvailable || System.getProperty("skytils.dev.updateChecker") != null) {
                updateObj = latestRelease
            } else {
                val potentialMCVersionChange = allowedVersions.any { Version.parse(it.versionNumber) > currentVersion }
                if (potentialMCVersionChange) {
                    Notifications.push("Skytils Update Checker", "A new $targetChannel version of Skytils is available, but it may require a Minecraft version change. Please check the Modrinth page for more details.", 1f)
                }
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
        val versionArtifact by lazy {
            //#if FABRIC
            Version.parse(matched!!.groups["version"]!!.value)
            //#else
            //$$ DefaultArtifactVersion(matched!!.groups["version"]!!.value)
            //#endif
        }
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

    override fun setup() {
        register(::onGuiOpen, EventPriority.Highest)
    }
}