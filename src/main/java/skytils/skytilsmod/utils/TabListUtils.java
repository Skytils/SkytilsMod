package skytils.skytilsmod.utils;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.WorldSettings;

import java.util.Collections;
import java.util.List;

public class TabListUtils {

    public static final Ordering<NetworkPlayerInfo> playerInfoOrdering = new Ordering<NetworkPlayerInfo>() {
        @Override
        public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_) {
            ScorePlayerTeam scoreplayerteam = p_compare_1_.getPlayerTeam();
            ScorePlayerTeam scoreplayerteam1 = p_compare_2_.getPlayerTeam();
            return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != WorldSettings.GameType.SPECTATOR, p_compare_2_.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
        }
    };

    public static List<NetworkPlayerInfo> getTabEntries() {
        if (Minecraft.getMinecraft().thePlayer == null) return Collections.emptyList();
        return playerInfoOrdering.sortedCopy(Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap());
    }

}
