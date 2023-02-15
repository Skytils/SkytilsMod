/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2023 Skytils
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

package gg.skytils.skytilsmod.features.impl.farming

import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.RenderUtil
import net.minecraft.block.Block
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SamsScythe {
    private val validBlocks = setOf<Block>(
        Blocks.tallgrass, Blocks.double_plant, Blocks.leaves, Blocks.leaves2, Blocks.red_flower, Blocks.yellow_flower
    )
    var items = hashMapOf("SAM_SCYTHE" to 1, "GARDEN_SCYTHE" to 2)

    @SubscribeEvent
    fun draw(event: DrawBlockHighlightEvent) {
        if (!Skytils.config.showSamScytheBlocks) return

        if (event.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return

        val size = items[ItemUtil.getSkyBlockItemID(Skytils.mc.thePlayer.heldItem)] ?: return
        val base = event.target.blockPos

        if (!validBlocks.contains(Skytils.mc.theWorld.getBlockState(base).block)) return
        draw(base, event.partialTicks)
        for (pos in BlockPos.getAllInBox(
            base.add(-size, -size, -size), base.add(size, size, size)
        )) {
            if (validBlocks.contains(Skytils.mc.theWorld.getBlockState(pos).block)) {
                draw(pos, event.partialTicks)
            }
        }

    }

    private fun draw(pos: BlockPos, partialTicks: Float) {
        val state = Skytils.mc.theWorld.getBlockState(pos)
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(partialTicks)
        val matrixStack = UMatrixStack()
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        state.block.setBlockBoundsBasedOnState(Skytils.mc.theWorld, pos)
        RenderUtil.drawFilledBoundingBox(
            matrixStack,
            state.block.getSelectedBoundingBox(Skytils.mc.theWorld, pos)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-viewerX, -viewerY, -viewerZ),
            Skytils.config.samScytheColor
        )
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
    }
}