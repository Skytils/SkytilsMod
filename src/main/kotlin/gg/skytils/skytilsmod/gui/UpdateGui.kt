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
package gg.skytils.skytilsmod.gui

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.IO
import gg.skytils.skytilsmod.Skytils.Companion.client
import gg.skytils.skytilsmod.core.UpdateChecker
import gg.skytils.skytilsmod.utils.MathUtil
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.EnumChatFormatting
import java.io.File
import kotlin.math.floor

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
class UpdateGui(restartNow: Boolean) : GuiScreen() {
    companion object {
        private val DOTS = arrayOf(".", "..", "...", "...", "...")
        private const val DOT_TIME = 200 // ms between "." -> ".." -> "..."
        var failed = false
        var complete = false
    }

    private var backButton: GuiButton? = null
    private var progress = 0.0
    override fun initGui() {
        buttonList.add(GuiButton(0, width / 2 - 100, height / 3 * 2, 200, 20, "").also { backButton = it })
        updateText()
    }

    private fun doUpdate(restartNow: Boolean) {
        try {
            val directory = File(Skytils.modDir, "updates")
            val url = UpdateChecker.updateDownloadURL
            val jarName = UpdateChecker.getJarNameFromUrl(url)
            IO.launch(CoroutineName("Skytils-update-downloader-thread")) {
                downloadUpdate(url, directory)
                if (!failed) {
                    UpdateChecker.scheduleCopyUpdateAtShutdown(jarName)
                    if (restartNow) {
                        mc.shutdown()
                    }
                    complete = true
                    updateText()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun updateText() {
        backButton!!.displayString = if (failed || complete) "Back" else "Cancel"
    }

    private suspend fun downloadUpdate(urlString: String, directory: File) {
        try {
            val url = Url(urlString)

            val st = client.get(url) {
                expectSuccess = false
                onDownload { bytesSentTotal, contentLength ->
                    progress = bytesSentTotal / contentLength.toDouble()
                }
            }
            if (st.status != HttpStatusCode.OK) {
                failed = true
                updateText()
                println("$url returned status code ${st.status}")
                return
            }
            if (!directory.exists() && !directory.mkdirs()) {
                failed = true
                updateText()
                println("Couldn't create update file directory")
                return
            }
            val fileSaved = File(directory, url.pathSegments.last().decodeURLPart())
            if (mc.currentScreen !== this@UpdateGui || st.bodyAsChannel().copyTo(fileSaved.writeChannel()) == 0L) {
                failed = true
                return
            }
            println("Downloaded update to $fileSaved")
        } catch (ex: Exception) {
            ex.printStackTrace()
            failed = true
            updateText()
        }
    }

    public override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            mc.displayGuiScreen(null)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        when {
            failed -> drawCenteredString(
                mc.fontRendererObj,
                EnumChatFormatting.RED.toString() + "Update download failed",
                width / 2,
                height / 2,
                -0x1
            )
            complete -> drawCenteredString(
                mc.fontRendererObj,
                EnumChatFormatting.GREEN.toString() + "Update download complete",
                width / 2,
                height / 2,
                0xFFFFFF
            )
            else -> {
                val left = (width / 2 - 100).coerceAtLeast(10)
                val right = (width / 2 + 100).coerceAtMost(width - 10)
                val top = height / 2 - 2 - MathUtil.ceil(mc.fontRendererObj.FONT_HEIGHT / 2f)
                val bottom = height / 2 + 2 + MathUtil.floor(mc.fontRendererObj.FONT_HEIGHT / 2f)
                drawRect(left - 1, top - 1, right + 1, bottom + 1, -0x3f3f40)
                val progressPoint = floor(progress * (right - left) + left).toInt().coerceIn(left, right)
                drawRect(left, top, progressPoint, bottom, -0x34c2cb)
                drawRect(progressPoint, top, right, bottom, -0x1)
                val label = String.format("%d%%", floor(progress * 100).toInt().coerceIn(0, 100))
                mc.fontRendererObj.drawString(
                    label,
                    (width - mc.fontRendererObj.getStringWidth(label)) / 2,
                    top + 3,
                    -0x1000000
                )
                val x = (width - mc.fontRendererObj.getStringWidth(
                    String.format(
                        "Downloading %s",
                        DOTS[DOTS.size - 1]
                    )
                )) / 2
                val title = String.format(
                    "Downloading %s",
                    DOTS[(System.currentTimeMillis() % (DOT_TIME * DOTS.size)).toInt() / DOT_TIME]
                )
                drawString(mc.fontRendererObj, title, x, top - mc.fontRendererObj.FONT_HEIGHT - 2, -0x1)
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    init {
        doUpdate(restartNow)
    }
}