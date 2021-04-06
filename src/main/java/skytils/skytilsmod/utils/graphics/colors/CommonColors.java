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

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class CommonColors extends CustomColor.SetBase {

    private CommonColors(int rgb) {
        super(rgb);
    }

    private CommonColors() {
        super(-10, -10, -10, 1);
    }

    public static final CommonColors BLACK = new CommonColors(0x000000);
    public static final CommonColors RED = new CommonColors(0xff0000);
    public static final CommonColors GREEN = new CommonColors(0x00ff00);
    public static final CommonColors BLUE = new CommonColors(0x0000ff);
    public static final CommonColors YELLOW = new CommonColors(0xffff00);
    public static final CommonColors BROWN = new CommonColors(0x563100);
    public static final CommonColors PURPLE = new CommonColors(0xb200ff);
    public static final CommonColors CYAN = new CommonColors(0x438e82);
    public static final CommonColors LIGHT_GRAY = new CommonColors(0xadadad);
    public static final CommonColors GRAY = new CommonColors(0x636363);
    public static final CommonColors PINK = new CommonColors(0xffb7b7);
    public static final CommonColors LIGHT_GREEN = new CommonColors(0x49ff59);
    public static final CommonColors LIGHT_BLUE = new CommonColors(0x00e9ff);
    public static final CommonColors MAGENTA = new CommonColors(0xff0083);
    public static final CommonColors ORANGE = new CommonColors(0xff9000);
    public static final CommonColors WHITE = new CommonColors(0xffffff);
    public static final CommonColors RAINBOW = new CommonColors();
    public static final CommonColors CRITICAL = new CommonColors();

    private static final CommonColors[] colors = {
            BLACK,      RED,     GREEN,  BLUE,
            YELLOW,     BROWN,   PURPLE, CYAN,
            LIGHT_GRAY, GRAY,    PINK,   LIGHT_GREEN,
            LIGHT_BLUE, MAGENTA, ORANGE, WHITE
    };

    private static final String[] names = {
            "BLACK",      "RED",     "GREEN",  "BLUE",
            "YELLOW",     "BROWN",   "PURPLE", "CYAN",
            "LIGHT_GRAY", "GRAY",    "PINK",   "LIGHT_GREEN",
            "LIGHT_BLUE", "MAGENTA", "ORANGE", "WHITE"
    };

    public static final ColorSet<CommonColors> set = new ColorSet<>(colors, names);

}
