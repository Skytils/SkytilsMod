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

import com.google.common.collect.Lists
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.*
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.PacketEvent.SendEvent
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import java.awt.Color
import kotlin.math.floor

class BoulderSolver {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        ticks++
        if (ticks % 20 == 0) {
            ticks = 0
            update()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Skytils.config.boulderSolver) return
        if (boulderChest == null) return
        val viewer = Minecraft.getMinecraft().renderViewEntity
        val viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks
        val viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks
        val viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks
        if (roomVariant >= 0) {
            val steps = variantSteps[roomVariant]
            for (step in steps) {
                if (grid[step.x][step.y] != BoulderState.EMPTY) {
                    val downRow = boulderFacing!!.opposite
                    val rightColumn = boulderFacing!!.rotateY()
                    val farLeftPos = boulderChest!!.offset(downRow, 5).offset(rightColumn.opposite, 9)
                    val boulderPos = farLeftPos.offset(rightColumn, 3 * step.x).offset(downRow, 3 * step.y)
                    val actualDirection: EnumFacing? = when (step.direction) {
                        Direction.FORWARD -> boulderFacing
                        Direction.BACKWARD -> boulderFacing!!.opposite
                        Direction.LEFT -> boulderFacing!!.rotateYCCW()
                        Direction.RIGHT -> boulderFacing!!.rotateY()
                    }
                    val buttonPos = boulderPos.offset(actualDirection!!.opposite, 2).down()
                    val x = buttonPos.x - viewerX
                    val y = buttonPos.y - viewerY
                    val z = buttonPos.z - viewerZ
                    GlStateManager.disableCull()
                    RenderUtil.drawFilledBoundingBox(
                        AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1),
                        Color(255, 0, 0, 255),
                        0.7f
                    )
                    GlStateManager.enableCull()
                    break
                }
            }
        }
    }

    @SubscribeEvent
    fun onSendPacket(event: SendEvent) {
        if (!Utils.inDungeons) return
        if (event.packet is C08PacketPlayerBlockPlacement) {
            val packet = event.packet as C08PacketPlayerBlockPlacement
            if (packet.position != null && packet.position == boulderChest) {
                roomVariant = -2
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        reset()
    }

    enum class Direction {
        FORWARD, BACKWARD, LEFT, RIGHT
    }

    enum class BoulderState {
        EMPTY, FILLED, PLACEHOLDER
    }

    class BoulderPush(var x: Int, var y: Int, var direction: Direction)
    companion object {
        private val mc = Minecraft.getMinecraft()
        var boulderChest: BlockPos? = null
        var boulderFacing: EnumFacing? = null
        var grid = Array(7) { arrayOfNulls<BoulderState>(6) }
        var roomVariant = -1
        var variantSteps = ArrayList<ArrayList<BoulderPush>>()
        var expectedBoulders = ArrayList<ArrayList<BoulderState>>()
        private var ticks = 0
        private var workerThread: Thread? = null
        fun update() {
            if (!Skytils.config.boulderSolver) return
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (Utils.inDungeons && world != null && player != null && roomVariant != -2 && (workerThread == null || !workerThread!!.isAlive || workerThread!!.isInterrupted)) {
                workerThread = Thread({
                    var foundBirch = false
                    var foundBarrier = false
                    for (potentialBarrier in Utils.getBlocksWithinRangeAtSameY(player.position, 13, 68)) {
                        if (foundBarrier && foundBirch) break
                        if (!foundBarrier) {
                            if (world.getBlockState(potentialBarrier).block === Blocks.barrier) {
                                foundBarrier = true
                            }
                        }
                        if (!foundBirch) {
                            val potentialBirch = potentialBarrier.down(2)
                            if (world.getBlockState(potentialBirch).block === Blocks.planks && Blocks.planks.getDamageValue(
                                    world,
                                    potentialBirch
                                ) == 2
                            ) {
                                foundBirch = true
                            }
                        }
                    }
                    if (!foundBirch || !foundBarrier) return@Thread
                    if (boulderChest == null || boulderFacing == null) {
                        for (potentialChestPos in Utils.getBlocksWithinRangeAtSameY(player.position, 25, 66)) {
                            if (boulderChest != null && boulderFacing != null) break
                            if (world.getBlockState(potentialChestPos).block === Blocks.chest) {
                                if (world.getBlockState(potentialChestPos.down()).block === Blocks.stonebrick && world.getBlockState(
                                        potentialChestPos.up(3)
                                    ).block === Blocks.barrier
                                ) {
                                    boulderChest = potentialChestPos
                                    println("Boulder chest is at $boulderChest")
                                    for (direction in EnumFacing.HORIZONTALS) {
                                        if (world.getBlockState(potentialChestPos.offset(direction)).block === Blocks.stained_hardened_clay) {
                                            boulderFacing = direction
                                            println("Boulder room is facing $direction")
                                            break
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    } else {
                        val downRow = boulderFacing!!.opposite
                        val rightColumn = boulderFacing!!.rotateY()
                        val farLeftPos = boulderChest!!.offset(downRow, 5).offset(rightColumn.opposite, 9)
                        var row = 0
                        while (row < 6) {
                            var column = 0
                            while (column < 7) {
                                val current = farLeftPos.offset(rightColumn, 3 * column).offset(downRow, 3 * row)
                                val state = world.getBlockState(current)
                                grid[column][row] =
                                    if (state.block === Blocks.air) BoulderState.EMPTY else BoulderState.FILLED
                                column++
                            }
                            row++
                        }
                        if (roomVariant == -1) {
                            roomVariant = -2
                            var i = 0
                            while (i < expectedBoulders.size) {
                                val expected = expectedBoulders[i]
                                var isRight = true
                                var j = 0
                                while (j < expected.size) {
                                    val column = j % 7
                                    val r = floor((j / 7f).toDouble()).toInt()
                                    val state = expected[j]
                                    if (grid[column][r] != state && state != BoulderState.PLACEHOLDER) {
                                        isRight = false
                                        break
                                    }
                                    j++
                                }
                                if (isRight) {
                                    roomVariant = i
                                    mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.GREEN.toString() + "Skytils detected boulder variant " + (roomVariant + 1) + "."))
                                    break
                                }
                                i++
                            }
                            if (roomVariant == -2) {
                                mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Skytils couldn't detect the boulder variant."))
                            }
                        }
                    }
                }, "Skytils-Boulder-Puzzle")
                workerThread!!.start()
            }
        }

        fun reset() {
            boulderChest = null
            boulderFacing = null
            grid = Array(7) { arrayOfNulls(6) }
            roomVariant = -1
            workerThread = null
        }
    }

    init {
        expectedBoulders.add(
            Lists.newArrayList(
                BoulderState.EMPTY,
                BoulderState.FILLED,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY
            )
        )
        variantSteps.add(
            Lists.newArrayList(
                BoulderPush(2, 4, Direction.RIGHT),
                BoulderPush(2, 3, Direction.FORWARD),
                BoulderPush(3, 3, Direction.RIGHT),
                BoulderPush(4, 3, Direction.RIGHT),
                BoulderPush(4, 1, Direction.FORWARD),
                BoulderPush(5, 1, Direction.RIGHT)
            )
        )
        expectedBoulders.add(
            Lists.newArrayList(
                BoulderState.FILLED,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.FILLED
            )
        )
        variantSteps.add(
            Lists.newArrayList(
                BoulderPush(3, 4, Direction.FORWARD),
                BoulderPush(2, 4, Direction.LEFT),
                BoulderPush(3, 3, Direction.RIGHT),
                BoulderPush(3, 2, Direction.FORWARD),
                BoulderPush(2, 2, Direction.LEFT),
                BoulderPush(4, 2, Direction.RIGHT),
                BoulderPush(2, 1, Direction.FORWARD),
                BoulderPush(4, 1, Direction.FORWARD),
                BoulderPush(3, 1, Direction.RIGHT)
            )
        )
        expectedBoulders.add(
            Lists.newArrayList(
                BoulderState.FILLED,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.FILLED,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.FILLED
            )
        )
        variantSteps.add(Lists.newArrayList(BoulderPush(1, 1, Direction.RIGHT)))
        expectedBoulders.add(
            Lists.newArrayList(
                BoulderState.FILLED,
                BoulderState.FILLED,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.FILLED,
                BoulderState.FILLED
            )
        )
        variantSteps.add(
            Lists.newArrayList(
                BoulderPush(4, 3, Direction.FORWARD),
                BoulderPush(3, 3, Direction.LEFT),
                BoulderPush(3, 1, Direction.FORWARD),
                BoulderPush(2, 1, Direction.LEFT)
            )
        )
        expectedBoulders.add(
            Lists.newArrayList(
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.FILLED
            )
        )
        variantSteps.add(
            Lists.newArrayList(
                BoulderPush(3, 4, Direction.FORWARD),
                BoulderPush(3, 3, Direction.FORWARD),
                BoulderPush(2, 1, Direction.FORWARD),
                BoulderPush(1, 1, Direction.LEFT)
            )
        )
        expectedBoulders.add(
            Lists.newArrayList(
                BoulderState.FILLED,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.FILLED,
                BoulderState.EMPTY,
                BoulderState.FILLED,
                BoulderState.EMPTY
            )
        )
        variantSteps.add(Lists.newArrayList(BoulderPush(1, 4, Direction.FORWARD), BoulderPush(1, 1, Direction.RIGHT)))
        expectedBoulders.add(
            Lists.newArrayList(
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.FILLED,
                BoulderState.FILLED,
                BoulderState.FILLED,
                BoulderState.FILLED,
                BoulderState.FILLED,
                BoulderState.FILLED,
                BoulderState.EMPTY,
                BoulderState.FILLED
            )
        )
        variantSteps.add(
            Lists.newArrayList(
                BoulderPush(6, 4, Direction.FORWARD),
                BoulderPush(6, 3, Direction.FORWARD),
                BoulderPush(4, 1, Direction.FORWARD),
                BoulderPush(5, 1, Direction.RIGHT)
            )
        )
        expectedBoulders.add(
            Lists.newArrayList(
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.EMPTY,
                BoulderState.FILLED
            )
        )
        variantSteps.add(Lists.newArrayList(BoulderPush(0, 1, Direction.FORWARD)))
    }
}