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

//#if MC >= 11404
//$$ import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorWorld
//#endif
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.entity.LivingEntityDeathEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.RenderWorldPostEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.DungeonPuzzleDiscoveredEvent
import gg.skytils.skytilsmod._event.DungeonPuzzleResetEvent
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.stripControlCodes
import kotlinx.coroutines.launch
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.random.Random

object BlazeSolver : EventSubscriber {
    var orderedBlazes = arrayListOf<ShootableBlaze>()
    var blazeMode = 0
    var blazeChest: BlockPos? = null
    var impossible = false
    var lastKilledBlazeHp = 0

    init {
        tickTimer(4, repeats = true) {
            if (Skytils.config.blazeSolver && Utils.inDungeons && DungeonListener.missingPuzzles.contains(
                    "Higher Or Lower"
                )
            ) {
                calcOrder()
            }
        }
        tickTimer(20, repeats = true) {
            if (Skytils.config.blazeSolver && Utils.inDungeons && DungeonListener.missingPuzzles.contains(
                    "Higher Or Lower"
                )
            ) {
                detectOrientation()
            }
        }
    }

    override fun setup() {
        register(::onWorldRender)
        register(::onWorldChange)
        register(::onEntityDeath)
        register(::onPuzzleDiscovered)
        register(::onPuzzleReset)
    }

    fun detectOrientation() {
        if (blazeMode == 0 && orderedBlazes.size > 0 && mc.thePlayer != null) {
            Skytils.launch {
                //#if MC<=11202
                val blazes = mc.theWorld.getEntities(
                    EntityBlaze::class.java
                ) { blaze: EntityBlaze? -> mc.thePlayer.getDistanceSqToEntity(blaze) < 100 * 100 }
                //#else
                //$$ val blazes = mc.world!!.getEntitiesByClass(BlazeEntity::class.java, mc.player!!.boundingBox.expand(100.0)) { true }
                //#endif
                if (blazes.size > 10) {
                    println("More than 10 blazes, was there an update?")
                } else if (blazes.size > 0) {
                    val diffY = 5 * (10 - blazes.size)
                    val blaze = blazes[0]
                    val blazeX = blaze.posX.toInt()
                    val blazeZ = blaze.posZ.toInt()
                    val xRange = blazeX - 13..blazeX + 13
                    val zRange = blazeZ - 13..blazeZ + 13
                    val y1 = 70 + diffY
                    val y2 = 69 - diffY
                    //#if MC<=11202
                    findChest@ for (te in mc.theWorld.loadedTileEntityList) {
                    //#else
                    //$$ findChest@ for (te in (mc.world!! as AccessorWorld).blockEntityTickers) {
                    //#endif
                        val pos = te.pos
                        if ((pos.y == y1 || pos.y == y2) && pos.x in xRange && pos.z in zRange) {
                            //#if MC<=11202
                            if (te is TileEntityChest && te.numPlayersUsing == 0) {
                            //#else
                            //$$ if (te is ChestBlockEntity && ChestBlockEntity.getPlayersLookingInChestCount(mc.world!!, pos) == 0) {
                            //#endif
                                if (mc.theWorld!!.getBlockState(pos.up()).block == Blocks.iron_bars) {
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
                    }
                    if (blazeChest != null && blazes.size == 10) {
                        blazeMode = if (mc.theWorld!!.getBlockState(blazeChest!!.down()).block == Blocks.stone) {
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

    fun calcOrder() {
        if (mc.theWorld == null) return
        orderedBlazes.clear()
        for (entity in mc.theWorld!!.loadedEntityList) {
            //#if MC<=11202
            if (entity is EntityArmorStand && entity.name.contains("Blaze") && entity.name.contains("/")) {
                val blazeName = entity.name.stripControlCodes()
            //#else
            //$$ if (entity is ArmorStandEntity && entity.name.string.contains("Blaze") && entity.name.string.contains("/")) {
            //$$   val blazeName = entity.name.string.stripControlCodes()
            //#endif
                try {
                    val health =
                        blazeName.substringAfter("/").dropLast(1).replace(",", "").toInt()
                    if (lastKilledBlazeHp != 0 && blazeMode != 0) {
                        if (blazeMode == -1 && health <= lastKilledBlazeHp) continue
                        if (blazeMode == 1 && health >= lastKilledBlazeHp) continue
                    }
                    val aabb = AxisAlignedBB(
                        entity.posX - 0.5,
                        entity.posY - 2,
                        entity.posZ - 0.5,
                        entity.posX + 0.5,
                        entity.posY,
                        entity.posZ + 0.5
                    )
                    //#if MC<=11202
                    val blazes = mc.theWorld.getEntitiesWithinAABB(
                        EntityBlaze::class.java, aabb
                    )
                    //#else
                    //$$ val blazes = mc.world!!.getEntitiesByClass(BlazeEntity::class.java, aabb) { true}
                    //#endif
                    if (blazes.isEmpty()) continue
                    orderedBlazes.add(ShootableBlaze(blazes[0], health))
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                }
            }
        }
        orderedBlazes.sortWith { blaze1, blaze2 ->
            if (SuperSecretSettings.bennettArthur) return@sortWith Random.nextInt(-1, 2)
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

    fun onPuzzleDiscovered(event: DungeonPuzzleDiscoveredEvent) {
        if (event.puzzle == "Higher Or Lower") {
            calcOrder()
            detectOrientation()
        }
    }

    fun onEntityDeath(event: LivingEntityDeathEvent) {
        if (event.entity is EntityBlaze && orderedBlazes.isNotEmpty()) {
            orderedBlazes.firstOrNull { it.blaze == event.entity }?.let {
                orderedBlazes.remove(it)
                lastKilledBlazeHp = it.health
            }
        }
    }

    fun onWorldRender(event: RenderWorldPostEvent) {
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
                if (Skytils.config.lineToNextBlaze) {
                    val secondLowestBlaze = orderedBlazes.getOrNull(1)?.blaze ?: return
                    RenderUtil.draw3DLine(
                        Vec3(lowestBlaze.posX, lowestBlaze.posY + 1.5, lowestBlaze.posZ),
                        Vec3(secondLowestBlaze.posX, secondLowestBlaze.posY + 1.5, secondLowestBlaze.posZ),
                        5,
                        Skytils.config.lineToNextBlazeColor,
                        event.partialTicks,
                        matrixStack
                    )
                }
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
                if (Skytils.config.lineToNextBlaze) {
                    val secondHighestBlaze = orderedBlazes.getOrNull(orderedBlazes.size - 2)?.blaze ?: return
                    RenderUtil.draw3DLine(
                        Vec3(highestBlaze.posX, highestBlaze.posY + 1.5, highestBlaze.posZ),
                        Vec3(secondHighestBlaze.posX, secondHighestBlaze.posY + 1.5, secondHighestBlaze.posZ),
                        5,
                        Skytils.config.lineToNextBlazeColor,
                        event.partialTicks,
                        matrixStack
                    )
                }
            }
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
        orderedBlazes.clear()
        blazeMode = 0
        blazeChest = null
        impossible = false
        lastKilledBlazeHp = 0
    }

    fun onPuzzleReset(event: DungeonPuzzleResetEvent) {
        if (event.puzzle == "Higher Or Lower") {
            orderedBlazes.clear()
            impossible = false
            lastKilledBlazeHp = 0
            calcOrder()
        }
    }

    data class ShootableBlaze(@JvmField var blaze: EntityBlaze, var health: Int)
}