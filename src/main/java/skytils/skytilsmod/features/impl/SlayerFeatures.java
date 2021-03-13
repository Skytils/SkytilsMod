package skytils.skytilsmod.features.impl;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.ScoreboardUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.List;

public class SlayerFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null) return;
        if (!Utils.inSkyblock) return;

        if (ticks % 4 == 0) {
            if (Skytils.config.rev5TNTPing) {
                if (ScoreboardUtil.getSidebarLines().stream().anyMatch(l->ScoreboardUtil.cleanSB(l).contains("Slay the boss!"))) {
                    BlockPos under = null;
                    if (mc.thePlayer.onGround) {
                        under = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
                    } else {
                        for (double i = mc.thePlayer.posY - 0.5; i > 0; i--) {
                            BlockPos test = new BlockPos(mc.thePlayer.posX, i, mc.thePlayer.posZ);
                            if (mc.theWorld.getBlockState(test).getBlock() != Blocks.air) {
                                under = test;
                                break;
                            }
                        }
                    }
                    if (under != null) {
                        IBlockState blockUnder = mc.theWorld.getBlockState(under);
                        boolean isDanger = false;
                        if (blockUnder.getBlock() == Blocks.stone_slab && blockUnder.getValue(BlockHalfStoneSlab.VARIANT) == BlockStoneSlab.EnumType.QUARTZ) {
                            isDanger = true;
                        } else if (blockUnder.getBlock() == Blocks.quartz_stairs || blockUnder.getBlock() == Blocks.acacia_stairs) {
                            isDanger = true;
                        } else if (blockUnder.getBlock() == Blocks.wooden_slab && blockUnder.getValue(BlockHalfWoodSlab.VARIANT) == BlockPlanks.EnumType.ACACIA) {
                            isDanger = true;
                        } else if (blockUnder.getBlock() == Blocks.stained_hardened_clay) {
                            int color = Blocks.stained_hardened_clay.getMetaFromState(blockUnder);
                            if (color == 0 || color == 8 || color == 14)
                                isDanger = true;
                        } else if (blockUnder.getBlock() == Blocks.bedrock) {
                            isDanger = true;
                        }
                        if (isDanger) {
                            mc.thePlayer.playSound("random.orb", 1, 1);
                        }
                    }
                }
            }
            ticks = 0;
        }
        ticks++;
    }

}
