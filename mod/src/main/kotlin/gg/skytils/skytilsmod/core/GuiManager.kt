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

import com.mojang.blaze3d.opengl.GlStateManager
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.unstable.state.v2.onChange
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod._event.RenderHUDEvent
import gg.skytils.skytilsmod.core.structure.v2.HudElement
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.toast.Toast
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.minecraft.client.MinecraftClient
import net.minecraft.util.profiler.Profilers
import org.lwjgl.opengl.GL11
import java.io.File
import java.io.Reader
import java.io.Writer

object GuiManager : PersistentSave(File(Skytils.modDir, "guipositions.json")), EventSubscriber {
    val elements = hashMapOf<String, HudElement>()
    val elementMetadata = hashMapOf<String, GuiElementMetadata>()
    val hud = Window(ElementaVersion.V5)
    val demoHud = object : WindowScreen(ElementaVersion.V5) {}

    @JvmField
    var title: String? = null
    var subtitle: String? = null
    var titleDisplayTicks = 0
    var subtitleDisplayTicks = 0

    private val gui = Window(ElementaVersion.V2)
    private val toastQueue = ArrayDeque<Toast>()
    private val maxToasts: Int
        get() = ((UResolution.scaledHeight * 0.5) / 32).toInt()
    private val takenSlots = sortedSetOf<Int>()

    fun registerElement(e: HudElement) {
        if (e.name in elements) error("Element with name '${e.name}' already exists!")
        val meta = elementMetadata[e.name]
        meta?.x?.let(e.x::set)
        meta?.y?.let(e.y::set)
        e.x.onChange(hud) {
            elementMetadata[e.name]?.x = it
            markDirty<GuiManager>()
        }
        e.y.onChange(hud) {
            elementMetadata[e.name]?.y = it
            markDirty<GuiManager>()
        }
        elements[e.name] = e
        hud.addChild(e.component)
        demoHud.window.addChild(e.demoComponent)
    }

    fun addToast(toast: Toast) {
        val index = (0..<maxToasts).firstOrNull { it !in takenSlots }
        if (index != null) {
            gui.addChild(toast)
            toast.constraints.y = (index * 32).pixels
            takenSlots.add(index)
            toast.animateBeforeHide {
                takenSlots.remove(index)
                toastQueue.removeFirstOrNull()?.let { newToast ->
                    addToast(newToast)
                }
            }
            toast.animateIn()
        } else {
            toastQueue.add(toast)
        }
    }

    fun getByName(name: String?): HudElement? {
        return elements[name]
    }

    fun searchElements(query: String): Collection<HudElement> {
        return elements.filter { it.key.contains(query) }.values
    }

    @JvmStatic
    fun createTitle(title: String?, ticks: Int) {
        SoundQueue.addToQueue("entity.experience_orb.pickup", 0.5f, isLoud = true)
        this.title = title
        titleDisplayTicks = ticks
    }

    fun onRenderHUD(event: RenderHUDEvent) {
        if (
            MinecraftClient.getInstance().currentScreen == demoHud
            ) return
        //#if MC>=12000
        val profiler = Profilers.get()
        //#else
        //$$ val profiler = mc.tickProfilerResult
        //#endif
        profiler.push("SkytilsHUD")
        profiler.push("Toasts")
        gui.draw(UMatrixStack.Compat.get())
        profiler.swap("Hud")
        hud.draw(UMatrixStack.Compat.get())
        profiler.swap("Titles")
        renderTitles()
        profiler.pop()
    }

    fun onTick(event: gg.skytils.event.impl.TickEvent) {
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
    private fun renderTitles() {
        if (mc.world == null || mc.player == null || !Utils.inSkyblock) {
            return
        }
        val scaledWidth = UResolution.scaledWidth
        val scaledHeight = UResolution.scaledHeight
        val matrixStack = UMatrixStack.Compat.get()
        if (title != null) {
            val stringWidth = mc.textRenderer.getWidth(title)
            var scale = 4f // Scale is normally 4, but if its larger than the screen, scale it down...
            if (stringWidth * scale > scaledWidth * 0.9f) {
                scale = scaledWidth * 0.9f / stringWidth.toFloat()
            }
            matrixStack.push()
            matrixStack.translate((scaledWidth / 2).toFloat(), (scaledHeight / 2).toFloat(), 0.0f)
            GlStateManager._enableBlend()
            GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            matrixStack.scale(scale, scale, scale) // TODO Check if changing this scale breaks anything...
            UGraphics.drawString(
                matrixStack,
                title,
                (-stringWidth / 2).toFloat(),
                -20.0f,
                -0x10000,
                true
            )
            matrixStack.pop()
        }
        if (subtitle != null) {
            val stringWidth = mc.textRenderer.getWidth(subtitle)
            var scale = 2f // Scale is normally 2, but if its larger than the screen, scale it down...
            if (stringWidth * scale > scaledWidth * 0.9f) {
                scale = scaledWidth * 0.9f / stringWidth.toFloat()
            }
            matrixStack.push()
            matrixStack.translate((scaledWidth / 2).toFloat(), (scaledHeight / 2).toFloat(), 0.0f)
            GlStateManager._enableBlend()
            GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            matrixStack.scale(scale, scale, scale) // TODO Check if changing this scale breaks anything...
            UGraphics.drawString(
                matrixStack,
                subtitle, -stringWidth / 2f, -23.0f,
                -0x10000, true
            )
            matrixStack.pop()
        }
    }

    override fun read(reader: Reader) {
        json.decodeFromString<Map<String, GuiElementMetadata>>(reader.readText()).forEach { (name, metadata) ->
            elementMetadata[name] = metadata
            getByName(name)?.applyMetadata(metadata)
        }
    }

    override fun write(writer: Writer) {
        elements.entries.forEach { (n, e) ->
            elementMetadata[n] = e.asMetadata()
        }
        writer.write(json.encodeToString(elementMetadata))
    }

    override fun setDefault(writer: Writer) {
        writer.write("{}")
    }

    enum class TextShadow {
        NONE, NORMAL, OUTLINE
    }

    @Serializable
    data class GuiElementMetadata(var x: Float, var y: Float, var scale: Float = 1f, val textShadow: TextShadow = TextShadow.NORMAL)

    override fun setup() {
        register(::onRenderHUD, EventPriority.Highest)
        register(::onTick)
    }
}