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
import net.minecraft.client.gui.ScaledResolution;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;

public abstract class GuiElement {
    private static final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
    String name;
    float scale;
    FloatPair pos;

    public GuiElement(String name) {
        this(name, new FloatPair(0, 0));
    }

    public GuiElement (String name, FloatPair fp) {
        this(name, 1.0F, fp);
    }

    public GuiElement(String name, float scale, FloatPair fp) {
        this.name = name;
        this.scale = GuiManager.GUISCALES.getOrDefault(name, scale);
        this.pos = GuiManager.GUIPOSITIONS.getOrDefault(name, fp);
    }

    public abstract void render();

    public abstract void demoRender();

    public float getScale() {
        return this.scale;
    }

    public void setScale(float newScale) {
        this.scale = newScale;
    }

    public abstract boolean getToggled();

    public void setPos(FloatPair newPos) {
        this.pos = newPos;
    }

    public FloatPair getPos() {
        return this.pos;
    }

    public void setPos(int x, int y) {
        float fX = x / (float) sr.getScaledWidth();
        float fY = y / (float) sr.getScaledHeight();
        setPos(fX, fY);
    }

    public void setPos(float x, float y) {
        this.pos = new FloatPair(x, y);
    }

    public String getName() {
        return this.name;
    }

    public float getActualX() {
        int maxX = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
        return (maxX * getPos().getX());
    }

    public float getActualY() {
        int maxY = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
        return (maxY * getPos().getY());
    }

    public abstract int getHeight();

    public abstract int getWidth();

    public float getActualHeight() {
        return this.getHeight() * this.getScale();
    }

    public float getActualWidth() {
        return this.getWidth() * this.getScale();
    }
}
