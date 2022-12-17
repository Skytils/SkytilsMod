/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package gg.skytils.skytilsmod.features.impl.slayer.slayers.impl

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.features.impl.slayer.SlayerManager
import gg.skytils.skytilsmod.features.impl.slayer.slayers.Slayer
import net.minecraft.block.BlockHalfStoneSlab
import net.minecraft.block.BlockHalfWoodSlab
import net.minecraft.block.BlockPlanks
import net.minecraft.block.BlockStoneSlab
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent


class RevenantSlayer(entity: EntityZombie) :
    Slayer<EntityZombie>(entity, "Revenant Horror", "§c☠ §bRevenant Horror", "§c☠ §fAtoned Horror") {

    var ticks = 0

    override fun tick(event: TickEvent.ClientTickEvent) {
        if (++ticks % 4 != 0) return
        if (Skytils.config.rev5TNTPing) {
            if (SlayerManager.hasSlayerText) {
                var under: BlockPos? = null
                if (Skytils.mc.thePlayer.onGround) {
                    under =
                        BlockPos(Skytils.mc.thePlayer.posX, Skytils.mc.thePlayer.posY - 0.5, Skytils.mc.thePlayer.posZ)
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
        ticks = 0
    }
}
