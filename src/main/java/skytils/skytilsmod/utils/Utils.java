package skytils.skytilsmod.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;

import java.util.List;

public class Utils {

    public static boolean inSkyblock = false;
    public static boolean inDungeons = false;

    public static boolean isOnHypixel() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null && !mc.isSingleplayer()) {
            return mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel") || (mc.thePlayer != null && mc.thePlayer.getClientBrand().toLowerCase().contains("hypixel"));
        }
        return false;
    }

    /**
     * Taken from Danker's Skyblock Mod under GNU 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
    */
    public static void checkForSkyblock() {
        Minecraft mc = Minecraft.getMinecraft();
        if (isOnHypixel()) {
            ScoreObjective scoreboardObj = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            if (scoreboardObj != null) {
                String scObjName = ScoreboardUtil.cleanSB(scoreboardObj.getDisplayName());
                if (scObjName.contains("SKYBLOCK")) {
                    inSkyblock = true;
                    return;
                }
            }
        }
        inSkyblock = false;
    }

    /**
     * Taken from Danker's Skyblock Mod under GNU 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    public static void checkForDungeons() {
        if (inSkyblock) {
            List<String> scoreboard = ScoreboardUtil.getSidebarLines();
            for (String s : scoreboard) {
                String sCleaned = ScoreboardUtil.cleanSB(s);
                if (sCleaned.contains("The Catacombs")) {
                    inDungeons = true;
                    return;
                }
            }
        }
        inDungeons = false;
    }

}