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

package skytils.skytilsmod.utils.graphics.colors;

import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.Arrays;

public class RainbowColor extends CustomColor {

    public int speed, offset;
    public float saturation, brightness;

    public RainbowColor(int speed, int offset, float saturation, float brightness) {
        super();
        this.speed = speed;
        this.offset = offset;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public Color getRainbowColor() {
        float hue = (System.currentTimeMillis() + offset) % (speed == 0 ? 1 : speed);
        hue /= (speed == 0 ? 1 : speed);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    @Override
    public void applyColor() {
        Color color = getRainbowColor();
        GlStateManager.color(color.getRed() / 255f, color.getBlue() / 255f, color.getGreen() / 255f);
    }

    public static RainbowColor fromString(String string) {
        if (string == null) throw new NullPointerException("Argument cannot be null!");
        if (!string.startsWith("rainbow(") && !string.endsWith(")")) throw new IllegalArgumentException("Invalid rainbow color format");
        try {
            int first = string.indexOf(",");
            int second = string.indexOf(",", first + 1);
            int third = string.lastIndexOf(",");
            int speed = Integer.parseInt(string.substring(string.indexOf("(") + 1, first));
            int offset = Integer.parseInt(string.substring(first + 1, second));
            float saturation = Float.parseFloat(string.substring(second + 1, third));
            float brightness = Float.parseFloat(string.substring(third + 1, string.indexOf(")")));
            return new RainbowColor(speed, offset, saturation, brightness);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid rainbow string");
        }
    }

    @Override
    public float[] toHSV() {
        Color color = getRainbowColor();
        float r = color.getRed(), g = color.getBlue(), b = color.getBlue(), a = color.getAlpha();
        float hue, saturation, value;
        float cmax = Math.max(Math.max(r, g), b);
        float cmin = Math.min(Math.min(r, g), b);

        value = cmax;
        saturation = cmax == 0 ? 0 : (cmax - cmin) / cmax;
        if (saturation == 0) {
            hue = 0;
        } else {
            float redc = (cmax - r) / (cmax - cmin);
            float greenc = (cmax - g) / (cmax - cmin);
            float bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax) {
                hue = bluec - greenc;
            } else if (g == cmax) {
                hue = 2.0f + redc - bluec;
            } else {
                hue = 4.0f + greenc - redc;
            }
            hue = hue / 6.0f;
            if (hue < 0) {
                hue = hue + 1.0f;
            }
        }

        return new float[]{ hue, saturation, value, a };
    }

    @Override
    public int toInt() {
        return getRainbowColor().getRGB();
    }

    @Override
    public String toString() {
        return "rainbow("+speed+","+offset+","+saturation+","+brightness+")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other instanceof RainbowColor) {
            RainbowColor c = (RainbowColor) other;
            return speed == c.speed && offset == c.offset && saturation == c.saturation && brightness == c.brightness;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new float[]{ speed, offset, saturation, brightness });
    }
}
