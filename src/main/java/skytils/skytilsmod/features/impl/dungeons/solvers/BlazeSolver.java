package skytils.skytilsmod.features.impl.dungeons.solvers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license and modified by the Skytils team
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
public class BlazeSolver {
    private int ticks = 0;

    public static ArrayList<ShootableBlaze> orderedBlazes = new ArrayList<>();

    public static int blazeMode = 0;
    public static BlockPos blazeChest = null;

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        WorldClient world = mc.theWorld;

        if (!Utils.inDungeons || world == null || player == null) return;

        if (ticks % 20 == 0) {
            ticks = 0;
            if (blazeMode == 0 && orderedBlazes.size() > 0) {
                new Thread(() -> {
                    List<EntityBlaze> blazes = mc.theWorld.getEntities(EntityBlaze.class, (blaze) -> player.getDistanceToEntity(blaze) < 100);
                    if (blazes.size() > 10) {
                        System.out.println("More than 10 blazes, was there an update?");
                    } else if (blazes.size() > 0) {
                        int diffY = 5 * (10 - blazes.size());
                        EntityBlaze blaze = blazes.get(0);
                        for (BlockPos pos : Utils.getBlocksWithinRangeAtSameY(blaze.getPosition(), 13, 69)) {
                            int x = pos.getX();
                            int z = pos.getZ();
                            BlockPos blockPos1 = new BlockPos(x, 70 + diffY, z);
                            BlockPos blockPos2 = new BlockPos(x, 69 - diffY, z);
                            if (world.getBlockState(blockPos1).getBlock() == Blocks.chest) {
                                if (world.getBlockState(blockPos1.up()).getBlock() == Blocks.iron_bars) {
                                    blazeChest = blockPos1;
                                    if (blazes.size() < 10) {
                                        blazeMode = -1;
                                        System.out.println("Block scanning determined lowest -> highest");
                                    }
                                    break;
                                }
                            } else if (world.getBlockState(blockPos2).getBlock() == Blocks.chest) {
                                if (world.getBlockState(blockPos2.up()).getBlock() == Blocks.iron_bars) {
                                    blazeChest = blockPos2;
                                    if (blazes.size() < 10) {
                                        blazeMode = 1;
                                        System.out.println("Block scanning determined highest -> lowest");
                                    }
                                    break;
                                }
                            }
                        }

                        if (blazeChest != null && blazes.size() == 10) {
                            if (world.getBlockState(blazeChest.down()).getBlock() == Blocks.stone) {
                                System.out.println("Bottom block scanning determined lowest -> highest");
                                blazeMode = -1;
                            } else {
                                System.out.println("Bottom block scanning determined highest -> lowest");
                                blazeMode = 1;
                            }
                        }
                    }
                }, "Skytils-Blaze-Orientation").start();
            }
        }

        if (ticks % 4 == 0) {
            if (Skytils.config.blazeSolver) {
                orderedBlazes.clear();

                for (Entity entity : world.getLoadedEntityList()) {
                    if (entity instanceof EntityArmorStand && entity.getName().contains("Blaze") && entity.getName().contains("/")) {
                        String blazeName = StringUtils.stripControlCodes(entity.getName());
                        try {
                            int health = Integer.parseInt(blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length() - 1));
                            AxisAlignedBB aabb = new AxisAlignedBB(entity.posX - 0.5, entity.posY - 2, entity.posZ - 0.5, entity.posX + 0.5, entity.posY, entity.posZ + 0.5);
                            List<EntityBlaze> blazes = mc.theWorld.getEntitiesWithinAABB(EntityBlaze.class, aabb);
                            if (blazes.size() == 0) continue;
                            orderedBlazes.add(new ShootableBlaze(blazes.get(0), health));
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                orderedBlazes.sort(Comparator.comparingInt(blaze -> blaze.health));

            }
        }

        ticks++;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (Skytils.config.blazeSolver && Utils.inDungeons && orderedBlazes.size() > 0) {
            if (blazeMode <= 0) {
                ShootableBlaze shootableBlaze = orderedBlazes.get(0);
                EntityBlaze lowestBlaze = shootableBlaze.blaze;
                if (lowestBlaze != null) {
                    RenderUtil.draw3DString(new Vec3(lowestBlaze.posX, lowestBlaze.posY + 3, lowestBlaze.posZ), EnumChatFormatting.BOLD + "Smallest", new Color(255, 0, 0, 200), event.partialTicks);
                }
            }
            if (blazeMode >= 0) {
                ShootableBlaze shootableBlaze = orderedBlazes.get(orderedBlazes.size() - 1);
                EntityBlaze highestBlaze = shootableBlaze.blaze;
                if (highestBlaze != null) {
                    RenderUtil.draw3DString(new Vec3(highestBlaze.posX, highestBlaze.posY + 3, highestBlaze.posZ), EnumChatFormatting.BOLD + "Biggest", new Color(0, 255, 0, 200), event.partialTicks);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        orderedBlazes.clear();
        blazeMode = 0;
        blazeChest = null;
    }

    public static class ShootableBlaze {
        public EntityBlaze blaze;
        public int health;

        public ShootableBlaze(EntityBlaze blaze, int health) {
            this.blaze = blaze;
            this.health = health;
        }

    }

}
