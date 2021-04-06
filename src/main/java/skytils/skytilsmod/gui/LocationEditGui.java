package skytils.skytilsmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.core.structure.LocationButton;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LocationEditGui extends GuiScreen {

    private float xOffset;
    private float yOffset;

    private GuiElement dragging;
    private final Map<GuiElement, LocationButton> locationButtons = new HashMap<>();

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        for (Map.Entry<Integer, GuiElement> e : Skytils.GUIMANAGER.getElements().entrySet()) {
            LocationButton lb = new LocationButton(e.getValue());
            this.buttonList.add(lb);
            this.locationButtons.put(e.getValue(), lb);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        onMouseMove(mouseX, mouseY);
        this.drawGradientRect(0, 0, this.width, this.height, new Color(0,0, 0,50).getRGB(), new Color(0,0, 0,200).getRGB());
        for (GuiButton button : this.buttonList) {
            if (button instanceof LocationButton) {
                if (((LocationButton) button).element.getToggled()) {
                    button.drawButton(this.mc, mouseX, mouseY);
                }
            } else {
                button.drawButton(this.mc, mouseX, mouseY);
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button instanceof LocationButton) {
            LocationButton lb = (LocationButton) button;
            dragging = lb.getElement();

            ScaledResolution sr = new ScaledResolution(mc);
            float minecraftScale = sr.getScaleFactor();
            float floatMouseX = Mouse.getX() / minecraftScale;
            float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

            xOffset = floatMouseX - dragging.getActualX();
            yOffset = floatMouseY - dragging.getActualY();
        }
    }

    /**
     * Set the coordinates when the mouse moves.
     */
    protected void onMouseMove(int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution(mc);
        float minecraftScale = sr.getScaleFactor();
        float floatMouseX = Mouse.getX() / minecraftScale;
        float floatMouseY = (Display.getHeight() - Mouse.getY()) / minecraftScale;
        if (dragging != null) {
            LocationButton lb = locationButtons.get(dragging);
            if (lb == null) {
                return;
            }
            float x = (floatMouseX - xOffset) / (float) sr.getScaledWidth();
            float y = (floatMouseY - yOffset) / (float) sr.getScaledHeight();
            dragging.setPos(x, y);
        }
    }

    /**
     * Reset the dragged feature when the mouse is released.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
    }

    /**
     * Saves the positions when the gui is closed
     */
    @Override
    public void onGuiClosed() {
        GuiManager.saveConfig();
    }
}
