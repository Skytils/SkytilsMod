package skytils.skytilsmod.features.impl.mining;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemCloth;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GetBlockModelEvent;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.ScoreboardUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.Objects;

public class DarkModeMist {

    public static boolean inMist = false;

    private static int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !Utils.inSkyblock) return;
        if (ticks % 20 == 0) {
            checkMist();
            ticks = 0;
        }
        ticks++;
    }

    @SubscribeEvent
    public void onGetBlockModel(GetBlockModelEvent event) {
        if (!Utils.inSkyblock || !Skytils.config.darkModeMist) return;

        IBlockState state = event.state;

        if (inMist) {
            if (state.getBlock() == Blocks.stained_glass && state.getValue(BlockStainedGlass.COLOR) == EnumDyeColor.WHITE) {
                event.state = Blocks.stained_glass.getStateFromMeta(EnumDyeColor.GRAY.getMetadata());
            }
            if (state.getBlock() == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.WHITE) {
                event.state = Blocks.wool.getStateFromMeta(EnumDyeColor.GRAY.getMetadata());
            }
        }
    }

    private static void checkMist() {
        if (!Utils.inSkyblock || !Objects.equals(SBInfo.getInstance().getLocation(), "mining_3")) {
            inMist = false;
            return;
        }
        for (String l : ScoreboardUtil.getSidebarLines()) {
            if (ScoreboardUtil.cleanSB(l).contains("The Mist")) {
                inMist = true;
                return;
            }
        }
        inMist = false;
    }

}
