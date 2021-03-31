package skytils.skytilsmod.features.impl.mining;

import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.RenderBlockInWorldEvent;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;

import java.util.Objects;

public class DarkModeMist {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGetBlockModel(RenderBlockInWorldEvent event) {
        if (!Utils.inSkyblock || !Skytils.config.darkModeMist) return;

        IBlockState state = event.state;

        if (Objects.equals(SBInfo.getInstance().getLocation(), "mining_3") && event.state != null && event.pos != null) {
            if (event.pos.getY() <= 76) {
                if (state.getBlock() == Blocks.stained_glass && state.getValue(BlockStainedGlass.COLOR) == EnumDyeColor.WHITE) {
                    event.state = state.withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY);
                }
                if (state.getBlock() == Blocks.carpet && state.getValue(BlockCarpet.COLOR) == EnumDyeColor.WHITE) {
                    event.state = state.withProperty(BlockCarpet.COLOR, EnumDyeColor.GRAY);
                }
            }
        }
    }
}
