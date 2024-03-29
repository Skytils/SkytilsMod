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

import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.features.impl.funny.Funny
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.middleVec
import net.minecraft.block.BlockButtonStone
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object SimonSaysSolver {
    val startBtn = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0

    @SubscribeEvent
    fun onPacket(event: PacketEvent) {
        if (Skytils.config.simonSaysSolver && Utils.inDungeons && clickInOrder.isNotEmpty() && clickNeeded < clickInOrder.size) {
            if (event.packet is C08PacketPlayerBlockPlacement || event.packet is C07PacketPlayerDigging) {
                val pos = when (event.packet) {
                    is C07PacketPlayerDigging -> event.packet.position
                    is C08PacketPlayerBlockPlacement -> event.packet.position
                    else -> error("can't reach")
                }.east()
                if (pos.x == 111 && pos.y in 120..123 && pos.z in 92..95) {
                    if (SuperSecretSettings.azooPuzzoo && clickInOrder.size == 3 && clickNeeded == 0 && pos == clickInOrder[1]) {
                        clickNeeded += 2
                    } else if (clickInOrder[clickNeeded] != pos) {
                        if (Skytils.config.blockIncorrectTerminalClicks) event.isCanceled = true
                    } else {
                        clickNeeded++
                    }
                }
            } else if (Skytils.config.predictSimonClicks && event.packet is S0BPacketAnimation && event.packet.animationType == 0) {
                val entity = mc.theWorld.getEntityByID(event.packet.entityID) as? EntityOtherPlayerMP ?: return
                if (entity.posX in 105.0..115.0 && entity.posY in 115.0..128.0 && entity.posZ in 87.0..100.0) {
                    val rayCast = entity.rayTrace(5.0, RenderUtil.getPartialTicks())
                    if (rayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        val hitPos = rayCast.blockPos ?: return
                        if (hitPos.x in 110..111 && hitPos.y in 120..123 && hitPos.z in 92..95) {
                            clickNeeded++
                            //UChat.chat("${Skytils.prefix} Registered teammate click on Simon Says. (Report on Discord if wrong.)")
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        val pos = event.pos
        val old = event.old
        val state = event.update
        if (Utils.inDungeons && Skytils.config.simonSaysSolver && TerminalFeatures.isInPhase3()) {
            if ((pos.y in 120..123) && pos.z in 92..95) {
                if (pos.x == 111) {
                    //println("Block at $pos changed to ${state.block.localizedName} from ${old.block.localizedName}")
                    if (state.block === Blocks.sea_lantern) {
                        if (!clickInOrder.contains(pos)) {
                            clickInOrder.add(pos)
                        }
                    }
                } else if (pos.x == 110) {
                    if (state.block === Blocks.air) {
                        //println("Buttons on simon says were removed!")
                        clickNeeded = 0
                        clickInOrder.clear()
                    } /*else if (state.block === Blocks.stone_button) {
                        if (old.block === Blocks.stone_button) {
                            if (state.getValue(BlockButtonStone.POWERED)) {
                                //println("Button on simon says was pressed")
                                clickNeeded++
                            }
                        }
                    }*/
                }
            } else if ((pos == startBtn && state.block === Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) || Funny.ticks == 180) {
                //println("Simon says was started")
                clickInOrder.clear()
                clickNeeded = 0
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

        if (Skytils.config.simonSaysSolver && clickNeeded < clickInOrder.size) {
            val matrixStack = UMatrixStack()
            RenderUtil.drawLabel(
                startBtn.middleVec(),
                "${clickNeeded}/${clickInOrder.size}",
                Color.WHITE,
                event.partialTicks,
                matrixStack
            )

            for (i in clickNeeded..<clickInOrder.size) {
                val pos = clickInOrder[i]
                val x = pos.x - viewerX
                val y = pos.y - viewerY + .372
                val z = pos.z - viewerZ + .308
                val color = when (i) {
                    clickNeeded -> Color.GREEN
                    clickNeeded + 1 -> Color.YELLOW
                    else -> Color.RED
                }

                RenderUtil.drawFilledBoundingBox(
                    matrixStack,
                    AxisAlignedBB(x, y, z, x - .13, y + .26, z + .382),
                    color,
                    0.5f * Funny.alphaMult
                )
            }
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        clickInOrder.clear()
        clickNeeded = 0
    }
}