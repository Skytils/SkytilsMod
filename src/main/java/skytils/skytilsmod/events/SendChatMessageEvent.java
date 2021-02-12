package skytils.skytilsmod.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SendChatMessageEvent extends Event {
    public String message;
    public boolean addToChat;

    public SendChatMessageEvent(String message, boolean addToChat) {
        this.message = message;
        this.addToChat = addToChat;
    }
}
