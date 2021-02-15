package skytils.skytilsmod.features.impl.misc.damagesplash.graphics.colors;

import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.codec.digest.DigestUtils;
import skytils.skytilsmod.utils.MathUtil;

import java.util.Arrays;

/** CustomColor
 * will represent color or complex colors
 * in a more efficient way than awt's Color or minecraft's color ints.
 *
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class CustomColor {
    public float
            r,  // The RED   value of the color(0.0f -> 1.0f)
            g,  // The GREEN value of the color(0.0f -> 1.0f)
            b,  // The BLUE  value of the color(0.0f -> 1.0f)
            a;  // The ALPHA value of the color(0.0f -> 1.0f)

    public CustomColor(float r, float g, float b) { this(r, g, b, 1.0f); }

    public CustomColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public CustomColor(int r, int g, int b) {
        this.r = r / 255f;
        this.g = g / 255f;
        this.b = b / 255f;
        this.a = 1.0f;
    }

    public CustomColor() {}

    public CustomColor(CustomColor c) { this(c.r, c.g, c.b, c.a); }

    /** applyColor
     * Will set the color to OpenGL's active color
     */
    public void applyColor() {
        GlStateManager.color(r, g, b, a);
    }

    public CustomColor setA(float a) {
        this.a = a;
        return this;
    }

    public static CustomColor fromString(String string, float a) {
        if (string == null) return new CustomColor(0, 0, 0, a);
        String withoutHash = string.trim();
        if (withoutHash.startsWith("#")) withoutHash = withoutHash.substring(1).trim();
        if (withoutHash.equals("")) return new CustomColor(0, 0, 0, a);
        if (withoutHash.length() == 6) {
            try {
                return fromInt(Integer.parseInt(withoutHash, 16), a);
            } catch (Exception ignored) { }
        } else if (withoutHash.length() == 3) {
            // "rgb" -> "rrggbb"
            try {
                int rgb = Integer.parseInt(withoutHash, 16);
                int r = ((rgb >> 8) & 0xF) * 0x11;
                int g = ((rgb >> 4) & 0xF) * 0x11;
                int b = (rgb & 0xF) * 0x11;
                return fromBytes((byte) r, (byte) g, (byte) b, a);
            } catch (Exception ignored) { }
        } else if (withoutHash.length() == 2) {
            // "vv" -> "vvvvvv"
            try {
                byte v = (byte) Integer.parseInt(withoutHash, 16);
                return fromBytes(v, v, v, a);
            } catch (Exception ignored) { }
        }
        byte[] hash = DigestUtils.sha1(string);
        return fromBytes(hash[0], hash[1], hash[2], a);
    }

    public static CustomColor fromHSV(float h, float s, float v, float a) {
        a = MathUtil.clamp(a, 0, 1);
        if (v <= 0) return new CustomColor(0, 0, 0, a);
        if (v > 1) v = 1;
        if (s <= 0) return new CustomColor(v, v, v, a);
        if (s > 1) s = 1;

        float vh = ((h % 1 + 1) * 6) % 6;

        int vi = MathUtil.fastFloor(vh);
        float v1 = v * (1 - s);
        float v2 = v * (1 - s * (vh - vi));
        float v3 = v * (1 - s * (1 - (vh - vi)));

        switch (vi) {
            case 0: return new CustomColor(v, v3, v1, a);
            case 1: return new CustomColor(v2, v, v1, a);
            case 2: return new CustomColor(v1, v, v3, a);
            case 3: return new CustomColor(v1, v2, v, a);
            case 4: return new CustomColor(v3, v1, v, a);
            default: return new CustomColor(v, v1, v2, a);
        }
    }

    /**
     * @return float[4]{ h, s, v, a }
     */
    public float[] toHSV() {
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

    /**
     * `c.toInt() & 0xFFFFFF` to get `0xRRGGBB` (without alpha)
     *
     * @return 0xAARRGGBB
     */
    public int toInt() {
        int r = (int) (Math.min(this.r, 1f) * 255);
        int g = (int) (Math.min(this.g, 1f) * 255);
        int b = (int) (Math.min(this.b, 1f) * 255);
        int a = (int) (Math.min(this.a, 1f) * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Construct a CustomColor from an int 0xAARRGGBB
     *
     * @param argb 32 bits with the most significant 8 being the value of the alpha channel, followed by rgb values
     * @return A custom colour such that fromInt(x).toInt() == x
     */
    public static CustomColor fromInt(int argb) {
        return fromInt(argb & 0xFFFFFF, ((argb >>> 24) & 0xFF) / 255f);
    }

    /**
     * Construct a CustomColor from an int 0xRRGGBB and an alpha value
     *
     * @param rgb 24 bits (Most significant 8 are ignored)
     * @param a Alpha value of colour
     * @return A custom colour such that fromInt(rgb, a).toInt() & 0xFFFFFF == rgb && fromInt(rgb, a).a == a
     */
    public static CustomColor fromInt(int rgb, float a) {
        return fromBytes((byte) (rgb >>> 16), (byte) (rgb >>> 8), (byte) rgb, a);
    }

    public static CustomColor fromBytes(byte r, byte g, byte b) {
        return fromBytes(r, g, b, 0);
    }

    public static CustomColor fromBytes(byte r, byte g, byte b, byte a) {
        return fromBytes(r, g, b, Byte.toUnsignedInt(a) / 255f);
    }

    public static CustomColor fromBytes(byte r, byte g, byte b, float a) {
        return new CustomColor(Byte.toUnsignedInt(r) / 255f, Byte.toUnsignedInt(g) / 255f, Byte.toUnsignedInt(b) / 255f, a);
    }

    /** HeyZeer0: this is = rgba(1,1,1,1) **/
    @Override
    public String toString() {
        return "rgba(" + r + "," + g + "," + b + "," + a +")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other instanceof CustomColor) {
            CustomColor c = (CustomColor) other;
            return r == c.r && g == c.g && b == c.b && a == c.a;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new float[]{ r, g, b, a });
    }

    /* package-private */ static class SetBase extends CustomColor {
        SetBase(int rgb) {
            super((rgb >> 16) / 255.f, ((rgb >> 8) & 0xFF) / 255.f, (rgb & 0xFF) / 255.f, 1);
        }

        SetBase(float r, float g, float b, float a) {
            super(r, g, b, a);
        }

        // Prevent setA on global references. Create a copy with `new CustomColor(c)` first.
        @Override public CustomColor setA(float a) {
            new UnsupportedOperationException("Cannot set alpha of common color").printStackTrace();
            return this;
        }
    }

}
