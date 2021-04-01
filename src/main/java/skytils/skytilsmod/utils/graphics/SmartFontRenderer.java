package skytils.skytilsmod.utils.graphics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJModel;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;
import skytils.skytilsmod.utils.graphics.colors.CustomColor;
import skytils.skytilsmod.utils.graphics.colors.MinecraftChatColors;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class SmartFontRenderer extends FontRenderer {

    public static final int LINE_SPACING = 3;
    public static final int CHAR_SPACING = 0;
    public static final int CHAR_HEIGHT = 9;

    // Array of 16 CustomColors where minecraftColors[0xX] is the colour for §X
    private static final int[] minecraftColors = MinecraftChatColors.set.asInts();

    public SmartFontRenderer() {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().getTextureManager(), false);
    }

    public float drawString(String text, float x, float y, CustomColor customColor, TextAlignment alignment, TextShadow shadow) {
        if (text == null) return 0f;

        if (customColor == CommonColors.RAINBOW) {
            return drawRainbowText(text, x, y, alignment, shadow);
        } else if (customColor == CommonColors.CRITICAL) {
            return drawCritText(text, x, y, alignment, shadow);
        }

        String drawnText = text.replaceAll("§\\[\\d+\\.?\\d*,\\d+\\.?\\d*,\\d+\\.?\\d*\\]", "");
        switch (alignment) {
            case MIDDLE:
                return drawString(text, x - getStringWidth(drawnText) / 2.0f, y, customColor, TextAlignment.LEFT_RIGHT, shadow);
            case RIGHT_LEFT:
                return drawString(text, x - getStringWidth(drawnText), y, customColor, TextAlignment.LEFT_RIGHT, shadow);
            default:
                GlStateManager.enableTexture2D();
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                int colour = customColor.toInt();
                posX = x;
                posY = y;
                return drawChars(text, colour, shadow);
        }

    }

    private float drawRainbowText(String input, float x, float y, TextAlignment alignment, TextShadow shadow) {
        if (alignment == TextAlignment.MIDDLE)
            return drawRainbowText(input, x - getStringWidth(input) / 2.0f, y, TextAlignment.LEFT_RIGHT, shadow);
        else if (alignment == TextAlignment.RIGHT_LEFT)
            return drawRainbowText(input, x - getStringWidth(input), y, TextAlignment.LEFT_RIGHT, shadow);

        posX = x; posY = y;

        for (char c : input.toCharArray()) {
            long dif = ((long)posX * 10) - ((long)posY * 10);

            // color settings
            long time = System.currentTimeMillis() - dif;
            float z = 2000.0F;
            int color = Color.HSBtoRGB((float) (time % (int) z) / z, 0.8F, 0.8F);

            float red = (float)(color >> 16 & 255) / 255.0F;
            float blue = (float)(color >> 8 & 255) / 255.0F;
            float green = (float)(color & 255) / 255.0F;

            // rendering shadows
            float originPosX = posX; float originPosY = posY;
            float offset = (this.getUnicodeFlag() ? 0.5f : 1f);
            switch (shadow) {
                case OUTLINE:
                    GlStateManager.color(red * (1 - 0.8f), green * (1 - 0.8f), blue * (1 - 0.8f), 1);
                    posX = originPosX - offset;
                    posY = originPosY;
                    renderChar(c);
                    posX = originPosX + offset;
                    posY = originPosY;
                    renderChar(c);
                    posX = originPosX;
                    posY = originPosY - offset;
                    renderChar(c);
                    posX = originPosX;
                    posY = originPosY + offset;
                    renderChar(c);
                    posX = originPosX;
                    posY = originPosY;
                    break;
                case NORMAL:
                    GlStateManager.color(red * (1 - 0.8f), green * (1 - 0.8f), blue * (1 - 0.8f), 1);
                    posX = originPosX + offset;
                    posY = originPosY + offset;
                    renderChar(c);
                    posX = originPosX;
                    posY = originPosY;
                    break;
            }

            // rendering the text
            GlStateManager.color(red, green, blue, 1);
            float charLength = renderChar(c);
            posX += charLength + CHAR_SPACING;
        }

        return posX;
    }

    private float drawCritText(String input, float x, float y, TextAlignment alignment, TextShadow shadow) {
        if (alignment == TextAlignment.MIDDLE)
            return drawCritText(input, x - getStringWidth(input) / 2.0f, y, TextAlignment.LEFT_RIGHT, shadow);
        else if (alignment == TextAlignment.RIGHT_LEFT)
            return drawCritText(input, x - getStringWidth(input), y, TextAlignment.LEFT_RIGHT, shadow);

        posX = x; posY = y;

        for (char c : input.toCharArray()) {
            long dif = ((long)posX * 10) - ((long)posY * 10);

            // color settings
            long time = System.currentTimeMillis() - dif;

            float red = 1.0F;
            float blue = 85.0F / 255.0F;
            float green = (85.0F * (float)Math.cos(time % 2000 * Math.PI / 1000) + 170) / 255.0F;

            // rendering shadows
            float originPosX = posX; float originPosY = posY;
            float offset = (this.getUnicodeFlag() ? 0.5f : 1f);
            switch (shadow) {
                case OUTLINE:
                    GlStateManager.color(red * (1 - 0.8f), green * (1 - 0.8f), blue * (1 - 0.8f), 1);
                    posX = originPosX - offset;
                    posY = originPosY;
                    renderChar(c);
                    posX = originPosX + offset;
                    posY = originPosY;
                    renderChar(c);
                    posX = originPosX;
                    posY = originPosY - offset;
                    renderChar(c);
                    posX = originPosX;
                    posY = originPosY + offset;
                    renderChar(c);
                    posX = originPosX;
                    posY = originPosY;
                    break;
                case NORMAL:
                    GlStateManager.color(red * (1 - 0.8f), green * (1 - 0.8f), blue * (1 - 0.8f), 1);
                    posX = originPosX + offset;
                    posY = originPosY + offset;
                    renderChar(c);
                    posX = originPosX;
                    posY = originPosY;
                    break;
            }

            // rendering the text
            GlStateManager.color(red, green, blue, 1);
            float charLength = renderChar(c);
            posX += charLength + CHAR_SPACING;
        }

        return posX;
    }

    private float drawChars(String text, int color, TextShadow shadow) {
        if (text.isEmpty()) return -CHAR_SPACING;

        float textLength = 0f;
        boolean obfuscated = false;
        boolean italic = false;
        boolean bold = false;

        for (int index = 0; index < text.length(); index++) {

            // color codes identification
            if (text.charAt(index) == '§' && index + 1 < text.length()) {
                Integer detectedColor;
                detectedColor = decodeCommonColor(text.charAt(index + 1));

                if (detectedColor == null) { // means that or it's an invalid color code or a special one
                    switch (text.charAt(index + 1)) {
                        case 'k':
                            obfuscated = true;
                            index++; // skips the the next char
                            break;
                        case 'o':
                            italic = true;
                            index++; // skips the the next char
                            break;
                        case 'l':
                            bold = true;
                            index++;
                            break;
                        case 'r': // stands for reset
                            obfuscated = false;
                            italic = false;
                            bold = false;
                            detectedColor = 0xFFFFFF;
                            index++; // skips the the next char
                            break;
                    }
                } else { // if a valid color is found remove special effects
                    obfuscated = false;
                    italic = false;
                    bold = false;
                    index++;
                }

                if (detectedColor != null) {
                    detectedColor &= 0xFFFFFF;
                    detectedColor |= color & 0xFF000000;

                    color = detectedColor;
                }

                continue;
            }

            char character = text.charAt(index);
            if (obfuscated) {
                String characters = "\u0020\u0021\"\u0023\u0024\u0025\u0026\u0027\u0028\u0029\u002a\u002b\u002c\u002d\u002e\u002f\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037\u0038\u0039\u003a\u003b\u003c\u003d\u003e\u003f\u0040\u0041\u0042\u0043\u0044\u0045\u0046\u0047\u0048\u0049\u004a\u004b\u004c\u004d\u004e\u004f\u0050\u0051\u0052\u0053\u0054\u0055\u0056\u0057\u0058\u0059\u005a\u005b\\\u005d\u005e\u005f\u0060\u0061\u0062\u0063\u0064\u0065\u0066\u0067\u0068\u0069\u006a\u006b\u006c\u006d\u006e\u006f\u0070\u0071\u0072\u0073\u0074\u0075\u0076\u0077\u0078\u0079\u007a\u007b\u007c\u007d\u007e\u00a1\u00a3\u00aa\u00ab\u00ac\u00ae\u00b0\u00b1\u00b2\u00b7\u00ba\u00bb\u00bc\u00bd\u00bf\u00c0\u00c1\u00c2\u00c4\u00c5\u00c6\u00c7\u00c8\u00c9\u00ca\u00cb\u00cd\u00d1\u00d3\u00d4\u00d5\u00d6\u00d7\u00d8\u00da\u00dc\u00df\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u00e6\u00e7\u00e8\u00e9\u00ea\u00eb\u00ec\u00ed\u00ee\u00ef\u00f1\u00f2\u00f3\u00f4\u00f5\u00f6\u00f7\u00f8\u00f9\u00fa\u00fb\u00fc\u00ff\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0192\u0207\u0393\u0398\u03a3\u03a6\u03a9\u03b1\u03b2\u03b4\u03bc\u03c0\u03c3\u03c4\u207f\u2205\u2208\u2219\u221a\u221e\u2229\u2248\u2261\u2264\u2265\u2320\u2321\u2500\u2502\u250c\u2510\u2514\u2518\u251c\u2524\u252c\u2534\u253c\u2550\u2551\u2552\u2553\u2554\u2555\u2556\u2557\u2558\u2559\u255a\u255b\u255c\u255d\u255e\u255f\u2560\u2561\u2562\u2563\u2564\u2565\u2566\u2567\u2568\u2569\u256a\u256b\u256c\u2580\u2584\u2588\u258c\u2590\u2591\u2592\u2593\u25a0";
                if (characters.contains(String.valueOf(character))) {
                    int width = this.getCharWidth(character);
                    char newCharacter;
                    do {
                        newCharacter = characters.charAt((new Random()).nextInt(characters.length()));
                    } while (width != this.getCharWidth(newCharacter));
                    character = newCharacter;
                }
            }

            float x = posX;
            float y = posY;
            float offset = (this.getUnicodeFlag() ? 0.5f : 1f);
            float alpha = ((color >> 24) & 0xFF) / 255F;
            this.setColor(0F, 0F, 0F, alpha);

            // rendering shadows
            switch (shadow) {
                case OUTLINE:
                    posX -= offset;
                    renderChar(character);
                    posX += bold ? offset*3 : offset*2;
                    renderChar(character);
                    for (int i = 0; i < (bold ? 2 : 1); i++) {
                        posX -= offset;
                        posY -= offset;
                        renderChar(character);
                        posY += offset * 2;
                        renderChar(character);
                        posY -= offset;
                    }
                    break;
                case NORMAL:
                    posY = y + offset;
                    for (int i = 0; i < (bold ? 2 : 1); i++) {
                        posX += offset;
                        renderChar(character);
                    }
                    break;
                default:
            }

            posX = x;
            posY = y;

            float red = ((color >> 16) & 0xFF) / 255F;
            float green = ((color >> 8) & 0xFF) / 255F;
            float blue = (color & 0xFF) / 255F;
            // Alpha calculated for shadow

            this.setColor(red, green, blue, alpha);
            float charLength = renderChar(character, italic);

            if (bold) {
                posX += offset;
                renderChar(character);
                posX -= offset;

                charLength += offset;
            }

            posX += charLength + CHAR_SPACING;
            textLength += charLength + CHAR_SPACING;
        }

        return textLength - CHAR_SPACING;
    }

    private static Integer decodeCommonColor(char character) {

        if ('0' <= character && character <= '9') return minecraftColors[character - '0'];
        if ('a' <= character && character <= 'f') return minecraftColors[character + (10 - 'a')];
        if ('A' <= character && character <= 'F') return minecraftColors[character + (10 - 'A')];

        return null;
    }

    private float renderChar(char ch) {
        return renderChar(ch, false);
    }

    private float renderChar(char ch, boolean italic) {
        if (ch == 160) return 4.0F;  // forge: display nbsp as space. MC-2595

        if (ch == 32) {
            return 4.0F;
        } else {
            int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(ch);
            return i != -1 && !this.getUnicodeFlag() ? this.renderDefaultChar(i, italic) : this.renderUnicodeChar(ch, italic);
        }
    }

    public enum TextAlignment {
        LEFT_RIGHT, MIDDLE, RIGHT_LEFT
    }

    public enum TextShadow {
        NONE, NORMAL, OUTLINE
    }

}
