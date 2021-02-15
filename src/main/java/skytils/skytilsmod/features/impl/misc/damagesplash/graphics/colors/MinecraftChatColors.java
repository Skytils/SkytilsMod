package skytils.skytilsmod.features.impl.misc.damagesplash.graphics.colors;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class MinecraftChatColors extends CustomColor.SetBase {

    private MinecraftChatColors(int rgb) {
        super(rgb);
    }

    public static final MinecraftChatColors BLACK = new MinecraftChatColors(0x000000);
    public static final MinecraftChatColors DARK_BLUE = new MinecraftChatColors(0x0000AA);
    public static final MinecraftChatColors DARK_GREEN = new MinecraftChatColors(0x00AA00);
    public static final MinecraftChatColors DARK_AQUA = new MinecraftChatColors(0x00AAAA);
    public static final MinecraftChatColors DARK_RED = new MinecraftChatColors(0xAA0000);
    public static final MinecraftChatColors DARK_PURPLE = new MinecraftChatColors(0xAA00AA);
    public static final MinecraftChatColors GOLD = new MinecraftChatColors(0xFFAA00);
    public static final MinecraftChatColors GRAY = new MinecraftChatColors(0xAAAAAA);
    public static final MinecraftChatColors DARK_GRAY = new MinecraftChatColors(0x555555);
    public static final MinecraftChatColors BLUE = new MinecraftChatColors(0x5555FF);
    public static final MinecraftChatColors GREEN = new MinecraftChatColors(0x55FF55);
    public static final MinecraftChatColors AQUA = new MinecraftChatColors(0x55FFFF);
    public static final MinecraftChatColors RED = new MinecraftChatColors(0xFF5555);
    public static final MinecraftChatColors LIGHT_PURPLE = new MinecraftChatColors(0xFF55FF);
    public static final MinecraftChatColors YELLOW = new MinecraftChatColors(0xFFFF55);
    public static final MinecraftChatColors WHITE = new MinecraftChatColors(0xFFFFFF);

    private static final MinecraftChatColors[] colors = {
            BLACK,     DARK_BLUE,    DARK_GREEN, DARK_AQUA,
            DARK_RED,  DARK_PURPLE,  GOLD,       GRAY,
            DARK_GRAY, BLUE,         GREEN,      AQUA,
            RED,       LIGHT_PURPLE, YELLOW,     WHITE
    };

    private static final String[] names = {
            "BLACK",     "DARK_BLUE",    "DARK_GREEN", "DARK_AQUA",
            "DARK_RED",  "DARK_PURPLE",  "GOLD",       "GRAY",
            "DARK_GRAY", "BLUE",         "GREEN",      "AQUA",
            "RED",       "LIGHT_PURPLE", "YELLOW",     "WHITE"
    };

    public static final ColorSet<MinecraftChatColors> set = new ColorSet<>(colors, names);

}
