/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package gg.skytils.skytilsmod.features.impl.misc

import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.structure.FloatPair
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.gui.elements.GIFResource
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.getSkytilsResource
import net.minecraft.util.MathHelper
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Funny {
    var ticks = 0
    var alphaMult = 0f

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (SuperSecretSettings.bennettArthur) {
            if (++ticks >= 360) ticks = 0
            alphaMult = MathHelper.sin(ticks * 0.0174533f)
        } else {
            ticks = 0
            alphaMult = 1f
        }
    }

    init {
        GuiManager.registerElement(JamCatElement)
    }

    object JamCatElement : GuiElement("Jamcat", fp = FloatPair(0, 0)) {
        val gif by lazy {
            GIFResource(getSkytilsResource("splashes/jamcat.gif"), frameDelay = 5)
        }

        override fun render() {
            if (!toggled) return
            gif.draw()
        }

        override fun demoRender() = render()

        override val toggled: Boolean
            get() = SuperSecretSettings.jamCat
        override val height: Int = 128
        override val width: Int = 128
    }
}