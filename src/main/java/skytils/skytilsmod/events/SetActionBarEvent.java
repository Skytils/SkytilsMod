package skytils.skytilsmod.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SetActionBarEvent extends Event {

    public String message;
    public boolean isPlaying;

    public SetActionBarEvent(String message, boolean isPlaying) {
        this.message = message;
        this.isPlaying = isPlaying;
    }

}
