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
package skytils.skytilsmod.core

import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMainMenu
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.APIUtil
import java.io.*

class UpdateChecker {
    companion object {
        private val mc = Minecraft.getMinecraft()
        private val updateGetter = UpdateGetter()
        val updateDownloadURL: String
            get() = updateGetter.updateObj!!["assets"].asJsonArray[0].asJsonObject["browser_download_url"].asString

        fun getJarNameFromUrl(url: String): String {
            val sUrl = url.split("/".toRegex()).toTypedArray()
            return sUrl[sUrl.size - 1]
        }

        fun scheduleCopyUpdateAtShutdown(jarName: String?) {
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    println("Attempting to apply Skytils update.")
                    val oldJar = Skytils.jarFile
                    if (oldJar == null || !oldJar.exists() || oldJar.isDirectory) {
                        println("Old jar file not found.")
                        return@Thread
                    }
                    val newJar = File(File(Skytils.modDir, "updates"), jarName)
                    copyFile(newJar, oldJar)
                    newJar.delete()
                    println("Successfully applied Skytils update.")
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            })
        }

        /**
         * Taken from Wynntils under GNU Affero General Public License v3.0
         * Modified to perform faster
         * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
         * @author Wynntils
         * Copy a file from a location to another
         *
         * @param sourceFile The source file
         * @param destFile Where it will be
         */
        @Throws(IOException::class)
        fun copyFile(sourceFile: File, destFile: File?) {
            var destFile = destFile
            if (destFile == null || !destFile.exists()) {
                destFile = File(File(sourceFile.parentFile, "mods"), "Skytils.jar")
                sourceFile.renameTo(destFile)
                return
            }
            var source: InputStream? = null
            var dest: OutputStream? = null
            try {
                source = FileInputStream(sourceFile)
                dest = FileOutputStream(destFile)
                val buffer = ByteArray(1024)
                var length: Int
                while (source.read(buffer).also { length = it } > 0) {
                    dest.write(buffer, 0, length)
                }
            } finally {
                source!!.close()
                dest!!.close()
            }
        }

        init {
            val thread = Thread(updateGetter)
            thread.start()
            try {
                thread.join()
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
            }
        }
    }

    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        if (e.gui !is GuiMainMenu) return
        if (updateGetter.updateObj == null) return
        try {
/*            Notifications notifs = Notifications.INSTANCE;
            notifs.pushNotification("New Skytils Version Available", "Click here to download", () -> {
                Skytils.displayScreen = new UpdateGui();
                return Unit.INSTANCE;
            });*/
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private class UpdateGetter : Runnable {
        @Volatile
        var updateObj: JsonObject? = null

        /**
         * Modified version from Danker's Skyblock Mod, taken under GPL 3.0 license.
         * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
         * @author bowser0000
         */
        override fun run() {
            println("Checking for updates...")
            val latestRelease =
                if (Skytils.config.updateChannel == 1) APIUtil.getArrayResponse("https://api.github.com/repos/Skytils/SkytilsMod/releases")[0].asJsonObject else APIUtil.getJSONResponse(
                    "https://api.github.com/repos/Skytils/SkytilsMod/releases/latest"
                )
            val latestTag = latestRelease["tag_name"].asString
            val currentTag = Skytils.VERSION

            val currentVersion = DefaultArtifactVersion(currentTag.substringBefore("-"))
            val latestVersion = DefaultArtifactVersion(latestTag.substringAfter("v").substringBefore("-"))
            if (latestTag.contains("pre") || (currentTag.contains("pre") && currentVersion >= latestVersion)) {
                var currentPre = 0.0
                var latestPre = 0.0
                if (currentTag.contains("pre")) {
                    currentPre = currentTag.substringAfter("pre").toDouble()
                }
                if (latestTag.contains("pre")) {
                    latestPre = latestTag.substringAfter("pre").toDouble()
                }
                if ((latestPre > currentPre) || (latestPre == 0.0 && currentVersion.compareTo(latestVersion) == 0)) {
                    updateObj = latestRelease
                }
            } else if (currentVersion < latestVersion) {
                updateObj = latestRelease
            }
        }
    }
}