package gg.skytils.skytilsmod.features.impl.dungeons

import gg.essential.elementa.utils.withAlpha
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod._event.HypixelPacketReceiveEvent
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.util.SBInfo
import gg.skytils.skytilsmod.util.SuperSecretSettings
import gg.skytils.skytilsmod.util.drawDebugBlocks
import gg.skytils.skytilsmod.util.drawLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.block.Blocks
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import java.awt.Color

object IceFillSolver : EventSubscriber {
    private val LOGGER = LogManager.getLogger()

    private data class IceFillPuzzle(
        val three: IceFillSection,
        val five: IceFillSection,
        val seven: IceFillSection,
        val chestCenterPos: BlockPos,
        val roomFacing: Direction
    )

    private var iceFillPuzzle: IceFillPuzzle? = null
    private var lastScan = Long.MIN_VALUE

    override fun setup() {
        WorldRenderEvents.END_MAIN.register(::onRender)
        register(::onTick)
        register(::onHypixelPacket)
    }

    fun onRender(context: WorldRenderContext) {
        // TODO: Track uncompleted puzzles in a dungeon
        if (!SBInfo.skyblockState.getUntracked() || !SBInfo.dungeonsState.getUntracked() || !Config.iceFillSolver) return
        val (three, five, seven, chestCenterPos, roomFacing) = iceFillPuzzle ?: return

        context.drawDebugBlocks(
            chestCenterPos,
            "Chest Center Position (Facing ${roomFacing.opposite})",
            Color.CYAN.withAlpha(0.5f)
        )

        three.draw(context)
        five.draw(context)
        seven.draw(context)
    }

    // TODO: This is the wrong way to go about it but idc
    fun onHypixelPacket(event: HypixelPacketReceiveEvent) {
        if (!Config.iceFillSolver) return
        if (event.packet is ClientboundLocationPacket) {
            reset()
        }
    }

    // TODO: This is the wrong way to go about it but idc
    fun onTick(event: TickEvent) {
        if (!Config.iceFillSolver) return
        if (System.currentTimeMillis() < lastScan + 1000L) return
        if (!SBInfo.dungeonsState.getUntracked()) return
        if (iceFillPuzzle != null) return

        lastScan = System.currentTimeMillis()

        // TODO: Make this actually a sane job launch
        CoroutineScope(Dispatchers.IO).launch {
            scan()
        }
    }

    private fun scan() {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return
        val player = client.player ?: return

        val px = player.blockPos.x
        val pz = player.blockPos.z

        val chunkRadius = 2

        for (cx in (px shr 4) - chunkRadius..(px shr 4) + chunkRadius) {
            for (cz in (pz shr 4) - chunkRadius..(pz shr 4) + chunkRadius) {
                val chunk = world.getChunk(cx, cz) ?: continue

                for ((pos, blockEntity) in chunk.blockEntities) {
                    if (blockEntity !is ChestBlockEntity) continue
                    if (pos.y != 75) continue
                    if (world.getBlockState(pos.down()).block != Blocks.POLISHED_ANDESITE) continue

                    for (direction in Direction.Type.HORIZONTAL) {
                        if (world.getBlockState(pos.offset(direction)).block != Blocks.COBBLESTONE) continue
                        if (world.getBlockState(pos.offset(direction.opposite, 2)).block != Blocks.IRON_BARS) continue

                        val offsetDir = listOf(direction.rotateYCounterclockwise(), direction.rotateYClockwise()).find {
                            world.getBlockState(
                                pos.offset(
                                    it,
                                    1
                                )
                            ).block == Blocks.TORCH && world.getBlockState(
                                pos.offset(
                                    it.opposite,
                                    3
                                )
                            ).block == Blocks.TORCH
                        }?.opposite ?: continue

                        if (world.getBlockState(
                                pos.offset(direction.opposite).offset(offsetDir).down(2)
                            ).block != Blocks.STONE_BRICK_STAIRS
                        ) continue

                        val chestCenter = pos.offset(offsetDir)

                        val starts = Triple(
                            chestCenter.down(5).offset(direction.opposite, 22),
                            chestCenter.down(4).offset(direction.opposite, 17),
                            chestCenter.down(3).offset(direction.opposite, 10),
                        )
                        val ends = Triple(
                            starts.first.offset(direction, 3),
                            starts.second.offset(direction, 5),
                            starts.third.offset(direction, 7),
                        )

                        iceFillPuzzle = IceFillPuzzle(
                            IceFillSection(world, starts.first, ends.first, direction),
                            IceFillSection(world, starts.second, ends.second, direction),
                            IceFillSection(world, starts.third, ends.third, direction),
                            chestCenter,
                            direction
                        )

                        LOGGER.debug("Ice Fill Chest at $pos, is facing $direction and is offset $offsetDir")

                        return
                    }
                }
            }
        }
    }

