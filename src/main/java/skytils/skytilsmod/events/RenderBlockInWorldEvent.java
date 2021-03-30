package skytils.skytilsmod.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderBlockInWorldEvent extends Event {

    public IBlockState state;
    public IBlockAccess world;
    public BlockPos pos;

    public RenderBlockInWorldEvent(IBlockState state, IBlockAccess world, BlockPos pos) {
        this.state = state;
        this.world = world;
        this.pos = pos;
    }

}
