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

package skytils.skytilsmod.features.impl.misc

import net.minecraft.client.Minecraft
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors

class BonzoMaskTimer {

    class BonzoMaskGuiElement : GuiElement(name = "Bonzo Timer", fp = FloatPair(10, 10)) {
        override fun render() {
            if (Utils.inDungeons && toggled) {
                var text = "§6Bonzo Mask: "
                var mc = Minecraft.getMinecraft()
                var item = mc.thePlayer.getCurrentArmor(3)
                if (item != null && item.displayName.contains("Bonzo's Mask") || bonzoUse > 0) {
                    var time = System.currentTimeMillis() / 1000
                    if (bonzoUse - time < 0)
                        text += "§aREADY"
                    else {
                        if (Skytils.config.bonzoMaskMode == 0)
                            text += "§c " + timeBetween(time, bonzoUse.toLong())
                        else if (Skytils.config.bonzoMaskMode == 1)
                            text += "§c " + (bonzoUse - time) + "s"
                    }
                    fr.drawString(
                        text,
                        0f,
                        0f,
                        CommonColors.GREEN,
                        SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                        SmartFontRenderer.TextShadow.NONE
                    )
                }
            }
        }

        override fun demoRender() {
            fr.drawString(
                "§6Bonzo Mask: §aREADY",
                0f,
                0f,
                CommonColors.GREEN,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NONE
            )
        }

        override val toggled: Boolean
            get() = Skytils.config.bonzoMaskTimer
        override val height: Int
            get() = fr.FONT_HEIGHT
        override val width: Int
            get() = fr.getStringWidth("§6Bonzo Mask: §cREADY")

        init {
            Skytils.guiManager.registerElement(this)
        }

        private fun timeBetween(timeOne: Long, timeTwo: Long): String {
            val totalSecs = timeTwo - timeOne
            val minutes = totalSecs / 60
            val seconds = totalSecs % 60
            return ("" + minutes + "m" + seconds + "s")
        }

    }


    @SubscribeEvent
    fun onWorld(event: WorldEvent.Load) {
        bonzoUse = 0
    }

    companion object {
        var bonzoUse = 0

        init {
            BonzoMaskGuiElement()
        }
    }


}