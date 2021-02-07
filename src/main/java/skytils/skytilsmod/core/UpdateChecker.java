package skytils.skytilsmod.core;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.APIUtil;

/**
 * Original version taken from Danker's Skyblock Mod under GPL 3.0 license. Modified by the Skytils team.
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
public class UpdateChecker {

    public static boolean updateChecked = false;

    private final static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer != null) {
            if (!updateChecked && Skytils.config.updateChannel > 0) {
                updateChecked = true;

                new Thread(() -> {
                    System.out.println("Checking for updates...");

                    JsonObject latestRelease = Skytils.config.updateChannel == 1 ? APIUtil.getJSONResponse("https://api.github.com/repos/Skytils/SkytilsMod/releases/latest") : APIUtil.getArrayResponse("https://api.github.com/repos/Skytils/SkytilsMod/releases").get(0).getAsJsonObject();
                    String latestTag = latestRelease.get("tag_name").getAsString();
                    DefaultArtifactVersion currentVersion = new DefaultArtifactVersion(Skytils.VERSION);
                    DefaultArtifactVersion latestVersion = new DefaultArtifactVersion(latestTag.substring(1));

                    if (currentVersion.compareTo(latestVersion) < 0) {
                        String releaseURL = latestRelease.get("html_url").getAsString();

                        ChatComponentText update = new ChatComponentText(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "  [UPDATE]  ");
                        update.setChatStyle(update.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, releaseURL)));

                        ChatComponentText discord = new ChatComponentText(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD + "  [DISCORD]  ");
                        discord.setChatStyle(discord.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/K2wJsBRUqR")).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to join the Discord!"))));

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Your version of Skytils is outdated. Please update to version " + latestTag + ".\n").appendSibling(update).appendSibling(discord));
                    }
                }).start();
            }
        }
    }

}
