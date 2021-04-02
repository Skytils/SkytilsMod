package skytils.skytilsmod.core.structure;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import skytils.skytilsmod.utils.RenderUtil;

import java.awt.*;

public class LocationButton extends GuiButton {
    private float x;
    private float y;
    private float x2;
    private float y2;
    private double scale;
    public GuiElement element;

    public LocationButton(GuiElement element) {
        super(-1, 0, 0, null);
        this.element = element;
        this.x = this.element.getActualX() - 5;
        this.y = this.element.getActualY() - 5;
        this.x2 = this.x + this.element.getWidth() + 5;
        this.y2 = this.y + this.element.getHeight() + 5;
    }

    public LocationButton(int buttonId, GuiElement element) {
        super(-1, 0, 0, null);
        this.element = element;
    }

    private void refreshLocations() {
        this.x = this.element.getActualX() - 2;
        this.y = this.element.getActualY() - 2;
        this.x2 = this.x + this.element.getWidth() + 4;
        this.y2 = this.y + this.element.getHeight() + 4;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        refreshLocations();
        hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x2 && mouseY < this.y2;
        Color c = new Color(255,255,255, hovered ? 100 : 40);
        RenderUtil.drawRect(x, y, x2, y2, c.getRGB());
        this.element.demoRender();
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
