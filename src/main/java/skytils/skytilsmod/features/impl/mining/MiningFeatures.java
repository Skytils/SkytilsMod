package skytils.skytilsmod.features.impl.mining;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.ScoreboardUtil;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiningFeatures {

    public static BlockPos puzzlerSolution = null;
    private final static Minecraft mc = Minecraft.getMinecraft();


    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());

        if (Skytils.config.puzzlerSolver && unformatted.contains("[NPC]") && unformatted.contains("Puzzler")) {
            if (unformatted.contains("Nice")) {
                puzzlerSolution = null;
                return;
            }
            if (unformatted.contains("Wrong") || unformatted.contains("Come") || (!unformatted.contains("▶") && !unformatted.contains("▲") && !unformatted.contains("◀") && !unformatted.contains("▼"))) return;
            if (ScoreboardUtil.getSidebarLines().stream().anyMatch(line -> ScoreboardUtil.cleanSB(line).contains("Dwarven Mines"))) {
                puzzlerSolution = new BlockPos(181, 195, 135);
                String msg = unformatted.substring(15).trim();
                Matcher matcher = Pattern.compile("([▶▲◀▼]+)").matcher(unformatted);
                if (matcher.find()) {
                    String sequence = matcher.group(1).trim();
                    if (sequence.length() != msg.length()) {
                        System.out.println(String.format("%s - %s | %s - %s", sequence, msg, sequence.length(), unformatted.length()));
                    }
                    for (char c : sequence.toCharArray()) {
                        switch (String.valueOf(c)) {
                            case "▲":
                                puzzlerSolution = puzzlerSolution.south();
                                break;
                            case "▶":
                                puzzlerSolution = puzzlerSolution.west();
                                break;
                            case "◀":
                                puzzlerSolution = puzzlerSolution.east();
                                break;
                            case "▼":
                                puzzlerSolution = puzzlerSolution.north();
                                break;
                            default:
                                System.out.println("Invalid Puzzler character: " + c);
                        }
                    }
                    System.out.println("Puzzler Solution: " + puzzlerSolution);
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Mine the block highlighted in " + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "RED" + EnumChatFormatting.GREEN + "!"));
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
        if (Skytils.config.puzzlerSolver && puzzlerSolution != null) {
            GlStateManager.disableCull();
            RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(puzzlerSolution.add(-viewerX, -viewerY, -viewerZ), puzzlerSolution.add(-viewerX, -viewerY, -viewerZ).add(1, 1, 1)).expand(0.01f, 0.01f, 0.01f), new Color(255, 0, 0, 200), 1f);
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        puzzlerSolution = null;
    }

}
