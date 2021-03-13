package skytils.skytilsmod.events;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GuiContainerEvent extends Event {

    public Container container;
    public GuiContainerEvent(Container container) {
        this.container = container;
    }

    public static class CloseWindowEvent extends GuiContainerEvent {

        public CloseWindowEvent(Container container) {
            super(container);
        }
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

    @Cancelable
    public static class SlotClickEvent extends GuiContainerEvent {

        public Slot slot;
        public int slotId, clickedButton, clickType;
        public SlotClickEvent(Container container, Slot slot, int slotId, int clickedButton, int clickType) {
            super(container);
            this.slot = slot;
            this.slotId = slotId;
            this.clickedButton = clickedButton;
            this.clickType = clickType;
        }
    }
}
