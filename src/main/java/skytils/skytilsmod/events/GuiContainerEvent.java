package skytils.skytilsmod.events;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GuiContainerEvent extends Event {

    public Container container;
    public GuiContainerEvent(Container container) {
        this.container = container;
    }

    public static class DrawSlotEvent extends GuiContainerEvent {

        public Slot slot;

        public DrawSlotEvent(Container container, Slot slot) {
            super(container);
            this.slot = slot;
        }

        @Cancelable
        public static class Pre extends DrawSlotEvent {
            public Pre(Container container, Slot slot) {
                super(container, slot);
            }
        }

    }
}
