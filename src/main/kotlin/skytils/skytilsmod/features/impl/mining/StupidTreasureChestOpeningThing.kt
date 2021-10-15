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

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.impl.BlockChangeEvent
import skytils.skytilsmod.events.impl.PacketEvent
import skytils.skytilsmod.mixins.transformers.accessors.AccessorMinecraft
import skytils.skytilsmod.utils.*
import java.awt.Color

object StupidTreasureChestOpeningThing {

    var lastFoundChest = -1L

    var iLovePain: BlockPos? = null

    val sendHelpPlease = hashMapOf<BlockPos, StupidChest>()

    data class StupidChest(val pos: BlockPos, var progress: Int = 0, var particle: Vec3? = null) {
        val box = AxisAlignedBB(
            pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
            (pos.x + 1).toDouble(), (pos.y + 1).toDouble(), (pos.z + 1).toDouble()
        )

        var particleBox: AxisAlignedBB? = null
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!Skytils.config.chTreasureHelper || mc.thePlayer == null || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return
        if ((event.old.block == Blocks.air || event.old.block == Blocks.stone) && event.update.block == Blocks.chest) {
            printDevMessage("Distance ${event.pos} ${mc.thePlayer.getDistanceSq(event.pos)}", "chtreasure")
            if (mc.thePlayer.entityBoundingBox.expand(8.0, 8.0, 8.0).isPosInside(event.pos)) {
                val diff = System.currentTimeMillis() - lastFoundChest
                if (diff < 1000) {
                    lastFoundChest = -1L
                    sendHelpPlease[event.pos] = StupidChest(event.pos)
                    printDevMessage("chest found at $diff", "chtreasure")
                }
            }
        } else if (event.old.block == Blocks.chest && event.update.block == Blocks.air) {
            sendHelpPlease.remove(event.pos)
        }
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.SendEvent) {
        if (sendHelpPlease.isEmpty() || !Skytils.config.chTreasureHelper || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return

        when (val packet = event.packet) {
            is C08PacketPlayerBlockPlacement -> {
                if (sendHelpPlease.containsKey(packet.position)) {
                    iLovePain = packet.position
                }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (!Skytils.config.chTreasureHelper || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return

        when (val packet = event.packet) {
            is S02PacketChat -> {
                val formatted = packet.chatComponent.formattedText
                if (formatted == "§r§aYou uncovered a treasure chest!§r") {
                    lastFoundChest = System.currentTimeMillis()
                } else if (iLovePain != null && Utils.equalsOneOf(
                        formatted,
                        "§r§6You have successfully picked the lock on this chest!",
                        "§r§aThe remaining contents of this treasure chest were placed in your inventory"
                    )
                ) {
                    sendHelpPlease.remove(iLovePain)
                    iLovePain = null
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
                    val probable = sendHelpPlease.values.minByOrNull {
                        it.pos.distanceSq(x, y, z)
                    } ?: return

                    if (probable.pos.distanceSqToCenter(x, y, z) < 2.5) {
                        probable.particle = Vec3(x, y, z)
                        probable.particleBox = AxisAlignedBB(
                            probable.particle!!.xCoord,
                            probable.particle!!.yCoord,
                            probable.particle!!.zCoord,
                            probable.particle!!.xCoord + 0.1,
                            probable.particle!!.yCoord + 0.1,
                            probable.particle!!.zCoord + 0.1
                        )
                        printDevMessage(
                            "$count ${if (longDistance) "long-distance" else ""} ${type.particleName} particles with $speed speed at $x, $y, $z, offset by $xOffset, $yOffset, $zOffset",
                            "chtreasure"
                        )
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
                if (volume == 1f && pitch == 1f && Utils.equalsOneOf(
                        sound,
                        "random.orb",
                        "mob.villager.no"
                    ) && sendHelpPlease.isNotEmpty()
                ) {
                    val rt = mc.thePlayer.rayTrace(25.0, (mc as AccessorMinecraft).timer.renderPartialTicks)
                    if (rt.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        val probable = sendHelpPlease[rt.blockPos] ?: return
                        if (sound == "random.orb") probable.progress++
                        else probable.progress = 0
                        printDevMessage("sound $sound, $pitch pitch, $volume volume, at $x, $y, $z", "chtreasure")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!Skytils.config.chTreasureHelper || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return
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
            if (chest.particleBox != null) {
                RenderUtil.drawFilledBoundingBox(
                    chest.particleBox!!.offset(-viewerX, -viewerY, -viewerZ),
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