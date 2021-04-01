package skytils.skytilsmod.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is posted when the chunk renderer tries to get the block model for a certain block.
 * Cancelling this event has no effect and is used to skip the rest of the event bus
 */
@Cancelable
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
