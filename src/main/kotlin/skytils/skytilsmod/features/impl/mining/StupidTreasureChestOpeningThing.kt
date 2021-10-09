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

package skytils.skytilsmod.features.impl.mining

import gg.essential.universal.UChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.BlockChangeEvent
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.SkyblockIsland
import skytils.skytilsmod.utils.Utils
import java.awt.Color

object StupidTreasureChestOpeningThing {

    var lastFoundChest = -1L

    val sendHelpPlease = hashMapOf<BlockPos, StupidChest>()

    data class StupidChest(val pos: BlockPos, var progress: Int = 0, var particle: Vec3? = null) {
        val box = AxisAlignedBB(
            pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
            (pos.x + 1).toDouble(), (pos.y + 1).toDouble(), (pos.z + 1).toDouble()
        )
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (mc.thePlayer == null || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return
        if ((event.old.block == Blocks.air || event.old.block == Blocks.stone) && event.update.block == Blocks.chest) {
            UChat.chat("Distance ${event.pos} ${mc.thePlayer.getDistanceSq(event.pos)}")
            if (mc.thePlayer.entityBoundingBox.expand(5.0, 5.0, 5.0).isVecInside(Vec3(event.pos))) {
                val diff = System.currentTimeMillis() - lastFoundChest
                if (diff < 1000) {
                    lastFoundChest = -1L
                    UChat.chat("chest found at $diff")
                    sendHelpPlease[event.pos] = StupidChest(event.pos)
                }
            }
        } else if (event.old.block == Blocks.chest && event.update.block == Blocks.air) {
            sendHelpPlease.remove(event.pos)
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return

        when (val packet = event.packet) {
            is S02PacketChat -> {
                if (packet.chatComponent.formattedText == "§r§aYou uncovered a treasure chest!§r") {
                    lastFoundChest = System.currentTimeMillis()
                }
            }
            is S2APacketParticles -> {
                val type = packet.particleType
                val longDistance = packet.isLongDistance
                val count = packet.particleCount
                val speed = packet.particleSpeed
                val xOffset = packet.xOffset
                val yOffset = packet.yOffset
                val zOffset = packet.zOffset
                val x = packet.xCoordinate
                val y = packet.yCoordinate
                val z = packet.zCoordinate
                if (type == EnumParticleTypes.CRIT && longDistance && count == 1 && speed == 0f && xOffset == 0f && yOffset == 0f && zOffset == 0f) {
                    val probable = sendHelpPlease.entries.minByOrNull {
                        it.key.distanceSq(x, y, z)
                    } ?: return

                    if (probable.key.distanceSqToCenter(x, y, z) < 2.5) {
                        probable.value.particle = Vec3(x, y, z)
                        UChat.chat("$count ${if (longDistance) "long-distance" else ""} ${type.particleName} particles with $speed speed at $x, $y, $z, offset by $xOffset, $yOffset, $zOffset")
                    }
                }
            }
            is S29PacketSoundEffect -> {
                val sound = packet.soundName
                val pitch = packet.pitch
                val volume = packet.volume
                val x = packet.x
                val y = packet.y
                val z = packet.z
                if (volume == 1f && pitch == 1f && Utils.equalsOneOf(sound, "random.orb", "mob.villager.no")) {
                    val probable = sendHelpPlease.entries.minByOrNull {
                        it.key.distanceSq(x, y, z)
                    } ?: return

                    UChat.chat("$sound distance from chest ${probable.key.distanceSq(x, y, z)}")
                    if (probable.key.distanceSq(x, y, z) < 6.9 * 6.9) {
                        if (sound == "random.orb") probable.value.progress++
                        else probable.value.progress = 0
                        UChat.chat("sound $sound, $pitch pitch, $volume volume, at $x, $y, $z")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

        for ((pos, chest) in sendHelpPlease) {
            GlStateManager.disableCull()
            GlStateManager.disableDepth()
            RenderUtil.drawFilledBoundingBox(
                chest.box.offset(-viewerX, -viewerY, -viewerZ).expand(0.01, 0.01, 0.01),
                Color(255, 0, 0, 69),
                1f
            )
            RenderUtil.draw3DString(
                Vec3(pos).addVector(0.5, 1.5, 0.5),
                "${chest.progress}/5",
                Color.ORANGE,
                event.partialTicks
            )
            if (chest.particle != null) {
                RenderUtil.drawFilledBoundingBox(
                    AxisAlignedBB(
                        chest.particle!!.xCoord,
                        chest.particle!!.yCoord,
                        chest.particle!!.zCoord,
                        chest.particle!!.xCoord + 0.1,
                        chest.particle!!.yCoord + 0.1,
                        chest.particle!!.zCoord + 0.1
                    ).offset(-viewerX, -viewerY, -viewerZ),
                    Color(255, 0, 255, 69),
                    1f
                )
            }
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        sendHelpPlease.clear()
        lastFoundChest = -1L
    }

}