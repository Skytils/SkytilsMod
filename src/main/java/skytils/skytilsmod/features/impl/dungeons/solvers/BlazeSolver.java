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
import java.util.List;

/**
 * Original code was taken from Danker's Skyblock Mod under GNU 3.0 license and modified by the Skytils team
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
public class BlazeSolver {
    private int ticks = 0;
    
    public static EntityArmorStand highestBlazeLabel = null;
    public static EntityArmorStand lowestBlazeLabel = null;
    public static EntityBlaze highestBlaze = null;
    public static EntityBlaze lowestBlaze = null;
    public static int blazeMode = 0;
    public static BlockPos blazeChest = null;

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        WorldClient world = mc.theWorld;

        if (ticks % 20 == 0) {
            ticks = 0;
            if (Utils.inDungeons && blazeMode == 0 && (lowestBlazeLabel != null || highestBlazeLabel != null) && world != null && player != null) {
                new Thread(() -> {
                    List<EntityBlaze> blazes = mc.theWorld.getEntities(EntityBlaze.class, (blaze) -> player.getDistanceToEntity(blaze) < 100);
                    if (blazes.size() > 10) {
                        System.out.println("More than 10 blazes, was there an update?");
                    } else if (blazes.size() > 0) {
                        int diffY = 5 * (10 - blazes.size());
                        EntityBlaze blaze = blazes.get(0);
                        for (int x = (int) (blaze.posX - 13); x <= blaze.posX + 13; x++) {
                            for (int z = (int) (blaze.posZ - 13); z <= blaze.posZ + 13; z++) {
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
                }).start();
            }
        }

        if (ticks % 4 == 0) {
            if (Skytils.config.blazeSolver && Utils.inDungeons && world != null) {
                List<Entity> entities = world.getLoadedEntityList();
                int highestHealth = 0;
                highestBlazeLabel = null;
                int lowestHealth = 99999999;
                lowestBlazeLabel = null;

                for (Entity entity : entities) {
                    if (entity instanceof EntityArmorStand && entity.getName().contains("Blaze") && entity.getName().contains("/")) {
                        String blazeName = StringUtils.stripControlCodes(entity.getName());
                        try {
                            int health = Integer.parseInt(blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length() - 1));
                            if (health > highestHealth) {
                                highestHealth = health;
                                highestBlazeLabel = (EntityArmorStand) entity;
                            }
                            if (health < lowestHealth) {
                                lowestHealth = health;
                                lowestBlazeLabel = (EntityArmorStand) entity;
                            }
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        ticks++;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (Skytils.config.blazeSolver && Utils.inDungeons) {
            if (lowestBlazeLabel != null && blazeMode <= 0) {
                AxisAlignedBB aabb = new AxisAlignedBB(lowestBlazeLabel.posX - 0.5, lowestBlazeLabel.posY - 2, lowestBlazeLabel.posZ - 0.5, lowestBlazeLabel.posX + 0.5, lowestBlazeLabel.posY, lowestBlazeLabel.posZ + 0.5);
                for (Entity entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityBlaze) {
                        if (entity.getEntityBoundingBox().intersectsWith(aabb)) {
                            lowestBlaze = (EntityBlaze) entity;
                            break;
                        }
                    }
                }
                if (lowestBlaze != null) {
                    BlockPos stringPos = new BlockPos(lowestBlaze.posX, lowestBlaze.posY + 3, lowestBlaze.posZ);
                    RenderUtil.draw3DString(stringPos, EnumChatFormatting.BOLD + "Smallest", new Color(255, 0, 0, 200), event.partialTicks);
                }
            }
            if (highestBlazeLabel != null && blazeMode >= 0) {
                AxisAlignedBB aabb = new AxisAlignedBB(highestBlazeLabel.posX - 0.5, highestBlazeLabel.posY - 2, highestBlazeLabel.posZ - 0.5, highestBlazeLabel.posX + 0.5, highestBlazeLabel.posY, highestBlazeLabel.posZ + 0.5);
                for (Entity entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityBlaze) {
                        if (entity.getEntityBoundingBox().intersectsWith(aabb)) {
                            highestBlaze = (EntityBlaze) entity;
                            break;
                        }
                    }
                }
                if (highestBlaze != null) {
                    BlockPos stringPos = new BlockPos(highestBlaze.posX, highestBlaze.posY + 3, highestBlaze.posZ);
                    RenderUtil.draw3DString(stringPos, EnumChatFormatting.BOLD + "Biggest", new Color(0, 255, 0, 200), event.partialTicks);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        highestBlazeLabel = null;
        lowestBlazeLabel = null;
        highestBlaze = null;
        lowestBlaze = null;
        blazeMode = 0;
        blazeChest = null;
    }

}
