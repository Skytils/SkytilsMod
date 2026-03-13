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
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod._event.RenderHUDEvent
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.core.structure.v2.HudElement
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.toast.Toast
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.profiler.Profilers
import java.io.File
import java.io.Reader
import java.io.Writer

object GuiManager : PersistentSave(File(Skytils.modDir, "guipositions.json")), EventSubscriber {
    val elements = hashMapOf<String, GuiElement>()
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

    fun registerElement(e: GuiElement): Boolean {
        return try {
            elements[e.name] = e
            true
        } catch (err: Exception) {
            err.printStackTrace()
            false
        }
    }

    fun registerElement(e: HudElement) {
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

    fun getByName(name: String?): GuiElement? {
        return elements[name]
    }

    fun searchElements(query: String): Collection<GuiElement> {
        return elements.filter { it.key.contains(query) }.values
    }

    @JvmStatic
    fun createTitle(title: String?, ticks: Int) {
        SoundQueue.addToQueue("random.orb", 0.5f, isLoud = true)
        this.title = title
        titleDisplayTicks = ticks
    }

    fun onRenderHUD(event: RenderHUDEvent) {
        if (mc.currentScreen == demoHud) return
        Profilers.get().push("SkytilsHUD")
        gui.draw(UMatrixStack.Compat.get())
        hud.draw(UMatrixStack.Compat.get())
        for ((_, element) in elements) {
            Profilers.get().push(element.name)
            try {
                event.context.matrices.push()
                event.context.matrices.translate(element.scaleX, element.scaleY, 0f)
                event.context.matrices.scale(element.scale, element.scale, 0f)
                element.render(event.context, event.tickCounter)
                event.context.matrices.pop()
            } catch (ex: Exception) {
                ex.printStackTrace()
                UChat.chat("${Skytils.failPrefix} §cSkytils ${Skytils.VERSION} caught and logged an ${ex::class.simpleName ?: "error"} while rendering ${element.name}. Please report this on the Discord server at discord.gg/skytils.")
            }
            Profilers.get().pop()
        }
        renderTitles(event.context)
        Profilers.get().pop()
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

    private fun renderTitles(context: DrawContext) {
        if (mc.world == null || mc.player == null || !Utils.inSkyblock) {
            return
        }

        val textRenderer = mc.textRenderer
        val scaledWidth = context.scaledWindowWidth

        if (title != null) {
            val stringWidth = mc.textRenderer.getWidth(title)
            var scale = 4f // Scale is normally 4, but if its larger than the screen, scale it down...
            if (stringWidth * scale > scaledWidth * 0.9f) {
                scale = scaledWidth * 0.9f / stringWidth.toFloat()
            }
            context.matrices.push()
            context.matrices.translate(
                (context.scaledWindowWidth / 2).toFloat(),
                (context.scaledWindowHeight / 2).toFloat(),
                0.0f
            )
/*            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(770, 771, 1, 0)*/
            context.matrices.scale(scale, scale, scale) // TODO Check if changing this scale breaks anything...
            context.matrices.push()
            context.drawTextWithBackground(textRenderer, Text.of(title), -stringWidth / 2, -10, stringWidth, 0xFF0000)
            context.matrices.pop()
            context.matrices.pop()
        }

        if (subtitle != null) {
            val stringWidth = textRenderer.getWidth(subtitle)
            var scale = 2f // Scale is normally 2, but if its larger than the screen, scale it down...
            if (stringWidth * scale > scaledWidth * 0.9f) {
                scale = scaledWidth * 0.9f / stringWidth.toFloat()
            }
            context.matrices.push()
            context.matrices.translate(
                (context.scaledWindowWidth / 2).toFloat(),
                (context.scaledWindowHeight / 2).toFloat(),
                0.0f
            )
/*            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(770, 771, 1, 0)*/
            context.matrices.push()
            context.matrices.scale(scale, scale, scale) // TODO Check if changing this scale breaks anything...
            context.drawTextWithBackground(textRenderer, Text.of(subtitle), -stringWidth / 2, 5, stringWidth, 0xFF0000)
            context.matrices.pop()
            context.matrices.pop()
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

    @Serializable
    data class GuiElementMetadata(val x: Float, val y: Float, val scale: Float = 1f)

    override fun setup() {
        register(::onRenderHUD, EventPriority.Highest)
        register(::onTick)
    }
}