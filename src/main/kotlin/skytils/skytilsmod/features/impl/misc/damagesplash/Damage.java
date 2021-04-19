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

package skytils.skytilsmod.features.impl.misc.damagesplash;

import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.util.regex.Pattern;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public enum Damage {

    CRITICAL("✧", CommonColors.CRITICAL),
    PET("♞", CommonColors.MAGENTA),
    WITHER("☠", CommonColors.BLACK),
    SKULL("✷", CommonColors.GRAY),
    TRUE("❂", CommonColors.WHITE),
    FIRE("火", CommonColors.ORANGE),
    NORMAL("", CommonColors.LIGHT_GRAY);


    private final String symbol;
    private final CommonColors color;

    Damage(String symbol, CommonColors color) {
        this.symbol = symbol;
        this.color = color;
    }

    public String getSymbol() {
        return symbol;
    }

    public CommonColors getColor() {
        return color;
    }

    public static Damage fromSymbol(String symbol) {
        for (Damage type : values()) {
            if (type.symbol.equals(symbol))
                return type;
        }
        return null;
    }

    public static Pattern compileDamagePattern() {
        StringBuilder damageTypes = new StringBuilder();

        for (Damage type : values()) {
            damageTypes.append(type.getSymbol());
        }

        return Pattern.compile("-(.*?) ([" + damageTypes.toString() + "])");
    }

}
