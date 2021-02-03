package skytils.skytilsmod.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SendPacketEvent extends Event {

    public Packet packet;

    public SendPacketEvent(Packet packet) {
        this.packet = packet;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

}
