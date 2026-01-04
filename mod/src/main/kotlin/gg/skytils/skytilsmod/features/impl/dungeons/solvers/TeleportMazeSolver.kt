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

import com.mojang.blaze3d.opengl.GlStateManager
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageSentEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.MainThreadPacketReceiveEvent
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.DevTools
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.block.Blocks
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.math.Box
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object TeleportMazeSolver : EventSubscriber {

    private val steppedPads = HashSet<BlockPos>()
    val poss = HashSet<BlockPos>()
    val valid = HashSet<BlockPos>()

    override fun setup() {
        register(::onSendMsg)
        register(::onPacket)
        register(::onWorldRender)
        register(::onWorldChange)
    }

    fun onSendMsg(event: ChatMessageSentEvent) {
        if (DevTools.getToggle("tpmaze") && event.message == "/resettp") {
            steppedPads.clear()
            poss.clear()
            event.cancelled = true
        }
    }

    fun onPacket(event: MainThreadPacketReceiveEvent<*>) {
        if (!Skytils.config.teleportMazeSolver || !Utils.inDungeons || !DungeonListener.incompletePuzzles.contains("Teleport Maze")) return
        val player = mc.player ?: return
        val world = mc.world ?: return
        event.packet.apply {
            when (this) {
                is PlayerPositionLookS2CPacket -> {
                    val x = this.change.position.x
                    val y = this.change.position.y
                    val z = this.change.position.z
                    if (y == 69.5 && Utils.equalsOneOf(
                            player.y,
                            69.5,
                            69.8125
                        ) && abs(x % 1) == 0.5 && abs(z % 1) == 0.5
                    ) {
                        val currPos = player.blockPos
                        val pos = BlockPos.ofFloored(x, y, z)
                        val oldTpPad = findEndPortalFrame(currPos) ?: return
                        val tpPad = findEndPortalFrame(pos) ?: return
                        steppedPads.add(oldTpPad)
                        if (tpPad !in steppedPads) {
                            steppedPads.add(tpPad)
                            val deg2Rad = PI/180
                            val magicYaw = (-this.change.yaw * deg2Rad - PI)
                            val yawX = sin(magicYaw)
                            val yawZ = cos(magicYaw)
                            val pitchVal = -cos(-this.change.pitch * deg2Rad)
                            val vec = Vec3d(yawX * pitchVal, 69.0, yawZ * pitchVal)
                            valid.clear()
                            for (i in 4..23) {
                                val bp = BlockPos.ofFloored(
                                    x + vec.x * i,
                                    vec.y,
                                    z + vec.z * i
                                )
                                val allDir = Utils.getBlocksWithinRangeAtSameY(bp, 2, 69)

                                valid.addAll(allDir.filter {
                                    it !in steppedPads && world.getBlockState(
                                        it
                                    ).block === Blocks.END_PORTAL_FRAME
                                })
                            }
                            if (DevTools.getToggle("tpmaze")) UChat.chat(valid.joinToString { it.toString() })
                            if (poss.isEmpty()) poss.addAll(valid)
                            else poss.removeAll {
                                it !in valid
                            }
                        }
                        if (DevTools.getToggle("tpmaze")) UChat.chat(
                            "current: ${player.pos}, ${player.pitch} ${player.yaw} new: ${x} ${y} ${z} - ${this.change.pitch} ${this.change.yaw} - ${
                                this.relatives.joinToString { it.name }
                            }"
                        )
                    }
                }
            }
        }
    }

    private fun findEndPortalFrame(pos: BlockPos): BlockPos? {
        return Utils.getBlocksWithinRangeAtSameY(pos, 1, 69).find {
            mc.world?.getBlockState(it)?.block === Blocks.END_PORTAL_FRAME
        }
    }

    fun onWorldRender(event: WorldDrawEvent) {
        if (!Skytils.config.teleportMazeSolver || steppedPads.isEmpty() || !DungeonListener.incompletePuzzles.contains("Teleport Maze")) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
        val matrixStack = UMatrixStack()

        for (pos in steppedPads) {
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            GlStateManager._disableCull()
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                Box(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Skytils.config.teleportMazeSolverColor
            )
            GlStateManager._enableCull()
        }

        for (pos in poss) {
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            GlStateManager._disableCull()
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                Box(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Color.GREEN.withAlpha(69),
                1f
            )
            GlStateManager._enableCull()
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
        steppedPads.clear()
        poss.clear()
    }
}