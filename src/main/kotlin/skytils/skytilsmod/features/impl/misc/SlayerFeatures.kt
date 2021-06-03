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
package skytils.skytilsmod.features.impl.misc

import net.minecraft.block.BlockHalfStoneSlab
import net.minecraft.block.BlockHalfWoodSlab
import net.minecraft.block.BlockPlanks
import net.minecraft.block.BlockStoneSlab
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.GuiManager.Companion.createTitle
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.RenderUtil.drawOutlinedBoundingBox
import skytils.skytilsmod.utils.ScoreboardUtil.cleanSB
import skytils.skytilsmod.utils.ScoreboardUtil.sidebarLines
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.stripControlCodes
import java.awt.Color

class SlayerFeatures {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock) return
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null) return
        if (ticks % 4 == 0) {
            if (Skytils.config.rev5TNTPing) {
                if (sidebarLines.any { l: String -> cleanSB(l).contains("Slay the boss!") }) {
                    var under: BlockPos? = null
                    if (mc.thePlayer.onGround) {
                        under = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)
                    } else {
                        for (i in (mc.thePlayer.posY - 0.5f).toInt() downTo 0 step 1) {
                            val test = BlockPos(mc.thePlayer.posX, i.toDouble(), mc.thePlayer.posZ)
                            if (mc.theWorld.getBlockState(test).block !== Blocks.air) {
                                under = test
                                break
                            }
                        }
                    }
                    if (under != null) {
                        val blockUnder = mc.theWorld.getBlockState(under)
                        var isDanger = false
                        if (blockUnder.block === Blocks.stone_slab && blockUnder.getValue(BlockHalfStoneSlab.VARIANT) == BlockStoneSlab.EnumType.QUARTZ) {
                            isDanger = true
                        } else if (blockUnder.block === Blocks.quartz_stairs || blockUnder.block === Blocks.acacia_stairs) {
                            isDanger = true
                        } else if (blockUnder.block === Blocks.wooden_slab && blockUnder.getValue(BlockHalfWoodSlab.VARIANT) == BlockPlanks.EnumType.ACACIA) {
                            isDanger = true
                        } else if (blockUnder.block === Blocks.stained_hardened_clay) {
                            val color = Blocks.stained_hardened_clay.getMetaFromState(blockUnder)
                            if (color == 0 || color == 8 || color == 14) isDanger = true
                        } else if (blockUnder.block === Blocks.bedrock) {
                            isDanger = true
                        }
                        if (isDanger) {
                            mc.thePlayer.playSound("random.orb", 1f, 1f)
                        }
                    }
                }
            }
            if (Skytils.config.showRNGMeter) {
                for (index in 0 until sidebarLines.size - 1) {
                    if (cleanSB(sidebarLines[index]) == "Slayer Quest") {
                        val boss = cleanSB(sidebarLines[(index - 1).coerceAtLeast(0)].stripControlCodes())
                        if (boss.startsWith("Revenant Horror")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.revRNG,
                                    ChatComponentText("§2§lRevenant Horror RNG§r - §d${Skytils.config.revRNG}%")
                                ), true
                            )
                            break
                        }
                        if (boss.startsWith("Tarantula Broodfather")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.taraRNG,
                                    ChatComponentText("§8§lTarantula Broodfather RNG§r - §d${Skytils.config.taraRNG}%")
                                ), true
                            )
                            break
                        }
                        if (boss.startsWith("Sven Packmaster")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.svenRNG,
                                    ChatComponentText("§7§lSven Packmaster RNG§r - §d${Skytils.config.svenRNG}%")
                                ), true
                            )
                            break
                        }
                        if (boss.startsWith("Voidgloom Seraph")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.voidRNG,
                                    ChatComponentText("§5§lVoidgloom Seraph RNG§r - §d${Skytils.config.voidRNG}%")
                                ), true
                            )
                            break
                        }
                    }
                }
            }
            ticks = 0
        }
        ticks++
    }

    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<EntityLivingBase?>) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityArmorStand) {
            val entity = event.entity as EntityArmorStand
            if (!entity.hasCustomName()) return
            val name = entity.displayName.unformattedText
            if (Skytils.config.slayerBossHitbox && name.endsWith("§c❤") && !name.endsWith("§e0§c❤") && !mc.renderManager.isDebugBoundingBox) {
                val x = entity.posX
                val y = entity.posY
                val z = entity.posZ
                for (zombieBoss in ZOMBIE_BOSSES) {
                    if (name.contains(zombieBoss)) {
                        drawOutlinedBoundingBox(
                            AxisAlignedBB(x - 0.5, y - 2, z - 0.5, x + 0.5, y, z + 0.5),
                            Color(0, 255, 255, 255),
                            3f,
                            1f
                        )
                        return
                    }
                }
                for (spiderBoss in SPIDER_BOSSES) {
                    if (name.contains(spiderBoss)) {
                        drawOutlinedBoundingBox(
                            AxisAlignedBB(
                                x - 0.625,
                                y - 1,
                                z - 0.625,
                                x + 0.625,
                                y - 0.25,
                                z + 0.625
                            ), Color(0, 255, 255, 255), 3f, 1f
                        )
                        return
                    }
                }
                for (wolfBoss in WOLF_BOSSES) {
                    if (name.contains(wolfBoss)) {
                        drawOutlinedBoundingBox(
                            AxisAlignedBB(x - 0.5, y - 1, z - 0.5, x + 0.5, y, z + 0.5),
                            Color(0, 255, 255, 255),
                            3f,
                            1f
                        )
                        return
                    }
                }
                for (endermanBoss in ENDERMAN_BOSSES) {
                    if (name.contains(endermanBoss)) {
                        drawOutlinedBoundingBox(
                            AxisAlignedBB(x - 0.5, y - 3, z - 0.5, x + 0.5, y, z + 0.5),
                            Color(0, 255, 255, 255),
                            3f,
                            1f
                        )
                        return
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            if (Skytils.config.slayerMinibossSpawnAlert && packet.soundName == "random.explode" && packet.volume == 0.6f && packet.pitch == 9 / 7f && GuiManager.title != "§cMINIBOSS") {
                createTitle("§cMINIBOSS", 20)
            }
        }
        if (event.packet is S02PacketChat) {
            if (event.packet.chatComponent.unformattedText.stripControlCodes().trim().startsWith("RNGesus Meter")) {
                val rngMeter =
                    event.packet.chatComponent.unformattedText.stripControlCodes().replace(RNGRegex, "").toFloat()
                for (index in 0 until sidebarLines.size - 1) {
                    if (cleanSB(sidebarLines[index]) == "Slayer Quest") {
                        val boss = cleanSB(sidebarLines[(index - 1).coerceAtLeast(0)].stripControlCodes())
                        if (boss.startsWith("Revenant Horror")) {
                            Skytils.config.revRNG = rngMeter
                            break
                        }
                        if (boss.startsWith("Tarantula Broodfather")) {
                            Skytils.config.taraRNG = rngMeter
                            break
                        }
                        if (boss.startsWith("Sven Packmaster")) {
                            Skytils.config.svenRNG = rngMeter
                            break
                        }
                        if (boss.startsWith("Voidgloom Seraph")) {
                            Skytils.config.voidRNG = rngMeter
                            break
                        }
                    }
                }
            }
        }
    }

    private class RNGMeter(val max: Float, val current: Float, val name: IChatComponent) : IBossDisplayData {

        override fun getMaxHealth(): Float {
            return max
        }

        override fun getHealth(): Float {
            return current
        }

        override fun getDisplayName(): IChatComponent {
            return name
        }

    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private var ticks = 0
        private val ZOMBIE_BOSSES = arrayOf(
            "§cRevenant Sycophant",
            "§cRevenant Champion",
            "§4Deformed Revenant",
            "§cAtoned Champion",
            "§4Atoned Revenant"
        )
        private val SPIDER_BOSSES = arrayOf("§cTarantula Vermin", "§cTarantula Beast", "§4Mutant Tarantula")
        private val WOLF_BOSSES = arrayOf("§cPack Enforcer", "§cSven Follower", "§4Sven Alpha")
        private val ENDERMAN_BOSSES = arrayOf("Voidling Devotee", "Voidling Radical", "Voidgloom Maniac")
        private val RNGRegex = Regex("[^0-9.]")
    }
}