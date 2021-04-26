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

import com.google.common.collect.ImmutableSet
import net.minecraft.block.Block
import net.minecraft.block.BlockLever
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import java.awt.Color

/**
 * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license and modified by the Skytils team
 *
 * @author bowser0000
 * @link https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 */
class WaterBoardSolver {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.theWorld == null || mc.thePlayer == null) return
        if (!Skytils.config.waterBoardSolver) return
        val player = mc.thePlayer
        val world: World = mc.theWorld
        if (ticks % 4 == 0) {
            if (variant == -1 && (workerThread == null || !workerThread!!.isAlive || workerThread!!.isInterrupted)) {
                workerThread = Thread({
                    prevInWaterRoom = inWaterRoom
                    inWaterRoom = false
                    var foundPiston = false
                    for (potentialPiston in Utils.getBlocksWithinRangeAtSameY(player.position, 13, 54)) {
                        if (world.getBlockState(potentialPiston).block === Blocks.sticky_piston) {
                            foundPiston = true
                            break
                        }
                    }
                    if (foundPiston) {
                        if (chestPos == null) {
                            for (potentialChestPos in Utils.getBlocksWithinRangeAtSameY(player.position, 25, 56)) {
                                if (world.getBlockState(potentialChestPos).block === Blocks.chest) {
                                    if (world.getBlockState(potentialChestPos.down()).block === Blocks.stone && world.getBlockState(
                                            potentialChestPos.up(2)
                                        ).block === Blocks.stained_glass
                                    ) {
                                        for (direction in EnumFacing.HORIZONTALS) {
                                            if (world.getBlockState(
                                                    potentialChestPos.offset(direction.opposite, 3).down(2)
                                                ).block === Blocks.sticky_piston && world.getBlockState(
                                                    potentialChestPos.offset(direction, 2)
                                                ).block === Blocks.stone
                                            ) {
                                                chestPos = potentialChestPos
                                                println("Water board chest is at $chestPos")
                                                roomFacing = direction
                                                println("Water board room is facing $direction")
                                                break
                                            }
                                        }
                                        break
                                    }
                                }
                            }
                        }
                        if (chestPos == null) return@Thread
                        for (blockPos in Utils.getBlocksWithinRangeAtSameY(player.position, 25, 82)) {
                            if (world.getBlockState(blockPos).block === Blocks.piston_head) {
                                inWaterRoom = true
                                if (!prevInWaterRoom) {
                                    var foundGold = false
                                    var foundClay = false
                                    var foundEmerald = false
                                    var foundQuartz = false
                                    var foundDiamond = false
                                    val x = blockPos.x
                                    val z = blockPos.z

                                    // Detect first blocks near water stream
                                    for (puzzleBlockPos in BlockPos.getAllInBox(
                                        BlockPos(x + 1, 78, z + 1),
                                        BlockPos(x - 1, 77, z - 1)
                                    )) {
                                        val block = world.getBlockState(puzzleBlockPos).block
                                        when {
                                            block === Blocks.gold_block -> {
                                                foundGold = true
                                            }
                                            block === Blocks.hardened_clay -> {
                                                foundClay = true
                                            }
                                            block === Blocks.emerald_block -> {
                                                foundEmerald = true
                                            }
                                            block === Blocks.quartz_block -> {
                                                foundQuartz = true
                                            }
                                            block === Blocks.diamond_block -> {
                                                foundDiamond = true
                                            }
                                        }
                                    }
                                    if (foundGold && foundClay) {
                                        variant = 0
                                    } else if (foundEmerald && foundQuartz) {
                                        variant = 1
                                    } else if (foundQuartz && foundDiamond) {
                                        variant = 2
                                    } else if (foundGold && foundQuartz) {
                                        variant = 3
                                    }
                                    when (variant) {
                                        0 -> {
                                            solutions[WoolColor.PURPLE] = ImmutableSet.of(
                                                LeverBlock.QUARTZ,
                                                LeverBlock.GOLD,
                                                LeverBlock.DIAMOND,
                                                LeverBlock.CLAY
                                            )
                                            solutions[WoolColor.ORANGE] =
                                                ImmutableSet.of(LeverBlock.GOLD, LeverBlock.COAL, LeverBlock.EMERALD)
                                            solutions[WoolColor.BLUE] = ImmutableSet.of(
                                                LeverBlock.QUARTZ,
                                                LeverBlock.GOLD,
                                                LeverBlock.EMERALD,
                                                LeverBlock.CLAY
                                            )
                                            solutions[WoolColor.GREEN] = ImmutableSet.of(LeverBlock.EMERALD)
                                            solutions[WoolColor.RED] = ImmutableSet.of()
                                        }
                                        1 -> {
                                            solutions[WoolColor.PURPLE] = ImmutableSet.of(LeverBlock.COAL)
                                            solutions[WoolColor.ORANGE] = ImmutableSet.of(
                                                LeverBlock.QUARTZ,
                                                LeverBlock.GOLD,
                                                LeverBlock.EMERALD,
                                                LeverBlock.CLAY
                                            )
                                            solutions[WoolColor.BLUE] = ImmutableSet.of(
                                                LeverBlock.QUARTZ,
                                                LeverBlock.DIAMOND,
                                                LeverBlock.EMERALD
                                            )
                                            solutions[WoolColor.GREEN] =
                                                ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.EMERALD)
                                            solutions[WoolColor.RED] =
                                                ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.COAL, LeverBlock.EMERALD)
                                        }
                                        2 -> {
                                            solutions[WoolColor.PURPLE] =
                                                ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.GOLD, LeverBlock.DIAMOND)
                                            solutions[WoolColor.ORANGE] = ImmutableSet.of(LeverBlock.EMERALD)
                                            solutions[WoolColor.BLUE] =
                                                ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.DIAMOND)
                                            solutions[WoolColor.GREEN] = ImmutableSet.of()
                                            solutions[WoolColor.RED] =
                                                ImmutableSet.of(LeverBlock.GOLD, LeverBlock.EMERALD)
                                        }
                                        3 -> {
                                            solutions[WoolColor.PURPLE] = ImmutableSet.of(
                                                LeverBlock.QUARTZ,
                                                LeverBlock.GOLD,
                                                LeverBlock.EMERALD,
                                                LeverBlock.CLAY
                                            )
                                            solutions[WoolColor.ORANGE] =
                                                ImmutableSet.of(LeverBlock.GOLD, LeverBlock.COAL)
                                            solutions[WoolColor.BLUE] = ImmutableSet.of(
                                                LeverBlock.QUARTZ,
                                                LeverBlock.GOLD,
                                                LeverBlock.COAL,
                                                LeverBlock.EMERALD,
                                                LeverBlock.CLAY
                                            )
                                            solutions[WoolColor.GREEN] =
                                                ImmutableSet.of(LeverBlock.GOLD, LeverBlock.EMERALD)
                                            solutions[WoolColor.RED] = ImmutableSet.of(
                                                LeverBlock.GOLD,
                                                LeverBlock.DIAMOND,
                                                LeverBlock.EMERALD,
                                                LeverBlock.CLAY
                                            )
                                        }
                                        else -> {
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    } else {
                        variant = -1
                        solutions.clear()
                    }
                }, "Skytils-Water-Board-Puzzle")
                workerThread!!.start()
            }
            ticks = 0
        }
        ticks++
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Skytils.config.waterBoardSolver) return
        if (chestPos == null || roomFacing == null || variant == -1) return
        val leverStates = HashMap<LeverBlock, Boolean>()
        for (lever in LeverBlock.values()) {
            leverStates[lever] = getLeverToggleState(lever.leverPos)
        }
        val renderTimes = HashMap<LeverBlock, Int>()
        var matching = 0
        for (color in WoolColor.values()) {
            val renderColor = Color(color.dyeColor.mapColor.colorValue).brighter()
            if (color.isExtended) {
                val solution = solutions[color] ?: continue
                for ((lever, switched) in leverStates) {
                    if (switched && !solution.contains(lever) || !switched && solution.contains(lever)) {
                        val pos = lever.leverPos
                        val displayed =
                            renderTimes.compute(lever) { k: LeverBlock?, v: Int? -> if (v == null) return@compute 0 else return@compute v.inc() }
                        RenderUtil.draw3DString(
                            Vec3(pos!!.up()).addVector(0.5, 0.5 + 0.5 * displayed!!, 0.5),
                            "§l" + color.name,
                            renderColor,
                            event.partialTicks
                        )
                    }
                }
                if (leverStates.entries.all { (key, value) ->
                        value && solution.contains(key) || !value && !solution.contains(
                            key
                        )
                    }) {
                    RenderUtil.draw3DString(
                        Vec3(chestPos!!.offset(roomFacing!!.opposite, 17).up(5)).addVector(
                            0.5,
                            0.5 + 0.5 * matching,
                            0.5
                        ), "§l" + color.name, renderColor, event.partialTicks
                    )
                    matching++
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        variant = -1
        solutions.clear()
        chestPos = null
        roomFacing = null
        prevInWaterRoom = false
        inWaterRoom = false
    }

    private fun getLeverToggleState(pos: BlockPos?): Boolean {
        val block = mc.theWorld.getBlockState(pos)
        return if (block.block !== Blocks.lever) false else block.getValue(BlockLever.POWERED)
    }

    enum class WoolColor(var dyeColor: EnumDyeColor) {
        PURPLE(EnumDyeColor.PURPLE), ORANGE(EnumDyeColor.ORANGE), BLUE(EnumDyeColor.BLUE), GREEN(EnumDyeColor.GREEN), RED(
            EnumDyeColor.RED
        );

        val isExtended: Boolean
            get() = if (chestPos == null || roomFacing == null) false else mc.theWorld.getBlockState(
                chestPos!!.offset(
                    roomFacing!!.opposite, 3 + ordinal
                )
            ).block === Blocks.wool
    }

    enum class LeverBlock(var block: Block) {
        QUARTZ(Blocks.quartz_block), GOLD(Blocks.gold_block), COAL(Blocks.coal_block), DIAMOND(Blocks.diamond_block), EMERALD(
            Blocks.emerald_block
        ),
        CLAY(Blocks.hardened_clay);

        val leverPos: BlockPos?
            get() {
                if (chestPos == null || roomFacing == null) return null
                val shiftBy = ordinal % 3 * 5
                val leverSide = if (ordinal < 3) roomFacing!!.rotateY() else roomFacing!!.rotateYCCW()
                return chestPos!!.up(5).offset(leverSide.opposite, 6).offset(
                    roomFacing!!.opposite, 2 + shiftBy
                ).offset(leverSide)
            }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private val solutions = HashMap<WoolColor, ImmutableSet<LeverBlock>>()
        private var chestPos: BlockPos? = null
        private var roomFacing: EnumFacing? = null
        private var prevInWaterRoom = false
        private var inWaterRoom = false
        private var variant = -1
        private var workerThread: Thread? = null
        private var ticks = 0
    }
}