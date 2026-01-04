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
import gg.skytils.skytilsmod.utils.multiplatform.blockPos
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.block.Blocks
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos

class RevenantSlayer(entity: ZombieEntity) :
    Slayer<ZombieEntity>(entity, "Revenant Horror", "§c☠ §bRevenant Horror", "§c☠ §fAtoned Horror") {

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
                val player = Skytils.mc.player ?: return@tickTimer
                if (Utils.inSkyblock && Config.rev5TNTPing && player != null) {
                    if (SlayerFeatures.hasSlayerText) {
                        var under: BlockPos? = null
                        if (player.isOnGround) {
                            under = blockPos(
                                player.x,
                                player.y - 0.5,
                                player.z
                            )
                        } else {
                            for (i in (player.y - 0.5f).toInt() downTo 0 step 1) {
                                val test = blockPos(player.x, i.toDouble(), player.z)
                                if (Skytils.mc.world?.getBlockState(test)?.block !== Blocks.AIR) {
                                    under = test
                                    break
                                }
                            }
                        }
                        if (under != null) {
                            val blockUnder = Skytils.mc.world?.getBlockState(under) ?: return@tickTimer
                            val isDanger = when {
                                blockUnder.block === Blocks.QUARTZ_SLAB -> true
                                blockUnder.block === Blocks.QUARTZ_STAIRS || blockUnder.block === Blocks.ACACIA_STAIRS -> true
                                blockUnder.block == Blocks.ACACIA_SLAB -> true
                                blockUnder.isIn(BlockTags.TERRACOTTA) -> {
                                    val color = blockUnder.block.defaultMapColor.id
                                    color == 0 || color == 8 || color == 14
                                }

                                blockUnder.block === Blocks.BEDROCK -> true
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