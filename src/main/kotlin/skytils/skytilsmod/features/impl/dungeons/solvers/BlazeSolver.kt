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
package skytils.skytilsmod.features.impl.dungeons.solvers

import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import kotlinx.coroutines.launch
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.failPrefix
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.stripControlCodes

class BlazeSolver {
    private var ticks = 0

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        val player = mc.thePlayer
        val world = mc.theWorld
        if (!Utils.inDungeons || world == null || player == null) return
        if (ticks % 20 == 0) {
            ticks = 0
            if (blazeMode == 0 && orderedBlazes.size > 0) {
                Skytils.launch {
                    val blazes = world.getEntities(
                        EntityBlaze::class.java
                    ) { blaze: EntityBlaze? -> player.getDistanceSqToEntity(blaze) < 100 * 100 }
                    if (blazes.size > 10) {
                        println("More than 10 blazes, was there an update?")
                    } else if (blazes.size > 0) {
                        val diffY = 5 * (10 - blazes.size)
                        val blaze = blazes[0]
                        val blazeX = blaze.posX.toInt()
                        val blazeZ = blaze.posZ.toInt()
                        val xRange = blazeX - 13..blazeX + 13
                        val zRange = blazeZ - 13..blazeZ + 13
                        findChest@ for (te in world.loadedTileEntityList) {
                            val y1 = 70 + diffY
                            val y2 = 69 - diffY
                            if ((te.pos.y == y1 || te.pos.y == y2) && te is TileEntityChest && te.numPlayersUsing == 0 && te.pos.x in xRange && te.pos.z in zRange
                            ) {
                                val pos = te.pos
                                if (world.getBlockState(te.pos.up()).block == Blocks.iron_bars) {
                                    if (pos.y == y1) {
                                        blazeChest = pos
                                        if (blazes.size < 10) {
                                            blazeMode = -1
                                            println("Block scanning determined lowest -> highest")
                                        }
                                        break
                                    } else {
                                        blazeChest = pos
                                        if (blazes.size < 10) {
                                            blazeMode = 1
                                            println("Block scanning determined highest -> lowest")
                                        }
                                        break@findChest
                                    }
                                }
                            }
                        }
                        if (blazeChest != null && blazes.size == 10) {
                            blazeMode = if (world.getBlockState(blazeChest!!.down()).block == Blocks.stone) {
                                println("Bottom block scanning determined lowest -> highest")
                                -1
                            } else {
                                println("Bottom block scanning determined highest -> lowest")
                                1
                            }
                        }
                    }
                }
            }
        }
        if (ticks % 4 == 0) {
            if (Skytils.config.blazeSolver) {
                orderedBlazes.clear()
                if (DungeonListener.missingPuzzles.contains("Higher Or Lower")) {
                    for (entity in world.getLoadedEntityList()) {
                        if (entity is EntityArmorStand && entity.getName().contains("Blaze") && entity.getName()
                                .contains("/")
                        ) {
                            val blazeName = entity.getName().stripControlCodes()
                            try {
                                val health =
                                    blazeName.substringAfter("/").dropLast(1).toInt()
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
                                if (blazes.isEmpty()) continue
                                orderedBlazes.add(ShootableBlaze(blazes[0], health))
                            } catch (ex: NumberFormatException) {
                                ex.printStackTrace()
                            }
                        }
                    }
                }
                orderedBlazes.sortWith { blaze1, blaze2 ->
                    val compare = blaze1.health.compareTo(blaze2.health)
                    if (compare == 0 && !impossible) {
                        impossible = true
                        UChat.chat("$failPrefix §cDetected two blazes with the exact same amount of health!")
                        val first = blaze1.blaze.health
                        val second = blaze2.blaze.health
                        if (first.toInt() == second.toInt()) return@sortWith first.compareTo(second)
                    }
                    return@sortWith compare
                }
            }
        }
        ticks++
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (Skytils.config.blazeSolver && Utils.inDungeons && orderedBlazes.size > 0) {
            val matrixStack = UMatrixStack()
            if (blazeMode < 0) {
                val shootableBlaze = orderedBlazes.first()
                val lowestBlaze = shootableBlaze.blaze
                RenderUtil.drawLabel(
                    Vec3(lowestBlaze.posX, lowestBlaze.posY + 3, lowestBlaze.posZ),
                    "§lSmallest",
                    Skytils.config.lowestBlazeColor,
                    event.partialTicks,
                    matrixStack
                )
            }
            if (blazeMode > 0) {
                val shootableBlaze = orderedBlazes.last()
                val highestBlaze = shootableBlaze.blaze
                RenderUtil.drawLabel(
                    Vec3(highestBlaze.posX, highestBlaze.posY + 3, highestBlaze.posZ),
                    "§lBiggest",
                    Skytils.config.highestBlazeColor,
                    event.partialTicks,
                    matrixStack
                )
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        orderedBlazes.clear()
        blazeMode = 0
        blazeChest = null
        impossible = false
    }

    data class ShootableBlaze(@JvmField var blaze: EntityBlaze, var health: Int)
    companion object {
        @JvmField
        var orderedBlazes = ArrayList<ShootableBlaze>()

        @JvmField
        var blazeMode = 0
        var blazeChest: BlockPos? = null
        var impossible = false
    }
}