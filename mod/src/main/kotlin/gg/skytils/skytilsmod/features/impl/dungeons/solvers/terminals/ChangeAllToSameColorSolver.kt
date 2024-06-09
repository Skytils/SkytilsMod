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
package gg.skytils.skytilsmod.features.impl.dungeons.solvers.terminals

import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.item.ItemTooltipEvent
import gg.skytils.event.impl.screen.GuiContainerForegroundDrawnEvent
import gg.skytils.event.impl.screen.GuiContainerSlotClickEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import kotlin.random.Random

object ChangeAllToSameColorSolver : EventSubscriber {
    private val ordering =
        setOf(
            EnumDyeColor.RED,
            EnumDyeColor.ORANGE,
            EnumDyeColor.YELLOW,
            EnumDyeColor.GREEN,
            EnumDyeColor.BLUE
        ).withIndex().associate { (i, c) ->
            c.metadata to i
        }
    private var mostCommon = EnumDyeColor.RED.metadata

    override fun setup() {
        register(::onForegroundEvent)
        register(::onSlotClick, EventPriority.High)
        register(::onTooltip, EventPriority.Lowest)
    }

    fun onForegroundEvent(event: GuiContainerForegroundDrawnEvent) {
        if (!Utils.inDungeons || !Skytils.config.changeAllSameColorTerminalSolver || event.container !is ContainerChest || event.chestName != "Change all to same color!") return
        val container = event.container as? ContainerChest ?: return
        val grid = container.inventorySlots.filter {
            it.inventory == container.lowerChestInventory && it.stack?.displayName?.startsWith("§a") == true
        }
        val counts = ordering.keys.associateWith { c -> grid.count { it.stack?.metadata == c } }
        val currentPath = counts[mostCommon]!!
        val (candidate, maxCount) = counts.maxBy { it.value }

        if (maxCount > currentPath) {
            mostCommon = candidate
        }

        val targetIndex = ordering[mostCommon]!!
        val mapping = grid.filter { it.stack.metadata != mostCommon }.associateWith { slot ->
            val stack = slot.stack
            val myIndex = ordering[stack.metadata]!!
            val normalCycle = ((targetIndex - myIndex) % ordering.size + ordering.size) % ordering.size
            val otherCycle = -((myIndex - targetIndex) % ordering.size + ordering.size) % ordering.size
            normalCycle to otherCycle
        }
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 299f)
        for ((slot, clicks) in mapping) {
            var betterOpt = if (clicks.first > -clicks.second) clicks.second else clicks.first
            var color = CommonColors.WHITE
            if (Skytils.config.changeToSameColorMode == 1) {
                betterOpt = clicks.first
                when (betterOpt) {
                    1 -> color = CommonColors.GREEN
                    2, 3 -> color = CommonColors.YELLOW
                    4 -> color = CommonColors.RED
                }
            }

            if (SuperSecretSettings.bennettArthur) betterOpt = Random.nextInt(-4, 5)

            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
            GlStateManager.disableBlend()
            ScreenRenderer.fontRenderer.drawString(
                "$betterOpt",
                slot.xDisplayPosition + 9f,
                slot.yDisplayPosition + 4f,
                color,
                SmartFontRenderer.TextAlignment.MIDDLE,
                SmartFontRenderer.TextShadow.NORMAL
            )
            GlStateManager.enableLighting()
            GlStateManager.enableDepth()
        }
        GlStateManager.popMatrix()
    }

    fun onSlotClick(event: GuiContainerSlotClickEvent) {
        if (!Utils.inDungeons || !Skytils.config.changeAllSameColorTerminalSolver || !Skytils.config.blockIncorrectTerminalClicks) return
        if (event.container is ContainerChest && event.chestName == "Change all to same color!") {
            if (event.slot?.stack?.metadata == mostCommon) event.cancelled = true
        }
    }

    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inDungeons || !Skytils.config.changeAllSameColorTerminalSolver) return
        val chest = mc.thePlayer?.openContainer as? ContainerChest ?: return
        val chestName = chest.lowerChestInventory.displayName.unformattedText
        if (chestName == "Change all to same color!") {
            event.tooltip.clear()
        }
    }
}
