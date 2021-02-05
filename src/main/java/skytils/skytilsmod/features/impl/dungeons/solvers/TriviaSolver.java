package skytils.skytilsmod.features.impl.dungeons.solvers;

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
import skytils.skytilsmod.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Original code was taken from Danker's Skyblock Mod under GNU 3.0 license and modified by the Skytils team
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
public class TriviaSolver {
    public static Map<String, String[]> triviaSolutions = new HashMap<>();
    public static String[] triviaAnswers = null;
    public static String triviaAnswer = null;

    public TriviaSolver() {
        triviaSolutions.put("What is the status of The Watcher?", new String[]{"Stalker"});
        triviaSolutions.put("What is the status of Bonzo?", new String[]{"New Necromancer"});
        triviaSolutions.put("What is the status of Scarf?", new String[]{"Apprentice Necromancer"});
        triviaSolutions.put("What is the status of The Professor?", new String[]{"Professor"});
        triviaSolutions.put("What is the status of Thorn?", new String[]{"Shaman Necromancer"});
        triviaSolutions.put("What is the status of Livid?", new String[]{"Master Necromancer"});
        triviaSolutions.put("What is the status of Sadan?", new String[]{"Necromancer Lord"});
        triviaSolutions.put("What is the status of Maxor?", new String[]{"Young Wither"});
        triviaSolutions.put("What is the status of Goldor?", new String[]{"Wither Soldier"});
        triviaSolutions.put("What is the status of Storm?", new String[]{"Elementalist"});
        triviaSolutions.put("What is the status of Necron?", new String[]{"Wither Lord"});
        triviaSolutions.put("How many total Fairy Souls are there?", new String[]{"220 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in Spider's Den?", new String[]{"17 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in The End?", new String[]{"12 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in The Barn?", new String[]{"7 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in Mushroom Desert?", new String[]{"8 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in Blazing Fortress?", new String[]{"19 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in The Park?", new String[]{"11 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in Jerry's Workshop?", new String[]{"5 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in Hub?", new String[]{"79 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in The Hub?", new String[]{"79 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in Deep Caverns?", new String[]{"21 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in Gold Mine?", new String[]{"12 Fairy Souls"});
        triviaSolutions.put("How many Fairy Souls are there in Dungeon Hub?", new String[]{"7 Fairy Souls"});
        triviaSolutions.put("Which brother is on the Spider's Den?", new String[]{"Rick"});
        triviaSolutions.put("What is the name of Rick's brother?", new String[]{"Pat"});
        triviaSolutions.put("What is the name of the Painter in the Hub?", new String[]{"Marco"});
        triviaSolutions.put("What is the name of the person that upgrades pets?", new String[]{"Kat"});
        triviaSolutions.put("What is the name of the lady of the Nether?", new String[]{"Elle"});
        triviaSolutions.put("Which villager in the Village gives you a Rogue Sword?", new String[]{"Jamie"});
        triviaSolutions.put("How many unique minions are there?", new String[]{"53 Minions"});
        triviaSolutions.put("Which of these enemies does not spawn in the Spider's Den?", new String[]{"Zombie Spider", "Cave Spider", "Wither Skeleton",
                "Dashing Spooder", "Broodfather", "Night Spider"});
        triviaSolutions.put("Which of these monsters only spawns at night?", new String[]{"Zombie Villager", "Ghast"});
        triviaSolutions.put("Which of these is not a dragon in The End?", new String[]{"Zoomer Dragon", "Weak Dragon", "Stonk Dragon", "Holy Dragon", "Boomer Dragon",
                "Booger Dragon", "Older Dragon", "Elder Dragon", "Stable Dragon", "Professor Dragon"});
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (Skytils.config.triviaSolver && Utils.inDungeons) {
            if (unformatted.contains("Oruo the Omniscient") && unformatted.contains("correctly")) triviaAnswer = null;

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
