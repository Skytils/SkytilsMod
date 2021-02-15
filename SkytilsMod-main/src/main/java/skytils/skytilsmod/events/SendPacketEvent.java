package skytils.skytilsmod.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SendPacketEvent extends Event {

    public Packet<?> packet;

    public SendPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }
}