    fun reset() {
        iceFillPuzzle = null
    }

    private class IceFillSection(
        val world: World, val start: BlockPos, val end: BlockPos, val facing: Direction
    ) {
        // TODO: Probably make this a job again
        val path: List<BlockPos>? = findPath()

        private fun findPath(): List<BlockPos>? {
            val spaces = getSpaces()
            val n = spaces.size
            val startIndex = spaces.indexOf(start)
            if (startIndex == -1) return null

            val visited = BooleanArray(n).also { it[startIndex] = true }
            val pathArr = IntArray(n) { -1 }.also { it[0] = startIndex }

            val moves = spaces.associate { pos ->
                val neighbors = Direction.Type.HORIZONTAL.mapNotNull { dir ->
                    val neighbour = spaces.indexOf(pos.offset(dir))
                    if (neighbour >= 0) Pair(neighbour, dir) else null
                }
                Pair(spaces.indexOf(pos), neighbors)
            }

            return if (SuperSecretSettings.getSetting("azoopuzzoo")) {
                val optimizedMoves = Array(n) { i ->
                    moves[i]!!.map { (neighbourIdx, dir) ->
                        neighbourIdx + (dir.ordinal.toLong() shl 32)
                    }.toLongArray()
                }

                getOptimalPath(
                    optimizedMoves, n, startIndex, visited, pathArr, 1, facing.ordinal, 0, Int.MAX_VALUE
                )?.first?.map { spaces[it] }
            } else {
                val simplifiedMoves = Array(n) { i ->
                    moves[i]!!.map { (neighbourIdx, _) -> neighbourIdx }
                }

                getFirstPath(simplifiedMoves, n, startIndex, visited, pathArr, 1)?.map { spaces[it] }
            }
        }

        private fun getFirstPath(
            moves: Array<List<Int>>,
            n: Int,
            visiting: Int,
            visited: BooleanArray,
            path: IntArray,
            depth: Int,
        ): List<Int>? {
            if (depth == n) return path.toList()

            for (index in moves[visiting]) {
                if (visited[index]) continue
                visited[index] = true
                path[depth] = index

                getFirstPath(moves, n, index, visited, path, depth + 1)?.let { return it }

                visited[index] = false
            }
            return null
        }

        private fun getOptimalPath(
            moves: Array<LongArray>,
            n: Int,
            visiting: Int,
            visited: BooleanArray,
            path: IntArray,
            depth: Int,
            lastDirection: Int,
            corners: Int,
            knownLeastCorners: Int,
        ): Pair<List<Int>, Int>? {
            if (corners >= knownLeastCorners) return null
            if (depth == n) return Pair(path.toList(), corners)

            var bestPath: List<Int>? = null
            var leastCorners = knownLeastCorners

            for (value in moves[visiting]) {
                val index = value.toInt()
                if (visited[index]) continue

                val direction = (value shr 32).toInt()
                visited[index] = true
                path[depth] = index

                val newCorners = if (lastDirection != direction) corners + 1 else corners

                getOptimalPath(
                    moves, n, index, visited, path, depth + 1, direction, newCorners, leastCorners
                )?.let { (p, c) ->
                    bestPath = p
                    leastCorners = c
                }

                visited[index] = false
            }

            return bestPath?.let { Pair(it, leastCorners) }
        }

        private fun getSpaces(): List<BlockPos> {
            val spaces = mutableListOf(start)
            val queue = mutableListOf(start)

            while (queue.isNotEmpty()) {
                val current = queue.removeLast()
                for (dir in Direction.Type.HORIZONTAL) {
                    val next = current.offset(dir)
                    if (next in spaces) continue
                    val blockBelow = world.getBlockState(next.down()).block
                    if (world.getBlockState(next).block == Blocks.AIR && (blockBelow == Blocks.ICE || blockBelow == Blocks.PACKED_ICE)) {
                        spaces.add(next)
                        queue.add(next)
                    }
                }
            }
            return spaces
        }

        fun draw(context: WorldRenderContext) {
            val p = path ?: return
            p.zipWithNext { a, b -> context.drawLine(a, b, Color.MAGENTA, 3f, yOffset = 0.0005f) }
            context.drawDebugBlocks(this.start, "Start", Color.GREEN.withAlpha(0.5f))
            context.drawDebugBlocks(this.end, "End", Color.RED.withAlpha(0.5f))
        }
    }
}
