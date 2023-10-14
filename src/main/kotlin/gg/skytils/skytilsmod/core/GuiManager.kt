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

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.RenderHUDEvent
import gg.skytils.skytilsmod.gui.LocationEditGui
import gg.skytils.skytilsmod.utils.GlState
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.toast.Toast
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.io.File
import java.io.Reader
import java.io.Writer
import java.util.LinkedList
import java.util.Queue

object GuiManager : PersistentSave(File(Skytils.modDir, "guipositions.json")) {
    val GUIPOSITIONS = hashMapOf<String, Pair<Float, Float>>()
    val GUISCALES = hashMapOf<String, Float>()
    val elements = hashMapOf<Int, GuiElement>()
    private val names = hashMapOf<String, GuiElement>()

    @JvmField
    var title: String? = null
    var subtitle: String? = null
    var titleDisplayTicks = 0
    var subtitleDisplayTicks = 0

    private val gui = Window(ElementaVersion.V2)
    private val toastQueue: Queue<Toast> = LinkedList()
    private val maxToasts: Int
        get() = ((UResolution.scaledHeight * 0.5) / 32).toInt()
    private val takenSlots = sortedSetOf<Int>()

    private var counter = 0
    fun registerElement(e: GuiElement): Boolean {
        return try {
            counter++
            elements[counter] = e
            names[e.name] = e
            true
        } catch (err: Exception) {
            err.printStackTrace()
            false
        }
    }

    fun addToast(toast: Toast) {
        val index = (0 until maxToasts).firstOrNull { it !in takenSlots }
        if (index != null) {
            gui.addChild(toast)
            toast.constraints.y = (index * 32).pixels
            takenSlots.add(index)
            toast.animateBeforeHide {
                takenSlots.remove(index)
                toastQueue.poll()?.let { newToast ->
                    addToast(newToast)
                }
            }
            toast.animateIn()
        } else {
            toastQueue.add(toast)
        }
    }

    fun getByID(ID: Int): GuiElement? {
        return elements[ID]
    }

    fun getByName(name: String?): GuiElement? {
        return names[name]
    }

    fun searchElements(query: String): List<GuiElement> {
        val results: MutableList<GuiElement> = ArrayList()
        for ((key, value) in names) {
            if (key.contains(query)) results.add(value)
        }
        return results
    }

    @SubscribeEvent
    fun renderPlayerInfo(event: RenderGameOverlayEvent.Post) {
        if (Skytils.usingLabymod && Minecraft.getMinecraft().ingameGUI !is GuiIngameForge) return
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return
        GlState.pushState()
        MinecraftForge.EVENT_BUS.post(RenderHUDEvent(event))
        GlState.popState()
    }

    @JvmStatic
    fun createTitle(title: String?, ticks: Int) {
        SoundQueue.addToQueue("random.orb", 0.5f, isLoud = true)
        this.title = title
        titleDisplayTicks = ticks
    }

    // LabyMod Support
    @SubscribeEvent
    fun renderPlayerInfoLabyMod(event: RenderGameOverlayEvent) {
        if (!Skytils.usingLabymod) return
        if (event.type != null) return
        GlState.pushState()
        MinecraftForge.EVENT_BUS.post(RenderHUDEvent(event))
        GlState.popState()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderHUD(event: RenderHUDEvent) {
        if (Minecraft.getMinecraft().currentScreen is LocationEditGui) return
        mc.mcProfiler.startSection("SkytilsHUD")
        gui.draw(UMatrixStack.Compat.get())
        for ((_, element) in elements) {
            mc.mcProfiler.startSection(element.name)
            try {
                GlStateManager.pushMatrix()
                GlStateManager.translate(element.scaleX, element.scaleY, 0f)
                GlStateManager.scale(element.scale, element.scale, 0f)
                element.render()
                GlStateManager.popMatrix()
            } catch (ex: Exception) {
                ex.printStackTrace()
                UChat.chat("${Skytils.failPrefix} §cSkytils ${Skytils.VERSION} caught and logged an ${ex::class.simpleName ?: "error"} while rendering ${element.name}. Please report this on the Discord server at discord.gg/skytils.")
            }
            mc.mcProfiler.endSection()
        }
        renderTitles(event.event.resolution)
        mc.mcProfiler.endSection()
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

    override fun read(reader: Reader) {
        json.decodeFromString<Map<String, GuiElementLocation>>(reader.readText()).forEach { name, (x, y, scale) ->
            val pos = x to y
            GUIPOSITIONS[name] = pos
            GUISCALES[name] = scale
            getByName(name)?.let { element ->
                element.setPos(x, y)
                element.scale = scale
            }
        }
    }

    override fun write(writer: Writer) {
        names.entries.forEach { (n, e) ->
            GUIPOSITIONS[n] = e.x to e.y
            GUISCALES[n] = e.scale
        }
        writer.write(json.encodeToString(GUIPOSITIONS.entries.associate {
            it.key to GuiElementLocation(
                it.value.first,
                it.value.second,
                GUISCALES[it.key] ?: 1f
            )
        }))
    }

    override fun setDefault(writer: Writer) {
        writer.write("{}")
    }

    // this class sucks lol (why is there a thing called floatpair)
    // was going to make guielement serializable but it's too much effort
    @Serializable
    private data class GuiElementLocation(val x: Float, val y: Float, val scale: Float)
}