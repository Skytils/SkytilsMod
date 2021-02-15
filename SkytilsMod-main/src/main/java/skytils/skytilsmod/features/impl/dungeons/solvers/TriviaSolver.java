package skytils.skytilsmod.features.impl.dungeons.solvers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.utils.Utils;

import java.util.HashMap;

/**
 * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license and modified by the Skytils team
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
public class TriviaSolver {
    public static HashMap<String, String[]> triviaSolutions = new HashMap<>();
    public static String[] triviaAnswers = null;
    public static String triviaAnswer = null;

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (Skytils.config.triviaSolver && Utils.inDungeons) {
            if (unformatted.contains("Oruo the Omniscient") && unformatted.contains("correctly")) triviaAnswer = null;
            if (unformatted.contains("I am Oruo the Omniscient") && triviaSolutions.size() == 0) {
                mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7cSkytils failed to load solutions for Trivia."));
                DataFetcher.reloadData();
            }
            if (unformatted.contains("What SkyBlock year is it?")) {
                double currentTime = System.currentTimeMillis() / 1000d;

                double diff = Math.floor(currentTime - 1560276000);

                int year = (int) (diff / 446400 + 1);
                triviaAnswers = new String[]{"Year " + year};
            } else {
                for (String question : triviaSolutions.keySet()) {
                    if (unformatted.contains(question)) {
                        triviaAnswers = triviaSolutions.get(question);
                        break;
                    }
                }
            }
            // Set wrong answers to red and remove click events
            if (triviaAnswers != null && (unformatted.contains("ⓐ") || unformatted.contains("ⓑ") || unformatted.contains("ⓒ"))) {
                String answer = null;
                boolean isSolution = false;
                for (String solution : triviaAnswers) {
                    if (unformatted.contains(solution)) {
                        isSolution = true;
                        answer = solution;
                        break;
                    }
                }
                if (!isSolution) {
                    char letter = unformatted.charAt(5);
                    String option = unformatted.substring(6);
                    event.message = new ChatComponentText("     " + EnumChatFormatting.GOLD + letter + EnumChatFormatting.RED + option);
                    return;
                } else {
                    triviaAnswer = answer;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderArmorStandPre(RenderLivingEvent.Pre<EntityArmorStand> event) {
        if (Skytils.config.triviaSolver && triviaAnswer != null) {
            if (event.entity instanceof EntityArmorStand && event.entity.hasCustomName()) {
                String name = event.entity.getCustomNameTag();
                if (name.contains("ⓐ") || name.contains("ⓑ") || name.contains("ⓒ")) {
                    if (!name.contains(triviaAnswer)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        triviaAnswer = null;
    }

}
