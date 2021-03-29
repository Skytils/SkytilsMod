package skytils.skytilsmod.features.impl;

import net.minecraft.block.BlockHalfStoneSlab;
import net.minecraft.block.BlockHalfWoodSlab;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.ScoreboardUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;

public class SlayerFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static int ticks = 0;

    private static final String[] ZOMBIE_BOSSES = {"§cRevenant Sycophant", "§cRevenant Champion", "§4Deformed Revenant", "§cAtoned Champion", "§4Atoned Revenant"};
    private static final String[] SPIDER_BOSSES = {"§cTarantula Vermin", "§cTarantula Beast", "§4Mutant Tarantula"};
    private static final String[] WOLF_BOSSES = {"§cPack Enforcer", "§cSven Follower", "§4Sven Alpha"};

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!Utils.inSkyblock) return;
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null) return;

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

    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (!Utils.inSkyblock) return;
        if (event.entity instanceof EntityArmorStand) {
            EntityArmorStand entity = (EntityArmorStand) event.entity;
            if (!entity.hasCustomName()) return;
            String name = entity.getDisplayName().getUnformattedText();
            if (Skytils.config.slayerBossHitbox && name.endsWith("§c❤") && !name.contains("§e0§f/§a") && !mc.getRenderManager().isDebugBoundingBox()) {
                double x = entity.posX;
                double y = entity.posY;
                double z = entity.posZ;

                for (String zombieBoss : ZOMBIE_BOSSES) {
                    if (name.contains(zombieBoss)) {
                        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x - 0.5, y - 2, z - 0.5, x + 0.5, y, z + 0.5), new Color(0, 255, 255, 255), 3, 1f);
                        return;
                    }
                }

                for (String spiderBoss : SPIDER_BOSSES) {
                    if (name.contains(spiderBoss)) {
                        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x - 0.625, y - 1, z - 0.625, x + 0.625, y - 0.25, z + 0.625), new Color(0, 255, 255, 255), 3, 1f);
                        return;
                    }
                }

                for (String wolfBoss : WOLF_BOSSES) {
                    if (name.contains(wolfBoss)) {
                        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x - 0.5, y - 1, z - 0.5, x + 0.5, y, z + 0.5), new Color(0, 255, 255, 255), 3, 1f);
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!Utils.inSkyblock) return;
        if (event.packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            if (Skytils.config.slayerMinibossSpawnAlert && packet.getSoundName().equals("random.explode") && packet.getVolume() == 0.6f && packet.getPitch() == 9/7f) {
                GuiManager.createTitle("§cMINIBOSS", 20);
            }
        }
    }

}
