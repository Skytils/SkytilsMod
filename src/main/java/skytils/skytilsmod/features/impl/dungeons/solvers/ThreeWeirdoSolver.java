package skytils.skytilsmod.features.impl.dungeons.solvers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;

public class ThreeWeirdoSolver {
    static String[] riddleSolutions = {"The reward is not in my chest!", "At least one of them is lying, and the reward is not in",
            "My chest doesn't have the reward. We are all telling the truth", "My chest has the reward and I'm telling the truth",
            "The reward isn't in any of our chests", "Both of them are telling the truth."};
    static String riddleNPC = null;
    public static BlockPos riddleChest = null;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());

        if(!Utils.inDungeons) return;

        if (message.contains("PUZZLE SOLVED!")) {
            if (message.contains("wasn't fooled by ")) {
                riddleNPC = null;
                riddleChest = null;
            }
        }

        // Dungeon chat spoken by an NPC, containing :
        if (Skytils.config.threeWeirdosSolver && message.contains("[NPC]")) {
            for (String solution : riddleSolutions) {
                if (message.contains(solution)) {
                    String npcName = message.substring(message.indexOf("]") + 2, message.indexOf(":"));
                    riddleNPC = npcName;
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN.toString() + EnumChatFormatting.BOLD + StringUtils.stripControlCodes(npcName) + EnumChatFormatting.DARK_GREEN + " has the blessing."));
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        riddleNPC = null;
        riddleChest = null;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (Skytils.config.threeWeirdosSolver && riddleNPC != null && Utils.inDungeons) {
            if (riddleChest == null) {
                EntityArmorStand riddleLabel = Minecraft.getMinecraft().theWorld.getEntities(EntityArmorStand.class, (entity -> {
                    if (entity == null) return false;
                    if (!entity.hasCustomName()) return false;
                    return entity.getCustomNameTag().contains(riddleNPC);
                })).stream().findFirst().orElse(null);
                if (riddleLabel != null) {
                    System.out.println("Chest Finder: Found Riddle NPC " + riddleLabel.getCustomNameTag() + " at " + riddleLabel.posX + ", " + riddleLabel.posY + ", " + riddleLabel.posY);
                    BlockPos potentialPos = new BlockPos(Math.floor(riddleLabel.posX), 69, Math.floor(riddleLabel.posZ));
                    if (Minecraft.getMinecraft().theWorld.getBlockState(potentialPos.north()).getBlock() == Blocks.chest) {
                        riddleChest = potentialPos.north();
                        System.out.print("Correct position is at: " + riddleChest.getX() + ", " + riddleChest.getY() + riddleChest.getZ());
                    }
                    else if (Minecraft.getMinecraft().theWorld.getBlockState(potentialPos.south()).getBlock() == Blocks.chest) {
                        riddleChest = potentialPos.south();
                        System.out.print("Correct position is at: " + riddleChest);
                    }
                    else if (Minecraft.getMinecraft().theWorld.getBlockState(potentialPos.east()).getBlock() == Blocks.chest) {
                        riddleChest = potentialPos.east();
                        System.out.print("Correct position is at: " + riddleChest);
                    }
                    else if (Minecraft.getMinecraft().theWorld.getBlockState(potentialPos.west()).getBlock() == Blocks.chest) {
                        riddleChest = potentialPos.west();
                        System.out.print("Correct position is at: " + riddleChest);
                    }
                }
            } else {
                RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(riddleChest, riddleChest.add(1, 1, 1)), new Color(255,0,0), event.partialTicks);
//                Utils.draw3DBox(new AxisAlignedBB(riddleChest, riddleChest.add(1, 1, 1)), new Color(255, 0, 0).getRGB(), event.partialTicks);
            }
        }
    }
}
