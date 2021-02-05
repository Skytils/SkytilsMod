package skytils.skytilsmod.events;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.Event;

public class DamageBlockEvent extends Event {

    public BlockPos pos;
    public EnumFacing facing;

    public DamageBlockEvent(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        this.facing = facing;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
