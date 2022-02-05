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

package skytils.skytilsmod.mixins.hooks.gui

import net.minecraft.client.gui.GuiMainMenu
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiMainMenu
import skytils.skytilsmod.utils.NumberUtil.addSuffix
import java.util.*

fun setSplashText(gui: GuiMainMenu, cal: Calendar) {
    gui as AccessorGuiMainMenu
    if (cal.get(Calendar.MONTH) + 1 == 2 && cal.get(Calendar.DATE) == 5) {
        val numBirthday = cal.get(Calendar.YEAR) - 2021
        gui.splashText = "§z§kstay§z Happy ${numBirthday.addSuffix()} Birthday Skytils! §kmadL"
    }
}