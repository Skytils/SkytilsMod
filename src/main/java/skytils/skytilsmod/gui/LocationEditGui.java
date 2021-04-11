/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.core.structure.LocationButton;
import skytils.skytilsmod.core.structure.ResizeButton;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocationEditGui extends GuiScreen {

    private float xOffset;
    private float yOffset;

    private boolean resizing;
    private ResizeButton.Corner resizingCorner;

    private GuiElement dragging;
    private final Map<GuiElement, LocationButton> locationButtons = new HashMap<>();

    private float scaleCache = 0;

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
        recalculateResizeButtons();
        this.drawGradientRect(0, 0, this.width, this.height, new Color(0,0, 0,50).getRGB(), new Color(0,0, 0,200).getRGB());
        for (GuiButton button : this.buttonList) {
            if (button instanceof LocationButton) {
                if (((LocationButton) button).element.getToggled()) {
                    LocationButton locationButton = (LocationButton) button;
                    GlStateManager.pushMatrix();
                    float scale = locationButton.element.getScale();
                    GlStateManager.translate(locationButton.x, locationButton.y, 0);
                    GlStateManager.scale(scale, scale, 1.0);
                    button.drawButton(this.mc, mouseX, mouseY);
                    GlStateManager.popMatrix();
                }
            } else if (button instanceof ResizeButton) {
                ResizeButton resizeButton = (ResizeButton) button;
                GuiElement element = ((ResizeButton) button).element;
                GlStateManager.pushMatrix();
                float scale = element.getScale();
                GlStateManager.translate(resizeButton.x, resizeButton.y, 0);
                GlStateManager.scale(scale, scale, 1.0);
                button.drawButton(this.mc, mouseX, mouseY);
                GlStateManager.popMatrix();
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
        } else if (button instanceof ResizeButton) {
            ResizeButton resizeButton = (ResizeButton) button;
            dragging = resizeButton.element;
            resizing = true;
            scaleCache = resizeButton.element.getScale();

            ScaledResolution sr = new ScaledResolution(mc);
            float minecraftScale = sr.getScaleFactor();
            float floatMouseX = Mouse.getX() / minecraftScale;
            float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

            xOffset = floatMouseX - resizeButton.x;
            yOffset = floatMouseY - resizeButton.y;

            resizingCorner = resizeButton.getCorner();
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
        if (resizing) {
            LocationButton locationButton = locationButtons.get(dragging);
            if (locationButton == null) {
                return;
            }
            float scale = scaleCache;
            float scaledX1 = locationButton.x * scale;
            float scaledY1 = locationButton.y * scale;
            float scaledX2 = locationButton.x2 * scale;
            float scaledY2 = locationButton.y2 * scale;
            float scaledWidth = scaledX2-scaledX1;
            float scaledHeight = scaledY2-scaledY1;

            float width = locationButton.x2 - locationButton.x;
            float height = locationButton.y2 - locationButton.y;

            float middleX = scaledX1+scaledWidth/2F;
            float middleY = scaledY1+scaledHeight/2F;

            float xOffset = floatMouseX-this.xOffset*scale-middleX;
            float yOffset = floatMouseY-this.yOffset*scale-middleY;

            if (resizingCorner == ResizeButton.Corner.TOP_LEFT) {
                xOffset *= -1;
                yOffset *= -1;
            } else if (resizingCorner == ResizeButton.Corner.TOP_RIGHT) {
                yOffset *= -1;
            } else if (resizingCorner == ResizeButton.Corner.BOTTOM_LEFT) {
                xOffset *= -1;
            }

            float newWidth = xOffset * 2F;
            float newHeight = yOffset * 2F;

            float scaleX = newWidth / width;
            float scaleY = newHeight / height;

            float newScale = Math.max(scaleX, scaleY);

            locationButton.element.setScale(scaleCache + newScale);
            locationButton.drawButton(mc, mouseX, mouseY);
            recalculateResizeButtons();
        } else if (dragging != null) {
            LocationButton lb = locationButtons.get(dragging);
            if (lb == null) {
                return;
            }
            float x = (floatMouseX - xOffset) / (float) sr.getScaledWidth();
            float y = (floatMouseY - yOffset) / (float) sr.getScaledHeight();
            dragging.setPos(x, y);
            addResizeCorners(dragging);
        }
    }

    private void addResizeCorners(GuiElement element) {
        buttonList.removeIf((button) -> button instanceof ResizeButton && ((ResizeButton) button).element == element);
        buttonList.removeIf((button) -> button instanceof ResizeButton && ((ResizeButton) button).element != element);
        LocationButton locationButton = locationButtons.get(element);
        if (locationButton == null) {
            return;
        }

        float boxXOne = locationButton.x - ResizeButton.SIZE * element.getScale();
        float boxXTwo = locationButton.x + element.getActualWidth() + ResizeButton.SIZE * 2 * element.getScale();
        float boxYOne = locationButton.y - ResizeButton.SIZE * element.getScale();
        float boxYTwo = locationButton.y + element.getActualHeight() + ResizeButton.SIZE * 2 * element.getScale();

        buttonList.add(new ResizeButton(boxXOne, boxYOne, element, ResizeButton.Corner.TOP_LEFT));
        buttonList.add(new ResizeButton(boxXTwo, boxYOne, element, ResizeButton.Corner.TOP_RIGHT));
        buttonList.add(new ResizeButton(boxXOne, boxYTwo, element, ResizeButton.Corner.BOTTOM_LEFT));
        buttonList.add(new ResizeButton(boxXTwo, boxYTwo, element, ResizeButton.Corner.BOTTOM_RIGHT));
    }

    private void recalculateResizeButtons() {
        for (GuiButton button : this.buttonList) {
            if (button instanceof ResizeButton) {
                ResizeButton resizeButton = (ResizeButton) button;
                ResizeButton.Corner corner = resizeButton.getCorner();
                GuiElement element = resizeButton.element;
                LocationButton locationButton = locationButtons.get(element);
                if (locationButton == null) {
                    continue;
                }

                float boxXOne = locationButton.x - ResizeButton.SIZE * element.getScale();
                float boxXTwo = locationButton.x + element.getActualWidth() + ResizeButton.SIZE * element.getScale();
                float boxYOne = locationButton.y - ResizeButton.SIZE * element.getScale();
                float boxYTwo = locationButton.y + element.getActualHeight() + ResizeButton.SIZE * element.getScale();

                if (corner == ResizeButton.Corner.TOP_LEFT) {
                    resizeButton.x = boxXOne;
                    resizeButton.y = boxYOne;
                } else if (corner == ResizeButton.Corner.TOP_RIGHT) {
                    resizeButton.x = boxXTwo;
                    resizeButton.y = boxYOne;
                } else if (corner == ResizeButton.Corner.BOTTOM_LEFT) {
                    resizeButton.x = boxXOne;
                    resizeButton.y = boxYTwo;
                } else if (corner == ResizeButton.Corner.BOTTOM_RIGHT) {
                    resizeButton.x = boxXTwo;
                    resizeButton.y = boxYTwo;
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        GuiElement hovered = LocationButton.lastHoveredElement;
        if (hovered != null) {
            hovered.setScale(hovered.getScale() + (Mouse.getEventDWheel() / 1_000f));
        }
    }

    /**
     * Reset the dragged feature when the mouse is released.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
        resizing = false;
        scaleCache = 0;
    }

    /**
     * Saves the positions when the gui is closed
     */
    @Override
    public void onGuiClosed() {
        GuiManager.saveConfig();
    }
}
