package skytils.skytilsmod.core.structure;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;

public class LocationButton extends GuiButton {
    private float x;
    private float y;
    private double scale;
    public GuiElement element;

    public LocationButton(GuiElement element) {
        super(-1, 0, 0, null);
        this.element = element;
    }

    public LocationButton(int buttonId, GuiElement element) {
        super(-1, 0, 0, null);
        this.element = element;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.element.demoRender();
        hovered = mouseX >= this.element.getActualX() && mouseY >= this.getElement().getActualY() && mouseX < this.getElement().getActualX() + this.getElement().getWidth() && mouseY < this.getElement().getActualY() + this.getElement().getHeight();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && this.visible && hovered;
    }

    /**
     * get rid of clicking noise
     */
    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {}

    public GuiElement getElement() {
        return this.element;
    }
}
