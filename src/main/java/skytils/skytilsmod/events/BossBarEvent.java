package skytils.skytilsmod.events;

import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class BossBarEvent extends Event {
    @Cancelable
    public static class Set extends BossBarEvent {
        public IBossDisplayData displayData;
        public boolean hasColorModifier;
        public Set(IBossDisplayData displayData, boolean hasColorModifier) {
            this.displayData = displayData;
            this.hasColorModifier = hasColorModifier;
        }
    }
}
