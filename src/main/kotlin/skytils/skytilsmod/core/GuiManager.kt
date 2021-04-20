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

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.gui.LocationEditGui
import skytils.skytilsmod.utils.MathUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.toasts.GuiToast
import java.io.*

class GuiManager {
    private var counter = 0
    fun registerElement(e: GuiElement): Boolean {
        return try {
            counter++
            Companion.elements[counter] = e
            names[e.name] = e
            true
        } catch (err: Exception) {
            err.printStackTrace()
            false
        }
    }

    fun getByID(ID: Int): GuiElement? {
        return Companion.elements[ID]
    }

    fun getByName(name: String?): GuiElement? {
        return names[name]
    }

    fun searchElements(query: String): List<GuiElement> {
        val results: MutableList<GuiElement> = ArrayList()
        for ((key, value) in names) {
            if (key == query) results.add(value)
        }
        return results
    }

    val elements: Map<Int, GuiElement>
        get() = Companion.elements

    @SubscribeEvent
    fun renderPlayerInfo(event: RenderGameOverlayEvent.Post) {
        if (Skytils.usingLabymod && Minecraft.getMinecraft().ingameGUI !is GuiIngameForge) return
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return
        if (Minecraft.getMinecraft().currentScreen is LocationEditGui) return
        for ((_, element) in Companion.elements) {
            try {
                GlStateManager.pushMatrix()
                GlStateManager.translate(element.actualX, element.actualY, 0f)
                GlStateManager.scale(element.scale, element.scale, 0f)
                element.render()
                GlStateManager.popMatrix()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        renderTitles(event.resolution)
    }

    // LabyMod Support
    @SubscribeEvent
    fun renderPlayerInfoLabyMod(event: RenderGameOverlayEvent) {
        if (!Skytils.usingLabymod) return
        if (event.type != null) return
        if (Minecraft.getMinecraft().currentScreen is LocationEditGui) return
        for ((_, value) in Companion.elements) {
            try {
                value.render()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        renderTitles(event.resolution)
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (titleDisplayTicks > 0) {
            titleDisplayTicks--
        } else {
            titleDisplayTicks = 0
            title = null
        }
        if (subtitleDisplayTicks > 0) {
            subtitleDisplayTicks--
        } else {
            subtitleDisplayTicks = 0
            subtitle = null
        }
    }

    /**
     * Adapted from SkyblockAddons under MIT license
     * @link https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
     * @author BiscuitDevelopment
     */
    private fun renderTitles(scaledResolution: ScaledResolution) {
        val mc = Minecraft.getMinecraft()
        if (mc.theWorld == null || mc.thePlayer == null || !Utils.inSkyblock) {
            return
        }
        val scaledWidth = scaledResolution.scaledWidth
        val scaledHeight = scaledResolution.scaledHeight
        if (title != null) {
            val stringWidth = mc.fontRendererObj.getStringWidth(title)
            var scale = 4f // Scale is normally 4, but if its larger than the screen, scale it down...
            if (stringWidth * scale > scaledWidth * 0.9f) {
                scale = scaledWidth * 0.9f / stringWidth.toFloat()
            }
            GlStateManager.pushMatrix()
            GlStateManager.translate((scaledWidth / 2).toFloat(), (scaledHeight / 2).toFloat(), 0.0f)
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scale, scale, scale) // TODO Check if changing this scale breaks anything...
            mc.fontRendererObj.drawString(
                title,
                (-mc.fontRendererObj.getStringWidth(title) / 2).toFloat(),
                -20.0f,
                0xFF0000,
                true
            )
            GlStateManager.popMatrix()
            GlStateManager.popMatrix()
        }
        if (subtitle != null) {
            val stringWidth = mc.fontRendererObj.getStringWidth(subtitle)
            var scale = 2f // Scale is normally 2, but if its larger than the screen, scale it down...
            if (stringWidth * scale > scaledWidth * 0.9f) {
                scale = scaledWidth * 0.9f / stringWidth.toFloat()
            }
            GlStateManager.pushMatrix()
            GlStateManager.translate((scaledWidth / 2).toFloat(), (scaledHeight / 2).toFloat(), 0.0f)
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scale, scale, scale) // TODO Check if changing this scale breaks anything...
            mc.fontRendererObj.drawString(
                subtitle, -mc.fontRendererObj.getStringWidth(subtitle) / 2f, -23.0f,
                0xFF0000, true
            )
            GlStateManager.popMatrix()
            GlStateManager.popMatrix()
        }
    }

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private lateinit var positionFile: File
        lateinit var GUIPOSITIONS: MutableMap<String?, FloatPair?>
        private const val GUI_SCALE_MINIMUM = 0.5f
        private const val GUI_SCALE_MAXIMUM = 5f
        private val elements: MutableMap<Int, GuiElement> = HashMap()
        private val names: MutableMap<String?, GuiElement> = HashMap()

        @JvmField
        var toastGui = GuiToast(Minecraft.getMinecraft())

        @JvmField
        var title: String? = null
        var subtitle: String? = null
        var titleDisplayTicks = 0
        var subtitleDisplayTicks = 0
        fun readConfig() {
            var file: JsonObject
            try {
                FileReader(positionFile).use { `in` ->
                    file = gson.fromJson(`in`, JsonObject::class.java)
                    for ((key, value) in file.entrySet()) {
                        try {
                            GUIPOSITIONS[key] = FloatPair(
                                value.asJsonObject["x"].asJsonObject["value"].asFloat,
                                value.asJsonObject["y"].asJsonObject["value"].asFloat
                            )
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                GUIPOSITIONS = HashMap()
                try {
                    FileWriter(positionFile).use { writer -> gson.toJson(GUIPOSITIONS, writer) }
                } catch (ignored: Exception) {
                }
            }
        }

        fun saveConfig() {
            for ((key, value) in names) {
                GUIPOSITIONS[key] = value.pos
            }
            try {
                FileWriter(positionFile).use { writer -> gson.toJson(GUIPOSITIONS, writer) }
            } catch (ignored: Exception) {
            }
        }

        @JvmStatic
        fun createTitle(title: String?, ticks: Int) {
            Utils.playLoudSound("random.orb", 0.5)
            Companion.title = title
            titleDisplayTicks = ticks
        }

        fun normalizeValueNoStep(value: Float): Float {
            println(snapNearDefaultValue(value))
            return MathUtil.clamp(snapNearDefaultValue(value), GUI_SCALE_MINIMUM, GUI_SCALE_MAXIMUM)
        }

        private fun snapNearDefaultValue(value: Float): Float {
            return if (value != 1f && value > 1 - 0.05 && value < 1 + 0.05) {
                1f
            } else value
        }
    }

    init {
        positionFile = File(Skytils.modDir, "guipositions.json")
        GUIPOSITIONS = HashMap()
        readConfig()
    }
}