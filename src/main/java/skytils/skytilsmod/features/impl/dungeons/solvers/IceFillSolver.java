package skytils.skytilsmod.features.impl.dungeons.solvers;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.*;
import java.util.List;

public class IceFillSolver {


    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int ticks = 0;

    private static BlockPos chestPos;
    private static EnumFacing roomFacing;

    private static IceFillPuzzle three = null;
    private static IceFillPuzzle five = null;
    private static IceFillPuzzle seven = null;

    private static Thread solverThread = null;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null) return;

        if (!Skytils.config.iceFillSolver) return;
        World world = mc.theWorld;

        if (ticks % 20 == 0) {

            if (chestPos == null || roomFacing == null) {
                new Thread(() -> {
                    findChest:
                    for (BlockPos pos : Utils.getBlocksWithinRangeAtSameY(mc.thePlayer.getPosition(), 25, 75)) {
                        IBlockState block = world.getBlockState(pos);
                        if (block.getBlock() == Blocks.chest && world.getBlockState(pos.down()).getBlock() == Blocks.stone) {
                            for (EnumFacing direction : EnumFacing.HORIZONTALS) {
                                if (world.getBlockState(pos.offset(direction)).getBlock() == Blocks.cobblestone && world.getBlockState(pos.offset(direction.getOpposite(), 2)).getBlock() == Blocks.iron_bars && world.getBlockState(pos.offset(direction.rotateY(), 2)).getBlock() == Blocks.torch && world.getBlockState(pos.offset(direction.rotateYCCW(), 2)).getBlock() == Blocks.torch && world.getBlockState(pos.offset(direction.getOpposite()).down(2)).getBlock() == Blocks.stone_brick_stairs) {
                                    chestPos = pos;
                                    roomFacing = direction;
                                    System.out.println(String.format("Ice fill chest is at %s and is facing %s", chestPos, roomFacing));
                                    break findChest;
                                }
                            }
                        }
                    }
                }, "Skytils-Ice-Fill-Detection").start();
            }

            if ((solverThread == null || !solverThread.isAlive()) && chestPos != null) {
                solverThread = new Thread(() -> {
                    if (three == null) {
                        three = new IceFillPuzzle(world, 70);
                    }
                    if (five == null) {
                        five = new IceFillPuzzle(world, 71);
                    }
                    if (seven == null) {
                        seven = new IceFillPuzzle(world, 72);
                    }
                    if (three.paths.size() == 0) {
                        three.genPaths(world);
                    }
                    if (five.paths.size() == 0) {
                        five.genPaths(world);
                    }
                    if (seven.paths.size() == 0) {
                        seven.genPaths(world);
                    }
                }, "Skytils-Ice-Fill-Solution");
                solverThread.start();
            }
            ticks = 0;
        }

        ticks++;
    }

    private boolean checkForStart(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.air &&
                world.getBlockState(pos.offset(roomFacing.rotateY())).getBlock() == Blocks.cobblestone_wall &&
                world.getBlockState(pos.offset(roomFacing.rotateYCCW())).getBlock() == Blocks.cobblestone_wall;
    }

    private List<Move> generatePairs(World world, List<BlockPos> positions) {
        List<Move> moves = new ArrayList<>();
        for (BlockPos pos : positions) {
            List<BlockPos> potential = getPossibleMoves(world, pos);
            for (BlockPos potent : potential) {
                Move potentialMove = new Move(pos, potent);
                if (!moves.contains(potentialMove)) moves.add(potentialMove);
            }
        }
        return moves;
    }

    private List<BlockPos> getPossibleMoves(World world, BlockPos pos) {
        List<BlockPos> moves = new ArrayList<>();
        if (world.getBlockState(pos.north().down()).getBlock() == Blocks.ice && world.getBlockState(pos.north()).getBlock() != Blocks.stone) {
            moves.add(pos.north());
        }
        if (world.getBlockState(pos.south().down()).getBlock() == Blocks.ice && world.getBlockState(pos.south()).getBlock() != Blocks.stone) {
            moves.add(pos.south());
        }
        if (world.getBlockState(pos.east().down()).getBlock() == Blocks.ice && world.getBlockState(pos.east()).getBlock() != Blocks.stone) {
            moves.add(pos.east());
        }
        if (world.getBlockState(pos.west().down()).getBlock() == Blocks.ice && world.getBlockState(pos.west()).getBlock() != Blocks.stone) {
            moves.add(pos.west());
        }
        return moves;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!Skytils.config.iceFillSolver) return;

        if (chestPos != null && roomFacing != null) {
            if (three != null && three.paths.size() > 0) {
                for (int i = 0; i < three.paths.get(0).size() - 1; i++) {
                    Vec3 pos = new Vec3(three.paths.get(0).get(i));
                    Vec3 pos2 = new Vec3(three.paths.get(0).get(i + 1));
                    GlStateManager.disableCull();
                    RenderUtil.draw3DLine(pos.addVector(0.5, 0.01, 0.5), pos2.addVector(0.5, 0.01, 0.5), 5, new Color(255, 0, 0), event.partialTicks);
                    GlStateManager.enableCull();
                }
            }
            if (five != null && five.paths.size() > 0) {
                for (int i = 0; i < five.paths.get(0).size() - 1; i++) {
                    Vec3 pos = new Vec3(five.paths.get(0).get(i));
                    Vec3 pos2 = new Vec3(five.paths.get(0).get(i + 1));
                    GlStateManager.disableCull();
                    RenderUtil.draw3DLine(pos.addVector(0.5, 0.01, 0.5), pos2.addVector(0.5, 0.01, 0.5), 5, new Color(255, 0, 0), event.partialTicks);
                    GlStateManager.enableCull();
                }
            }
            if (seven != null && seven.paths.size() > 0) {
                for (int i = 0; i < seven.paths.get(0).size() - 1; i++) {
                    Vec3 pos = new Vec3(seven.paths.get(0).get(i));
                    Vec3 pos2 = new Vec3(seven.paths.get(0).get(i + 1));
                    GlStateManager.disableCull();
                    RenderUtil.draw3DLine(pos.addVector(0.5, 0.01, 0.5), pos2.addVector(0.5, 0.01, 0.5), 5, new Color(255, 0, 0), event.partialTicks);
                    GlStateManager.enableCull();
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        chestPos = null;
        roomFacing = null;
        three = null;
        five = null;
        seven = null;
    }


    private Vec3 getVec3RelativeToGrid7(int row, int column) {
        if (chestPos == null || roomFacing == null) return null;

        return new Vec3(chestPos
                .offset(roomFacing.getOpposite(), 4)
                .down(3)
                .offset(roomFacing.rotateYCCW(), 3)
                .offset(roomFacing.rotateY(), row)
                .offset(roomFacing.getOpposite(), column)
        );
    }

    private Vec3 getVec3RelativeToGrid5(int row, int column) {
        if (chestPos == null || roomFacing == null) return null;

        return new Vec3(new BlockPos(getVec3RelativeToGrid7(1,6))
                .offset(roomFacing.getOpposite(), 3)
                .down()
                .offset(roomFacing.rotateY(), row)
                .offset(roomFacing.getOpposite(), column)
        );
    }

    private Vec3 getVec3RelativeToGrid3(int row, int column) {
        if (chestPos == null || roomFacing == null) return null;

        return new Vec3(new BlockPos(getVec3RelativeToGrid5(1,4))
                .offset(roomFacing.getOpposite(), 3)
                .down()
                .offset(roomFacing.rotateY(), row)
                .offset(roomFacing.getOpposite(), column)
        );
    }

    private class IceFillPuzzle {
        private final List<BlockPos> spaces = new ArrayList<>();
        private BlockPos start;
        public List<List<BlockPos>> paths = new ArrayList<>();

        IceFillPuzzle(World world, int y) {
            for (BlockPos pos : Utils.getBlocksWithinRangeAtSameY(chestPos, 25, y)) {
                IBlockState block = world.getBlockState(pos);
                if(world.getBlockState(pos.down()).getBlock() == Blocks.ice || world.getBlockState(pos.down()).getBlock() == Blocks.packed_ice) {
                    if(block.getBlock() == Blocks.air) {
                        spaces.add(pos);
                    }
                } else if ((world.getBlockState(pos.down()).getBlock() == Blocks.stone_brick_stairs || world.getBlockState(pos.down()).getBlock() == Blocks.stone) && start == null) {
                    if (checkForStart(world, pos)) {
                        start = pos.offset(roomFacing);
                    }
                }
            }
        }

        public void genPaths(World world) {
            // Generate paths
            List<Move> moves = generatePairs(world, spaces);
            Graph g = new Graph(moves, world);
            List<BlockPos> path = new ArrayList<>();
            path.add(start);

            Map<BlockPos, Boolean> visited = new HashMap<>();
            visited.put(start, true);

            try {
                getPaths(g, start, visited, path, spaces.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Take from Techie delight
         * Modified
         * https://www.techiedelight.com/print-all-hamiltonian-path-present-in-a-graph/
         */
        private void getPaths(Graph g, BlockPos v, Map<BlockPos, Boolean> visited, List<BlockPos> path, int N) {
            if (path.size() == N) {
                List<BlockPos> newPath = ImmutableList.copyOf(path);
                if (!paths.contains(path)) paths.add(newPath);
                return;
            } else {

                // Check if every move starting from position `v` leads
                // to a solution or not
                for (BlockPos w: g.adjList.get(v)) {
                    // process only unvisited vertices as the Hamiltonian
                    // path visit each vertex exactly once
                    if (visited.get(w) == null || !visited.get(w)) {
                        visited.put(w, true);
                        path.add(w);

                        // check if adding vertex `w` to the path leads
                        // to the solution or not
                        getPaths(g, w, visited, path, N);

                        // backtrack
                        visited.put(w, false);
                        path.remove(path.size() - 1);
                    }
                }
            }
        }
    }

    /**
     * Take from Techie delight
     * Modified
     * https://www.techiedelight.com/print-all-hamiltonian-path-present-in-a-graph/
     */
    private class Move {
        BlockPos source, dest;

        Move(BlockPos source, BlockPos dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public boolean equals(Object other) {
            return equals(this, other);
        }

        public boolean equals(Object original, Object other) {
            if (other == original) return true;
            if(!(other instanceof Move) || !(original instanceof Move)) return false;
            Move o = (Move) other;
            Move e = (Move) original;
            if (e.dest.equals(o.dest) && e.source.equals(o.source)) {
                return true;
            } else return e.dest.equals(o.source) && e.source.equals(o.dest);
        }
    }

    /**
     * Take from Techie delight
     * Modified
     * https://www.techiedelight.com/print-all-hamiltonian-path-present-in-a-graph/
     */
    class Graph {
        Map<BlockPos, List<BlockPos>> adjList;

        Graph(List<Move> moves, World world) {
            adjList = new HashMap<>();

            for (Move move : moves) {
                BlockPos src = move.source;
                BlockPos dest = move.dest;

                adjList.put(src, getPossibleMoves(world, src));
                adjList.put(dest, getPossibleMoves(world, dest));
            }
        }
    }
}
