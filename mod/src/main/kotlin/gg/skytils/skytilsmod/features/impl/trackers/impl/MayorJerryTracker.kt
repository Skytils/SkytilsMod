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

package gg.skytils.skytilsmod.features.impl.trackers.impl

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.features.impl.trackers.Tracker
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import gg.skytils.skytilsmod.utils.stripControlCodes
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.Reader
import java.io.Writer

object MayorJerryTracker : Tracker("mayorjerry") {

    @Suppress("UNUSED")
    enum class HiddenJerry(val type: String, val colorCode: String, var discoveredTimes: Long = 0L) {
        GREEN("Green Jerry", "a"),
        BLUE("Blue Jerry", "9"),
        PURPLE("Purple Jerry", "5"),
        GOLDEN("Golden Jerry", "6");

        companion object {
            fun getFromString(str: String): HiddenJerry? {
                return entries.find { str == "§${it.colorCode}${it.type}" }
            }

            fun getFromType(str: String): HiddenJerry? {
                return entries.find { str == it.type }
            }
        }
    }

    @Suppress("UNUSED")
    enum class JerryBoxDrops(val dropName: String, val colorCode: String, var droppedAmount: Long = 0L) {
        COINS("Coins", "6"),
        FARMINGXP("Farming XP", "b"),
        FORAGINGXP("Foraging XP", "b"),
        MININGXP("Mining XP", "b"),
        JERRYCANDY("Jerry Candy", "a"),
        JERRYRUNE("Jerry Rune", "f"),
        JERRYTALI("Green Jerry Talisman", "a"),
        JERRYSTONE("Jerry Stone", "9"),
        JERRYCHINE("Jerry-chine Gun", "5"),
        JERRYGLASSES("Jerry 3D Glasses", "6");

        companion object {
            fun getFromName(str: String): JerryBoxDrops? {
                return entries.find { it.dropName == str }
            }
        }
    }

    fun onJerry(type: String) {
        if (!Skytils.config.trackHiddenJerry) return
        HiddenJerry.getFromString(type)!!.discoveredTimes++
        markDirty<MayorJerryTracker>()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Skytils.config.trackHiddenJerry) return
        val formatted = event.message.formattedText
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (!formatted.startsWith("§r§b ☺ ")) return
        if (formatted.startsWith("§r§b ☺ §r§eYou claimed ") && formatted.endsWith("§efrom the Jerry Box!§r")) {
            if (formatted.contains("coins")) {
                JerryBoxDrops.COINS.droppedAmount += unformatted.replace(Regex("[^0-9]"), "").toLong()
                markDirty<MayorJerryTracker>()
            } else if (formatted.contains("XP")) {
                val xpType = with(formatted) {
                    when {
                        contains("Farming XP") -> JerryBoxDrops.FARMINGXP
                        contains("Foraging XP") -> JerryBoxDrops.FORAGINGXP
                        contains("Mining XP") -> JerryBoxDrops.MININGXP
                        else -> null
                    }
                }
                if (xpType != null) {
                    xpType.droppedAmount += unformatted.replace(Regex("[^0-9]"), "").toLong()
                    markDirty<MayorJerryTracker>()
                }
            } else {
                (JerryBoxDrops.entries.find {
                    formatted.contains(it.dropName)
                } ?: return).droppedAmount++
                markDirty<MayorJerryTracker>()
            }
            return
        }
        if (formatted.endsWith("§r§ein a Jerry Box!§r") && formatted.contains(mc.thePlayer.name)) {
            (JerryBoxDrops.entries.find {
                formatted.contains(it.dropName)
            } ?: return).droppedAmount++
            markDirty<MayorJerryTracker>()
        }
    }

    override fun resetLoot() {
        HiddenJerry.entries.onEach { it.discoveredTimes = 0L }
        JerryBoxDrops.entries.onEach { it.droppedAmount = 0L }
    }

    // TODO: 5/3/2022  Redo this entire thing
    @Serializable
    private data class TrackerSave(
        val jerry: Map<HiddenJerry, Long>,
        val drops: Map<JerryBoxDrops, Long>
    )

    override fun read(reader: Reader) {
        val save = json.decodeFromString<TrackerSave>(reader.readText())
        HiddenJerry.entries.forEach {
            it.discoveredTimes = save.jerry[it] ?: 0L
        }
        JerryBoxDrops.entries.forEach {
            it.droppedAmount = save.drops[it] ?: 0L
        }
    }

    override fun write(writer: Writer) {
        writer.write(
            json.encodeToString(
                TrackerSave(
                    HiddenJerry.entries.associateWith(HiddenJerry::discoveredTimes),
                    JerryBoxDrops.entries.associateWith(JerryBoxDrops::droppedAmount)
                )
            )
        )
    }

    override fun setDefault(writer: Writer) {
        write(writer)
    }

    init {
        JerryTrackerElement()
    }

    class JerryTrackerElement : GuiElement("Mayor Jerry Tracker", x = 150, y = 120) {
        override fun render() {
            if (toggled && Utils.inSkyblock) {

                val leftAlign = scaleX < sr.scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                var drawnLines = 0
                for (jerry in HiddenJerry.entries) {
                    if (jerry.discoveredTimes == 0L) continue
                    ScreenRenderer.fontRenderer.drawString(
                        "§${jerry.colorCode}${jerry.type}§f: ${jerry.discoveredTimes}",
                        if (leftAlign) 0f else width.toFloat(),
                        (drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        CommonColors.WHITE,
                        alignment,
                        textShadow
                    )
                    drawnLines++
                }
                for (drop in JerryBoxDrops.entries) {
                    if (drop.droppedAmount == 0L) continue
                    ScreenRenderer.fontRenderer.drawString(
                        "§${drop.colorCode}${drop.dropName}§f: ${drop.droppedAmount}",
                        if (leftAlign) 0f else width.toFloat(),
                        (drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        CommonColors.WHITE,
                        alignment,
                        textShadow
                    )
                    drawnLines++
                }
            }
        }

        override fun demoRender() {

            val leftAlign = scaleX < sr.scaledWidth / 2f
            val alignment =
                if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Jerry Tracker",
                if (leftAlign) 0f else width.toFloat(),
                0f,
                CommonColors.YELLOW,
                alignment,
                textShadow
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