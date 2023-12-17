/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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
package gg.skytils.skytilsmod.features.impl.misc.damagesplash

import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
@Suppress("unused")
enum class Damage(val symbol: String, val color: CustomColor) {
    CRITICAL("✧", CommonColors.CRITICAL),
    PET("♞", CommonColors.MAGENTA),
    WITHER("☠", CommonColors.BLACK),
    SKULL("✷", CommonColors.GRAY),
    CURSE("☄", CommonColors.RED),
    TRUE("❂", CommonColors.WHITE),
    FIRE("火", CommonColors.ORANGE),
    DROWN("水", CommonColors.LIGHT_BLUE),
    OVERLOAD("✯", CommonColors.YELLOW),
    COMBO("ﬗ", CustomColor(242, 133, 0)),
    NORMAL("", CommonColors.LIGHT_GRAY);

    companion object {
        fun fromSymbol(symbol: String): Damage? {
            return entries.find { it.symbol == symbol }
        }
    }
}