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

package gg.skytils.skytilsmod.features.impl.mining

import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod._event.PacketSendEvent
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.utils.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

object CHTreasureChestHelper :EventSubscriber {

    var lastFoundChest = -1L
    var found = 0

    var lastOpenedChest: BlockPos? = null

    val chests = ConcurrentHashMap<BlockPos, CHTreasureChest>()

    data class CHTreasureChest(
        val pos: BlockPos,
        var progress: Int = 0,
        val time: Long = System.currentTimeMillis(),
        var particle: Vec3? = null
    ) {
        val box = AxisAlignedBB(
            pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
            (pos.x + 1).toDouble(), (pos.y + 1).toDouble(), (pos.z + 1).toDouble()
        )

        var particleBox: AxisAlignedBB? = null
    }

    override fun setup() {
        register(::onBlockChange)
    }

    fun onBlockChange(event: BlockChangeEvent) {
        if (!Skytils.config.chTreasureHelper || mc.thePlayer == null || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return
        if (((event.old.block == Blocks.air || event.old.block == Blocks.stone) && event.update.block == Blocks.chest)) {
            printDevMessage("Distance ${event.pos} ${mc.thePlayer.getDistanceSq(event.pos)}", "chtreasure")
            if (mc.thePlayer.entityBoundingBox.expand(8.0, 8.0, 8.0).isPosInside(event.pos)) {
                val diff = System.currentTimeMillis() - lastFoundChest
                if (diff < 1000 && found > 0) {
                    found--
                    chests[event.pos] = CHTreasureChest(event.pos)
                    printDevMessage("chest found at $diff", "chtreasure")
                } else found = 0
            }
        } else if (event.old.block == Blocks.chest && event.update.block == Blocks.air) {
            chests.remove(event.pos)
        }
    }

    fun onSendPacket(event: PacketSendEvent<*>) {
        if (chests.isEmpty() || !Skytils.config.chTreasureHelper || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return

        when (val packet = event.packet) {
            is C08PacketPlayerBlockPlacement -> {
                if (chests.containsKey(packet.position)) {
                    lastOpenedChest = packet.position
                }
            }
        }
    }

    fun onReceivePacket(event: PacketReceiveEvent<*>) {
        if (!Skytils.config.chTreasureHelper || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return

        when (val packet = event.packet) {
            is S02PacketChat -> {
                val formatted = packet.chatComponent.formattedText
                if (packet.type != 2.toByte()) {
                    if (formatted == "§r§aYou uncovered a treasure chest!§r") {
                        lastFoundChest = System.currentTimeMillis()
                        found++
                    } else if (lastOpenedChest != null && Utils.equalsOneOf(
                            formatted,
                            "§r§6You have successfully picked the lock on this chest!",
                            "§r§aThe remaining contents of this treasure chest were placed in your inventory"
                        )
                    ) {
                        chests.remove(lastOpenedChest)
                        lastOpenedChest = null
                    }
                }
            }
            is S2APacketParticles -> {
                packet.apply {
                    if (type == EnumParticleTypes.CRIT && isLongDistance && count == 1 && speed == 0f && xOffset == 0f && yOffset == 0f && zOffset == 0f) {
                        val probable = chests.values.minByOrNull {
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
                                "$count ${if (isLongDistance) "long-distance" else ""} ${type.particleName} particles with $speed speed at $x, $y, $z, offset by $xOffset, $yOffset, $zOffset",
                                "chtreasure"
                            )
                        }
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
                    ) && chests.isNotEmpty()
                ) {
                    val probable = chests.values.minByOrNull {
                        val rot = mc.thePlayer.getRotationFor(it.pos)
                        abs(rot.first - mc.thePlayer.rotationYaw) + abs(rot.second - mc.thePlayer.rotationPitch)
                    } ?: return
                    if (sound == "random.orb") probable.progress++
                    else probable.progress = 0
                    printDevMessage("sound $sound, $pitch pitch, $volume volume, at $x, $y, $z", "chtreasure")
                }
            }
        }
    }

    fun onRender(event: WorldDrawEvent) {
        if (!Skytils.config.chTreasureHelper || chests.isEmpty() || SBInfo.mode != SkyblockIsland.CrystalHollows.mode) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
        val matrixStack = UMatrixStack()
        val time = System.currentTimeMillis()
        chests.entries.removeAll { (pos, chest) ->
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                chest.box.offset(-viewerX, -viewerY, -viewerZ).expand(0.01, 0.01, 0.01),
                Color(255, 0, 0, 69),
                1f
            )
            RenderUtil.drawLabel(
                Vec3(pos).addVector(0.5, 1.5, 0.5),
                "${chest.progress}/5",
                Color.ORANGE,
                event.partialTicks,
                matrixStack
            )
            if (chest.particleBox != null) {
                RenderUtil.drawFilledBoundingBox(
                    matrixStack,
                    chest.particleBox!!.offset(-viewerX, -viewerY, -viewerZ),
                    Color(255, 0, 255, 69),
                    1f
                )
            }
            GlStateManager.enableCull()
            return@removeAll (time - chest.time) > (5 * 1000 * 60)
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
        chests.clear()
        lastFoundChest = -1L
    }

}