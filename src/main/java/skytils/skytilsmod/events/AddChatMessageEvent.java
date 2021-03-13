package skytils.skytilsmod.events;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class AddChatMessageEvent extends Event {
    public IChatComponent message;
    public AddChatMessageEvent(IChatComponent message) {
        this.message = message;
    }
}
