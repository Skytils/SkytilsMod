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
package gg.skytils.skytilsmod.features.impl.dungeons.solvers

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.events.impl.SendChatMessageEvent
import gg.skytils.skytilsmod.features.impl.misc.Funny
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.DevTools
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.PI
import kotlin.math.abs

object TeleportMazeSolver {

    private val steppedPads = HashSet<BlockPos>()
    val poss = HashSet<BlockPos>()
    val valid = HashSet<BlockPos>()

    @SubscribeEvent
    fun onSendMsg(event: SendChatMessageEvent) {
        if (DevTools.getToggle("tpmaze") && event.message == "/resettp") {
            steppedPads.clear()
            poss.clear()
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (!Skytils.config.teleportMazeSolver || !Utils.inDungeons || !DungeonListener.missingPuzzles.contains("Teleport Maze")) return
        if (mc.thePlayer == null || mc.theWorld == null) return
        event.packet.apply {
            when (this) {
                is S08PacketPlayerPosLook -> {
                    if (y == 69.5 && Utils.equalsOneOf(
                            mc.thePlayer.posY,
                            69.5,
                            69.8125
                        ) && abs(x % 1) == 0.5 && abs(z % 1) == 0.5
                    ) {
                        val currPos = mc.thePlayer.position
                        val pos = BlockPos(x, y, z)
                        val oldTpPad = findEndPortalFrame(currPos) ?: return
                        val tpPad = findEndPortalFrame(pos) ?: return
                        steppedPads.add(oldTpPad)
                        if (tpPad !in steppedPads) {
                            steppedPads.add(tpPad)
                            val deg2Rad = PI/180
                            val magicYaw = (-yaw * deg2Rad - PI).toFloat()
                            val yawX = MathHelper.sin(magicYaw)
                            val yawZ = MathHelper.cos(magicYaw)
                            val pitchVal = -MathHelper.cos(-pitch * deg2Rad.toFloat())
                            val vec = Vec3((yawX * pitchVal).toDouble(), 69.0, (yawZ * pitchVal).toDouble())
                            valid.clear()
                            for (i in 4..23) {
                                val bp = BlockPos(
                                    x + vec.xCoord * i,
                                    vec.yCoord,
                                    z + vec.zCoord * i
                                )
                                val allDir = Utils.getBlocksWithinRangeAtSameY(bp, 2, 69)

                                valid.addAll(allDir.filter {
                                    it !in steppedPads && mc.theWorld.getBlockState(
                                        it
                                    ).block === Blocks.end_portal_frame
                                })
                            }
                            if (DevTools.getToggle("tpmaze")) UChat.chat(valid.joinToString { it.toString() })
                            if (poss.isEmpty()) poss.addAll(valid)
                            else poss.removeAll {
                                it !in valid
                            }
                        }
                        if (DevTools.getToggle("tpmaze")) UChat.chat(
                            "current: ${mc.thePlayer.positionVector}, ${mc.thePlayer.rotationPitch} ${mc.thePlayer.rotationYaw} new: ${this.x} ${this.y} ${this.z} - ${this.pitch} ${this.yaw} - ${
                                this.func_179834_f().joinToString { it.name }
                            }"
                        )
                    }
                }
            }
        }
    }

    private fun findEndPortalFrame(pos: BlockPos): BlockPos? {
        return Utils.getBlocksWithinRangeAtSameY(pos, 1, 69).find {
            mc.theWorld.getBlockState(it).block === Blocks.end_portal_frame
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Skytils.config.teleportMazeSolver || steppedPads.isEmpty() || !DungeonListener.missingPuzzles.contains("Teleport Maze")) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
        val matrixStack = UMatrixStack()

        for (pos in steppedPads) {
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Skytils.config.teleportMazeSolverColor,
                Funny.alphaMult
            )
            GlStateManager.enableCull()
        }

        for (pos in poss) {
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Color.GREEN.withAlpha(69),
                1f
            )
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        steppedPads.clear()
        poss.clear()
    }
}