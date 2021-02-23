package skytils.skytilsmod.features.impl.dungeons.solvers;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IcePathSolver {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final List<Point> steps = new ArrayList<>();
    private static BlockPos silverfishChestPos;
    private static EnumFacing roomFacing;
    private static int[][] grid = null;
    private static EntitySilverfish silverfish = null;
    private static Point silverfishPos = null;
    private int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null)
            return;

        if (!Skytils.config.icePathSolver) return;

        if (ticks % 20 == 0) {
            new Thread(() -> {
                List<EntitySilverfish> silverfish = mc.theWorld.getEntities(EntitySilverfish.class, s -> mc.thePlayer.getDistanceToEntity(s) < 20);
                if (silverfish.size() > 0) {
                    IcePathSolver.silverfish = silverfish.get(0);
                    if (silverfishChestPos == null || roomFacing == null) {
                        findChest:
                        for (BlockPos pos : Utils.getBlocksWithinRangeAtSameY(mc.thePlayer.getPosition(), 25, 67)) {
                            IBlockState block = mc.theWorld.getBlockState(pos);
                            if (block.getBlock() == Blocks.chest && mc.theWorld.getBlockState(pos.down()).getBlock() == Blocks.packed_ice && mc.theWorld.getBlockState(pos.up(2)).getBlock() == Blocks.hopper) {
                                for (EnumFacing direction : EnumFacing.HORIZONTALS) {
                                    if (mc.theWorld.getBlockState(pos.offset(direction)).getBlock() == Blocks.stonebrick) {
                                        silverfishChestPos = pos;
                                        roomFacing = direction;
                                        System.out.println(String.format("Silverfish chest is at %s and is facing %s", silverfishChestPos, roomFacing));
                                        break findChest;
                                    }
                                }
                            }
                        }
                    } else if (grid == null) {
                        grid = getLayout();
                        silverfishPos = getGridPointFromPos(IcePathSolver.silverfish.getPosition());
                        steps.clear();
                        if (silverfishPos.x < 17 && silverfishPos.y < 17) {
                            steps.addAll(solve(grid, silverfishPos.x, silverfishPos.y, 9, 0));
                        }
                    }
                }
            }).start();
            ticks = 0;
        }

        if (IcePathSolver.silverfish != null && grid != null) {
            if (IcePathSolver.silverfish.isEntityAlive() && !getGridPointFromPos(IcePathSolver.silverfish.getPosition()).equals(silverfishPos)) {
                silverfishPos = getGridPointFromPos(IcePathSolver.silverfish.getPosition());

                if (silverfishPos.x < 17 && silverfishPos.y < 17) {
                    steps.clear();
                    steps.addAll(solve(grid, silverfishPos.x, silverfishPos.y, 9, 0));
                }
            }
        }

        ticks++;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!Skytils.config.icePathSolver) return;

        if (silverfishChestPos != null && roomFacing != null && grid != null) {
            for (int i = 0; i < steps.size() - 1; i++) {
                Point point = steps.get(i);
                Point point2 = steps.get(i + 1);
                Vec3 pos = getVec3RelativeToGrid(point.x, point.y);
                Vec3 pos2 = getVec3RelativeToGrid(point2.x, point2.y);
                GlStateManager.disableCull();
                RenderUtil.draw3DLine(pos.addVector(0.5, 0.5, 0.5), pos2.addVector(0.5, 0.5, 0.5), 5, new Color(255, 0, 0), event.partialTicks);
                GlStateManager.enableCull();
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        silverfishChestPos = null;
        roomFacing = null;
        grid = null;
        steps.clear();
        silverfish = null;
        silverfishPos = null;
    }

    private Vec3 getVec3RelativeToGrid(int column, int row) {
        if (silverfishChestPos == null || roomFacing == null) return null;

        return new Vec3(silverfishChestPos
                .offset(roomFacing.getOpposite(), 4)
                .offset(roomFacing.rotateYCCW(), 8)
                .offset(roomFacing.rotateY(), column)
                .offset(roomFacing.getOpposite(), row));
    }

    private Point getGridPointFromPos(BlockPos pos) {
        if (silverfishChestPos == null || roomFacing == null) return null;
        BlockPos topLeft = silverfishChestPos.offset(roomFacing.getOpposite(), 4).offset(roomFacing.rotateYCCW(), 8);
        int xChange = (pos.getX() - topLeft.getX()) * (roomFacing.getOpposite().getDirectionVec().getX() == -1 ? -1 : 1);
        int zChange = (pos.getZ() - topLeft.getZ()) * (roomFacing.getOpposite().getDirectionVec().getZ() == -1 ? -1 : 1);

        if (roomFacing.getAxis() == EnumFacing.Axis.Z) return new Point(xChange, zChange);
        if (roomFacing.getAxis() == EnumFacing.Axis.X) return new Point(zChange, xChange);
        return null;
    }

    private int[][] getLayout() {
        if (silverfishChestPos == null || roomFacing == null) return null;
        int[][] grid = new int[17][17];
        for (int row = 0; row < 17; row++) {
            for (int column = 0; column < 17; column++) {
                grid[row][column] = mc.theWorld.getBlockState(new BlockPos(getVec3RelativeToGrid(column, row))).getBlock() != Blocks.air ? 1 : 0;
            }
            if (row == 16) return grid;
        }
        return null;
    }

    /**
     * This code was modified into returning an ArrayList and was taken under CC BY-SA 4.0
     *
     * @link https://stackoverflow.com/a/55271133
     * @author ofekp
     */
    private ArrayList<Point> solve(int[][] iceCave, int startX, int startY, int endX, int endY) {
        Point startPoint = new Point(startX, startY);

        LinkedList<Point> queue = new LinkedList<>();
        Point[][] iceCaveColors = new Point[iceCave.length][iceCave[0].length];

        queue.addLast(new Point(startX, startY));
        iceCaveColors[startY][startX] = startPoint;

        while (queue.size() != 0) {
            Point currPos = queue.pollFirst();
            // traverse adjacent nodes while sliding on the ice
            for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                Point nextPos = move(iceCave, iceCaveColors, currPos, dir);
                if (nextPos != null) {
                    queue.addLast(nextPos);
                    iceCaveColors[(int) nextPos.getY()][(int) nextPos.getX()] = new Point((int) currPos.getX(), (int) currPos.getY());
                    if (nextPos.getY() == endY && nextPos.getX() == endX) {
                        ArrayList<Point> steps = new ArrayList<>();
                        // we found the end point
                        Point tmp = currPos;  // if we start from nextPos we will count one too many edges
                        int count = 0;
                        steps.add(nextPos);
                        steps.add(currPos);
                        while (tmp != startPoint) {
                            count++;
                            tmp = iceCaveColors[(int) tmp.getY()][(int) tmp.getX()];
                            steps.add(tmp);
                        }
                        //System.out.println("Silverfish solved in " + count + " moves.");
                        return steps;
                    }
                }
            }
        }
        return Lists.newArrayList();
    }

    /**
     * This code was modified to fit Minecraft and was taken under CC BY-SA 4.0
     *
     * @link https://stackoverflow.com/a/55271133
     * @author ofekp
     */
    private Point move(int[][] iceCave, Point[][] iceCaveColors, Point currPos, EnumFacing dir) {
        int x = (int) currPos.getX();
        int y = (int) currPos.getY();

        int diffX = dir.getDirectionVec().getX();
        int diffY = dir.getDirectionVec().getZ();

        int i = 1;
        while (x + i * diffX >= 0
                && x + i * diffX < iceCave[0].length
                && y + i * diffY >= 0
                && y + i * diffY < iceCave.length
                && iceCave[y + i * diffY][x + i * diffX] != 1) {
            i++;
        }

        i--;  // reverse the last step

        if (iceCaveColors[y + i * diffY][x + i * diffX] != null) {
            // we've already seen this point
            return null;
        }

        return new Point(x + i * diffX, y + i * diffY);
    }

}
