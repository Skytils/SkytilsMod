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
