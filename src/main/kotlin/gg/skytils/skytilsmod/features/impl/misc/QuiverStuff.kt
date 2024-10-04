/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.equalsAnyOf
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object QuiverStuff {
    private val activeArrowRegex = Regex("§7Active Arrow: (?<type>§.[\\w -]+) §7\\(§e(?<amount>\\d+)§7\\)")

    private var selectedType: String = ""
    private var arrowCount = -1
    private var sentWarning = false

    init {
        QuiverDisplay
        SelectedArrowDisplay
    }

    @SubscribeEvent
    fun onReceivePacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock || event.packet !is S2FPacketSetSlot || event.packet.func_149173_d() != 44) return
        val stack = event.packet.func_149174_e() ?: return
        if (!stack.item.equalsAnyOf(Items.arrow, Items.feather)) return
        val line = ItemUtil.getItemLore(stack).getOrNull(4) ?: return
        val match = activeArrowRegex.matchEntire(line) ?: return
        selectedType = match.groups["type"]?.value ?: ""
        arrowCount = match.groups["amount"]?.value?.toIntOrNull() ?: -1

        if (sentWarning && Skytils.config.restockArrowsWarning != 0 && arrowCount >= Skytils.config.restockArrowsWarning) {
            sentWarning = false
        } else if (
            !sentWarning && arrowCount != -1 &&
            Skytils.config.restockArrowsWarning != 0 && arrowCount < Skytils.config.restockArrowsWarning
        ) {
            GuiManager.createTitle("§c§lRESTOCK §r${selectedType.ifBlank { "§cUnknown" }}", 60)
            sentWarning = true
        }
    }

    object QuiverDisplay : GuiElement("Quiver Display", x = 0.05f, y = 0.4f) {
        private val arrowItem = ItemStack(Items.arrow, 1, 0)

        override fun render() {
            if (!toggled || !Utils.inSkyblock) return

            val color = when {
                arrowCount < 400 -> CommonColors.RED
                arrowCount < 1200 -> CommonColors.YELLOW
                else -> CommonColors.GREEN
            }

            RenderUtil.renderItem(arrowItem, 0, 0)
            ScreenRenderer.fontRenderer.drawString(
                if (arrowCount == -1) "???" else arrowCount.toString(),
                20f,
                5f,
                color,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                textShadow
            )
        }

        override fun demoRender() {
            RenderUtil.renderItem(arrowItem, 0, 0)
            ScreenRenderer.fontRenderer.drawString(
                "2000", 20f, 5f, CommonColors.GREEN, SmartFontRenderer.TextAlignment.LEFT_RIGHT, textShadow
            )
        }

        override val height: Int
            get() = 16
        override val width: Int
            get() = 20 + ScreenRenderer.fontRenderer.getStringWidth("2000")

        override val toggled: Boolean
            get() = Skytils.config.quiverDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    object SelectedArrowDisplay : GuiElement("Selected Arrow Display", x = 0.65f, y = 0.85f) {
        override fun render() {
            if (!toggled || !Utils.inSkyblock) return
            val alignment =
                if (scaleX < UResolution.scaledWidth / 2f) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            val text = "Selected: §r${selectedType.ifBlank { "§cUnknown" }}"

            ScreenRenderer.fontRenderer.drawString(
                text,
                if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.GREEN,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )

        }

        override fun demoRender() {
            val alignment =
                if (scaleX < UResolution.scaledWidth / 2f) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Selected: Redstone-tipped Arrow",
                if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.GREEN,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Selected: Redstone-tipped Arrow")
        override val toggled: Boolean
            get() = Skytils.config.showSelectedArrowDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}