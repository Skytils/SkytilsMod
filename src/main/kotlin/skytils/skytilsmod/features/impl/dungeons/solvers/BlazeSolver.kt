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
package skytils.skytilsmod.features.impl.dungeons.solvers

import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.init.Blocks
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import java.awt.Color

/**
 * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license and modified by the Skytils team
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
class BlazeSolver {
    private var ticks = 0

    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        val player = mc.thePlayer
        val world = mc.theWorld
        if (!Utils.inDungeons || world == null || player == null) return
        if (ticks % 20 == 0) {
            ticks = 0
            if (blazeMode == 0 && orderedBlazes.size > 0) {
                Thread({
                    val blazes = mc.theWorld.getEntities(
                        EntityBlaze::class.java
                    ) { blaze: EntityBlaze? -> player.getDistanceToEntity(blaze) < 100 }
                    if (blazes.size > 10) {
                        println("More than 10 blazes, was there an update?")
                    } else if (blazes.size > 0) {
                        val diffY = 5 * (10 - blazes.size)
                        val blaze = blazes[0]
                        for (pos in Utils.getBlocksWithinRangeAtSameY(blaze.position, 13, 69)) {
                            val x = pos.x
                            val z = pos.z
                            val blockPos1 = BlockPos(x, 70 + diffY, z)
                            val blockPos2 = BlockPos(x, 69 - diffY, z)
                            if (world.getBlockState(blockPos1).block === Blocks.chest) {
                                if (world.getBlockState(blockPos1.up()).block === Blocks.iron_bars) {
                                    blazeChest = blockPos1
                                    if (blazes.size < 10) {
                                        blazeMode = -1
                                        println("Block scanning determined lowest -> highest")
                                    }
                                    break
                                }
                            } else if (world.getBlockState(blockPos2).block === Blocks.chest) {
                                if (world.getBlockState(blockPos2.up()).block === Blocks.iron_bars) {
                                    blazeChest = blockPos2
                                    if (blazes.size < 10) {
                                        blazeMode = 1
                                        println("Block scanning determined highest -> lowest")
                                    }
                                    break
                                }
                            }
                        }
                        if (blazeChest != null && blazes.size == 10) {
                            blazeMode = if (world.getBlockState(blazeChest!!.down()).block === Blocks.stone) {
                                println("Bottom block scanning determined lowest -> highest")
                                -1
                            } else {
                                println("Bottom block scanning determined highest -> lowest")
                                1
                            }
                        }
                    }
                }, "Skytils-Blaze-Orientation").start()
            }
        }
        if (ticks % 4 == 0) {
            if (Skytils.config.blazeSolver) {
                orderedBlazes.clear()
                for (entity in world.getLoadedEntityList()) {
                    if (entity is EntityArmorStand && entity.getName().contains("Blaze") && entity.getName()
                            .contains("/")
                    ) {
                        val blazeName = StringUtils.stripControlCodes(entity.getName())
                        try {
                            val health = blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length - 1).toInt()
                            val aabb = AxisAlignedBB(
                                entity.posX - 0.5,
                                entity.posY - 2,
                                entity.posZ - 0.5,
                                entity.posX + 0.5,
                                entity.posY,
                                entity.posZ + 0.5
                            )
                            val blazes = mc.theWorld.getEntitiesWithinAABB(
                                EntityBlaze::class.java, aabb
                            )
                            if (blazes.size == 0) continue
                            orderedBlazes.add(ShootableBlaze(blazes[0], health))
                        } catch (ex: NumberFormatException) {
                            ex.printStackTrace()
                        }
                    }
                }
                orderedBlazes.sortWith(Comparator.comparingInt { blaze: ShootableBlaze -> blaze.health })
            }
        }
        ticks++
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (Skytils.config.blazeSolver && Utils.inDungeons && orderedBlazes.size > 0) {
            if (blazeMode <= 0) {
                val shootableBlaze = orderedBlazes[0]
                val lowestBlaze = shootableBlaze.blaze
                RenderUtil.draw3DString(
                    Vec3(lowestBlaze.posX, lowestBlaze.posY + 3, lowestBlaze.posZ),
                    EnumChatFormatting.BOLD.toString() + "Smallest",
                    Color(255, 0, 0, 200),
                    event.partialTicks
                )
            }
            if (blazeMode >= 0) {
                val shootableBlaze = orderedBlazes[orderedBlazes.size - 1]
                val highestBlaze = shootableBlaze.blaze
                RenderUtil.draw3DString(
                    Vec3(highestBlaze.posX, highestBlaze.posY + 3, highestBlaze.posZ),
                    EnumChatFormatting.BOLD.toString() + "Biggest",
                    Color(0, 255, 0, 200),
                    event.partialTicks
                )
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        orderedBlazes.clear()
        blazeMode = 0
        blazeChest = null
    }

    class ShootableBlaze(@JvmField var blaze: EntityBlaze, var health: Int)
    companion object {
        @JvmField
        var orderedBlazes = ArrayList<ShootableBlaze>()
        @JvmField
        var blazeMode = 0
        var blazeChest: BlockPos? = null
        private val mc = Minecraft.getMinecraft()
    }
}