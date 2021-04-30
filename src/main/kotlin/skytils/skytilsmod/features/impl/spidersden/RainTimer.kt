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
package skytils.skytilsmod.features.impl.spidersden

import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors

/**
 * Adopted from PikaFan's Rain Timer Website, licensed under GPL v3
 * @link https://github.com/PikaFan123/rain-timer
 */
class RainTimer {
    class RainTimerGuiElement : GuiElement(name = "Rain Timer", fp = FloatPair(10, 10)) {
        override fun render() {
            if (Utils.inSkyblock && toggled) {
                if (nextRain < System.currentTimeMillis()) nextRain += 4850000
                val remainingRain = ((nextRain - System.currentTimeMillis()) - 3850000) / 1000L
                if (remainingRain > 0) {
                    fr.drawString(
                        "${remainingRain / 60}:${"%02d".format(remainingRain % 60)}",
                        0f,
                        0f,
                        CommonColors.BLUE,
                        SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                        SmartFontRenderer.TextShadow.NONE
                    )
                } else {
                    val secondsToNext = (nextRain - System.currentTimeMillis()) / 1000L
                    fr.drawString(
                        "${secondsToNext / 60}:${"%02d".format(secondsToNext % 60)}",
                        0f,
                        0f,
                        CommonColors.ORANGE,
                        SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                        SmartFontRenderer.TextShadow.NONE
                    )
                }
            }
        }

        override fun demoRender() {
            fr.drawString(
                "99:99",
                0f,
                0f,
                CommonColors.ORANGE,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NONE
            )
        }

        override val toggled: Boolean
            get() = Skytils.config.rainTimer
        override val height: Int
            get() = fr.FONT_HEIGHT
        override val width: Int
            get() = fr.getStringWidth("99:99")

        init {
            Skytils.GUIMANAGER.registerElement(this)
        }

    }

    companion object {
        var nextRain = 1596552707000

        init {
            RainTimerGuiElement()
            while (nextRain < System.currentTimeMillis()) nextRain += 4850000
        }
    }
}