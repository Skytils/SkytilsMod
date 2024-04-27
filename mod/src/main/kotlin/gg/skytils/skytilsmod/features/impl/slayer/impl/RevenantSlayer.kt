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

package gg.skytils.skytilsmod.features.impl.slayer.impl

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.slayer.base.Slayer
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.block.BlockHalfStoneSlab
import net.minecraft.block.BlockHalfWoodSlab
import net.minecraft.block.BlockPlanks
import net.minecraft.block.BlockStoneSlab
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class RevenantSlayer(entity: EntityZombie) :
    Slayer<EntityZombie>(entity, "Revenant Horror", "§c☠ §bRevenant Horror", "§c☠ §fAtoned Horror") {

    override fun set() {
        rev5PingTask.start()
    }

    override fun unset() {
        rev5PingTask.cancel()
        rev5PingTask = createrev5PingTask()
    }

    companion object {
        private fun createrev5PingTask() =
            tickTimer(4, repeats = true, register = false) {
                if (Utils.inSkyblock && Config.rev5TNTPing && Skytils.mc.thePlayer != null) {
                    if (SlayerFeatures.hasSlayerText) {
                        var under: BlockPos? = null
                        if (Skytils.mc.thePlayer.onGround) {
                            under = BlockPos(
                                Skytils.mc.thePlayer.posX,
                                Skytils.mc.thePlayer.posY - 0.5,
                                Skytils.mc.thePlayer.posZ
                            )
                        } else {
                            for (i in (Skytils.mc.thePlayer.posY - 0.5f).toInt() downTo 0 step 1) {
                                val test = BlockPos(Skytils.mc.thePlayer.posX, i.toDouble(), Skytils.mc.thePlayer.posZ)
                                if (Skytils.mc.theWorld.getBlockState(test).block !== Blocks.air) {
                                    under = test
                                    break
                                }
                            }
                        }
                        if (under != null) {
                            val blockUnder = Skytils.mc.theWorld.getBlockState(under)
                            val isDanger = when {
                                blockUnder.block === Blocks.stone_slab && blockUnder.getValue(BlockHalfStoneSlab.VARIANT) == BlockStoneSlab.EnumType.QUARTZ -> true
                                blockUnder.block === Blocks.quartz_stairs || blockUnder.block === Blocks.acacia_stairs -> true
                                blockUnder.block === Blocks.wooden_slab && blockUnder.getValue(BlockHalfWoodSlab.VARIANT) == BlockPlanks.EnumType.ACACIA -> true
                                blockUnder.block === Blocks.stained_hardened_clay -> {
                                    val color = Blocks.stained_hardened_clay.getMetaFromState(blockUnder)
                                    color == 0 || color == 8 || color == 14
                                }

                                blockUnder.block === Blocks.bedrock -> true
                                else -> false
                            }
                            if (isDanger) {
                                SoundQueue.addToQueue("random.orb", 1f)
                            }
                        }
                    }
                }
            }
        private var rev5PingTask = createrev5PingTask()
    }
}