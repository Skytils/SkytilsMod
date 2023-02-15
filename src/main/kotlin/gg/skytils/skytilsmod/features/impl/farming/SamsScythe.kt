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
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.RenderUtil
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SamsScythe {
    private val validBlocks = setOf<Block>(Blocks.tallgrass,
        Blocks.double_plant,
        Blocks.leaves,
        Blocks.leaves2,
        Blocks.red_flower,
        Blocks.yellow_flower)
    var items = setOf(ItemInfo("SAM_SCYTHE", 2), ItemInfo("GARDEN_SCYTHE", 4))
    @SubscribeEvent
    fun draw(event: DrawBlockHighlightEvent){
        if(!Config.showSamScytheBlocks)
            return

        if(event.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return

        val spread = isValid(ItemUtil.getSkyBlockItemID(Minecraft.getMinecraft().thePlayer.heldItem))
        if(spread == -1)
            return
        val base = event.target.blockPos

        if(!validBlocks.contains(Minecraft.getMinecraft().theWorld.getBlockState(base).block))
            return

        val alreadyChecked: MutableSet<BlockPos> = HashSet()
        alreadyChecked.add(base)
        val s = Minecraft.getMinecraft().theWorld.getBlockState(base)
        val (x, y, z) = RenderUtil.getViewerPos(event.partialTicks)
        val m = UMatrixStack()
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        s.block.setBlockBoundsBasedOnState(Skytils.mc.theWorld, base)
        RenderUtil.drawFilledBoundingBox(
            m,
            s.block.getSelectedBoundingBox(Skytils.mc.theWorld, base)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-x, -y, -z),
            Skytils.config.samScytheColor
        )
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        val queue: MutableSet<BlockPos> = HashSet()
        queue.add(event.target.blockPos)





       for (i in 1 until spread){
           for (q: BlockPos in HashSet<BlockPos>(queue)){
               for (pos in setOf(q.up(), q.down(), q.north(), q.south(), q.east(), q.west(),
                   q.north().up(), q.north().down(), q.north().west(), q.north().east(),
                   q.south().up(), q.south().down(), q.south().west(), q.south().east(),
                   q.west().up(), q.west().down(), q.east().up(), q.east().down(),
                   q.north().down().west(), q.north().down().east(), q.north().up().west(), q.north().up().east(),
                   q.south().down().west(), q.south().down().east(), q.south().up().west(), q.south().up().east())){
                   if (alreadyChecked.contains(pos))
                       continue
                   alreadyChecked.add(pos)
                   if (isAllowed(pos)) {
                       queue.add(pos)
                       val state = Minecraft.getMinecraft().theWorld.getBlockState(pos)
                       val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
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
               queue.remove(q)
           }
       }


    }
    private fun isAllowed(pos: BlockPos): Boolean{
        return validBlocks.contains(Minecraft.getMinecraft().theWorld.getBlockState(pos).block)
    }

    private fun isValid(s: String?): Int{
        for (item in items){
            if(item.equals(s))
                return item.blocks
        }
        return -1
    }

    data class ItemInfo(val id: String, val blocks: Int){
        override fun equals(other: Any?): Boolean {
            val s: String? = other as String?

            return s != null && s == id
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + blocks
            return result
        }
    }
}