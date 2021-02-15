package skytils.skytilsmod.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ReceivePacketEvent extends Event {

    public Packet<?> packet;

    public ReceivePacketEvent(Packet<?> packet) {
        this.packet = packet;
    }
}
