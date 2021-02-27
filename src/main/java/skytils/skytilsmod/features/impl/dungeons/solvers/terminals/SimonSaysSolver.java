package skytils.skytilsmod.features.impl.dungeons.solvers.terminals;

import net.minecraft.block.BlockButtonStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.BlockChangeEvent;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.ArrayList;

public class SimonSaysSolver {

    private static final ArrayList<BlockPos> clickInOrder = new ArrayList<>();
    private static int clickNeeded = 0;

    @SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        BlockPos pos = event.pos;
        IBlockState old = event.old;
        IBlockState state = event.update;
        if (Utils.inDungeons) {
            if (Skytils.config.simonSaysSolver) {
                if (pos.getY() <= 123 && pos.getY() >= 120 && pos.getZ() >= 291 && pos.getZ() <= 294) {
                    if (pos.getX() == 310) {
                        System.out.println(String.format("Block at %s changed to %s from %s", pos, state.getBlock().getLocalizedName(), old.getBlock().getLocalizedName()));
                        if (state.getBlock() == Blocks.sea_lantern) {
                            if (!clickInOrder.contains(pos)) {
                                clickInOrder.add(pos);
                            }
                        }
                    } else if (pos.getX() == 309) {
                        if (state.getBlock() == Blocks.air) {
                            System.out.println("Buttons on simon says were removed!");
                            clickNeeded = 0;
                        } else if (state.getBlock() == Blocks.stone_button) {
                            if (old.getBlock() == Blocks.stone_button) {
                                if (state.getValue(BlockButtonStone.POWERED)) {
                                    System.out.println("Button on simon says was pressed");
                                    clickNeeded++;
                                }
                            }
                        }
                    }
                } else if (pos.equals(new BlockPos(309, 121, 290))) {
                    if (state.getBlock() == Blocks.stone_button) {
                        if (state.getValue(BlockButtonStone.POWERED)) {
                            System.out.println("Simon says was started");
                            clickInOrder.clear();
                            clickNeeded = 0;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        if (Skytils.config.simonSaysSolver && clickNeeded < clickInOrder.size()) {
            BlockPos pos = clickInOrder.get(clickNeeded).west();
            double x = pos.getX() - viewerX;
            double y = pos.getY() - viewerY;
            double z = pos.getZ() - viewerZ;
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), new Color(255, 0, 0), 0.5f);
            GlStateManager.enableBlend();
            GlStateManager.enableCull();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        clickInOrder.clear();
        clickNeeded = 0;
    }

}
