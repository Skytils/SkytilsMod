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

package skytils.skytilsmod.features.impl.trackers

import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object MayorJerryTracker : PersistentSave(File(File(Skytils.modDir, "trackers"), "mayorjerry.json")) {

    @Suppress("UNUSED")
    enum class HiddenJerry(val type: String, val colorCode: String, var discoveredTimes: Long = 0L) {
        GREEN("Green Jerry", "a"),
        BLUE("Blue Jerry", "9"),
        PURPLE("Purple Jerry", "5"),
        GOLDEN("Golden Jerry", "6");

        companion object {
            fun getFromString(str: String): HiddenJerry? {
                return values().find { str == "§${it.colorCode}${it.type}" }
            }

            fun getFromType(str: String): HiddenJerry? {
                return values().find { str == it.type }
            }
        }
    }

    fun onJerry(type: String) {
        if (!Skytils.config.trackHiddenJerry) return
        HiddenJerry.getFromString(type)!!.discoveredTimes++
    }

    override fun read(reader: FileReader) {
        val obj = gson.fromJson(reader, JsonObject::class.java)
        for (entry in obj.get("jerry").asJsonObject.entrySet()) {
            (HiddenJerry.getFromType(entry.key) ?: continue).discoveredTimes = entry.value.asLong
        }
    }

    override fun write(writer: FileWriter) {
        val obj = JsonObject()

        val jerryObj = JsonObject()
        for (jerry in HiddenJerry.values()) {
            jerryObj.addProperty(jerry.type, jerry.discoveredTimes)
        }
        obj.add("jerry", jerryObj)
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
    }

    init {
        JerryTrackerElement()
    }

    class JerryTrackerElement : GuiElement("Mayor Jerry Tracker", FloatPair(150, 120)) {
        override fun render() {
            if (toggled && Utils.inSkyblock) {
                val sr = ScaledResolution(Minecraft.getMinecraft())
                val leftAlign = actualX < sr.scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                var drawnLines = 0
                for (jerry in HiddenJerry.values()) {
                    ScreenRenderer.fontRenderer.drawString(
                        "§${jerry.colorCode}${jerry.type}§f: ${jerry.discoveredTimes}",
                        if (leftAlign) 0f else width.toFloat(),
                        (drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                    drawnLines++
                }
            }
        }

        override fun demoRender() {
            val sr = ScaledResolution(Minecraft.getMinecraft())
            val leftAlign = actualX < sr.scaledWidth / 2f
            val alignment =
                if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Jerry Tracker",
                if (leftAlign) 0f else width.toFloat(),
                0f,
                CommonColors.YELLOW,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Jerry Tracker")

        override val toggled: Boolean
            get() = Skytils.config.trackHiddenJerry

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

}