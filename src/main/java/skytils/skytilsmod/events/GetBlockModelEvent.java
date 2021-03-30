package skytils.skytilsmod.events;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GetBlockModelEvent extends Event {

    public IBlockState state;

    public GetBlockModelEvent(IBlockState state) {
        this.state = state;
    }

}
