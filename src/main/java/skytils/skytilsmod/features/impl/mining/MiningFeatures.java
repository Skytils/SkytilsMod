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
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiningFeatures {

    public static BlockPos puzzlerSolution = null;
    public static HashMap<String, String> fetchurItems = new HashMap<>();


    private final static Minecraft mc = Minecraft.getMinecraft();

    public MiningFeatures() {
        fetchurItems.put("theyre yellow and see through", "20 Yellow Stained Glass");
        fetchurItems.put("its circlular and sometimes moves", "1 Compass");
        fetchurItems.put("theyre expensive minerals", "20 Mithril");
        fetchurItems.put("its useful during celebrations", "1 Firework Rocket");
        fetchurItems.put("its hot and gives energy", "1 Cheap Coffee or 1 Decent Coffee");
        fetchurItems.put("its tall and can be opened", "1 Wooden Door");
        //hypixel disabled fetchur for a couple of days here, some may be missing
        fetchurItems.put("its explosive but more than usual", "1 Superboom TNT");
        fetchurItems.put("its wearable and grows", "1 Pumpkin");
        fetchurItems.put("its shiny and makes sparks", "1 Flint and Steel");
        fetchurItems.put("theyre red and white and you can mine it", "50 Nether Quartz Ore");
        fetchurItems.put("theyre round and green or purple", "16 Ender Pearls");
        fetchurItems.put("theyre red and soft", "50 Red Wool");
    }

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

        if (Skytils.config.fetchurSolver && unformatted.contains("[NPC]") && unformatted.contains("Fetchur")) {
            String solution = fetchurItems.keySet().stream().filter(unformatted::contains).findFirst().map(fetchurItems::get).orElse(null);
            new Thread(() -> {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (solution != null) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Fetchur needs: " + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.BOLD + solution + EnumChatFormatting.GREEN + "!"));
                } else {
                    if (unformatted.contains("its") || unformatted.contains("theyre")) {
                        System.out.println("Missing Fetchur item: " + unformatted);
                        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Skytils couldn't determine the Fetchur item."));
                    }
                }

            }).start();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;
        if (Skytils.config.puzzlerSolver && puzzlerSolution != null) {
            double x = puzzlerSolution.getX() - viewerX;
            double y = puzzlerSolution.getY() - viewerY;
            double z = puzzlerSolution.getZ() - viewerZ;
            GlStateManager.enableCull();
            RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1.01, z + 1), new Color(255, 0, 0, 200), 1f);
            GlStateManager.disableCull();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        puzzlerSolution = null;
    }

}
