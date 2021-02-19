package skytils.skytilsmod.features.impl.dungeons.solvers;

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
import java.util.Arrays;
import java.util.List;

public class IcePathSolver {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static BlockPos silverfishChestPos;
    private static EnumFacing roomFacing;

    private int ticks = 0;

    private static final List<Point> steps = Arrays.asList(
            new Point(15, 15),
            new Point(11, 15),
            new Point(11, 16),
            new Point(3, 16),
            new Point(3, 0),
            new Point(4,0),
            new Point(4, 1),
            new Point(2, 1),
            new Point(2, 10),
            new Point(9, 10),
            new Point(9, 0));

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null) return;

        if (!Skytils.config.icePathSolver) return;

        if (ticks % 20 == 0) {
            new Thread(() -> {
                if (mc.theWorld.getEntities(EntitySilverfish.class, silverfish -> mc.thePlayer.getDistanceToEntity(silverfish) < 20).size() > 0) {
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
                    }
                }
            }).start();
            ticks = 0;
        }

        ticks++;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!Skytils.config.icePathSolver) return;

        if (silverfishChestPos != null && roomFacing != null) {
            for (int i = 0; i < steps.size() - 1; i++) {
                Point point = steps.get(i);
                Point point2 = steps.get(i + 1);
                Vec3 pos = getVec3RelativeToGrid(point.x, point.y);
                Vec3 pos2 = getVec3RelativeToGrid(point2.x, point2.y);
                GlStateManager.disableCull();
                RenderUtil.draw3DLine(pos.addVector(0.5, -0.5, 0.5), pos2.addVector(0.5, -0.5, 0.5), 5, new Color(255, 0, 0), event.partialTicks);
                GlStateManager.enableCull();
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        silverfishChestPos = null;
        roomFacing = null;
    }

    private Vec3 getVec3RelativeToGrid(int row, int column) {
        if (silverfishChestPos == null || roomFacing == null) return null;

        return new Vec3(silverfishChestPos
                .offset(roomFacing.getOpposite(), 4)
                .offset(roomFacing.rotateYCCW(), 8)
                .offset(roomFacing.rotateY(), row)
                .offset(roomFacing.getOpposite(), column)
                .up());
    }

}
