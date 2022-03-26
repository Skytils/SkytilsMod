/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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
package skytils.skytilsmod.features.impl.dungeons.solvers.terminals

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors

object ChangeAllToSameColorSolver {
    val ordering =
        setOf(EnumDyeColor.RED, EnumDyeColor.ORANGE, EnumDyeColor.YELLOW, EnumDyeColor.GREEN, EnumDyeColor.BLUE)

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Utils.inDungeons || !Skytils.config.changeAllSameColorTerminalSolver || event.container !is ContainerChest || event.chestName != "Change all to same color!") return
        val grid = event.container.inventorySlots.filter {
            it.inventory == event.container.lowerChestInventory && it.stack?.displayName?.startsWith("§a") == true
        }
        val mostFans = ordering.maxByOrNull { c -> grid.count { it.stack?.metadata == c.metadata } } ?: EnumDyeColor.RED
        val targetIndex = ordering.indexOfFirst { it.metadata == mostFans.metadata }
        val mapping = grid.filter { it.stack.metadata != mostFans.metadata }.associateWith { slot ->
            val stack = slot.stack
            val myIndex = ordering.indexOfFirst { it.metadata == stack.metadata }
            ((targetIndex - myIndex) % ordering.size + ordering.size) % ordering.size to -(((myIndex - targetIndex) % ordering.size + ordering.size) % ordering.size)
        }
        GlStateManager.translate(0f, 0f, 299f)
        for ((slot, clicks) in mapping) {
            var betterOpt = if (clicks.first > -clicks.second) "${clicks.second}" else "${clicks.first}"
            var color = CommonColors.WHITE
            if(Skytils.config.changeToSameColorMode == 1){
                val leftClick = when(Minecraft.getMinecraft().gameSettings.keyBindAttack.keyCode){
                    -100 -> "L"
                    else -> "R"
                }
                val rightClick = when(Minecraft.getMinecraft().gameSettings.keyBindUseItem.keyCode){
                    -100 -> "L"
                    else -> "R"
                }
                betterOpt = if(betterOpt.toInt() < 0) { "${betterOpt.toInt() * -1}$rightClick" } else "${betterOpt}$leftClick"
            } else if(Skytils.config.changeToSameColorMode == 2){
                betterOpt = clicks.first.toString()
                when(betterOpt.toInt()) {
                    1 -> color = CommonColors.GREEN
                    2,3 -> color = CommonColors.YELLOW
                    4 -> color = CommonColors.RED
                }
            }

            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
            GlStateManager.disableBlend()
            ScreenRenderer.fontRenderer.drawString(
                betterOpt,
                slot.xDisplayPosition + 9f,
                slot.yDisplayPosition + 4f,
                color,
                SmartFontRenderer.TextAlignment.MIDDLE,
                SmartFontRenderer.TextShadow.NORMAL
            )
            GlStateManager.enableLighting()
            GlStateManager.enableDepth()
        }
        GlStateManager.translate(0f, 0f, -299f)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.changeAllSameColorTerminalSolver) return
        if (event.toolTip == null) return
        if (mc.thePlayer.openContainer is ContainerChest) {
            val chest = mc.thePlayer.openContainer as ContainerChest
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText
            if (chestName == "Change all to same color!") {
                event.toolTip.clear()
            }
        }
    }
}
