package skytils.skytilsmod.utils.graphics.colors;

import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

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
        if (string == null || !string.startsWith("rainbow(")) return null;
        int first = string.indexOf(",");
        int second = string.indexOf(",", first + 1);
        int third = string.lastIndexOf(",");
        int speed = Integer.parseInt(string.substring(string.indexOf("(") + 1, first));
        int offset = Integer.parseInt(string.substring(first + 1, second));
        float saturation = Integer.parseInt(string.substring(second + 1, third));
        float brightness = Integer.parseInt(string.substring(third + 1, string.indexOf(")")));
        return new RainbowColor(speed, offset, saturation, brightness);
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
        return super.hashCode();
    }
}
