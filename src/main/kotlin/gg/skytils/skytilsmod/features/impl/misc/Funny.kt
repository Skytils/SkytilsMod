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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.structure.FloatPair
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.utils.RenderUtil.renderTexture
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.graphics.DynamicResource
import gg.skytils.skytilsmod.utils.nextOrNull
import kotlinx.coroutines.launch
import net.minecraft.util.ResourceLocation
import javax.imageio.ImageIO

object Funny {
    init {
        GuiManager.registerElement(JamCatElement)
    }

    object JamCatElement : GuiElement("Jamcat", fp = FloatPair(0, 0)) {

        init {
            if (toggled) {
                Skytils.IO.launch {
                    frames
                }
            }
        }

        private val frames: List<ResourceLocation> by lazy {
            ImageIO.createImageInputStream(javaClass.getResourceAsStream("assets/skytils/splashes/jamcat.gif"))
                .use { stream ->
                    ImageIO.getImageReaders(stream).nextOrNull()?.apply {
                        input = stream
                        return@lazy (0 until getNumImages(true)).map {
                            DynamicResource("skytils_jamcat_frame", read(it)).resource
                        }
                    }
                }
            return@lazy emptyList()
        }
        private var currentFrame = 0

        override fun render() {
            if (!toggled || frames.isEmpty()) return
            renderTexture(frames[currentFrame++ % frames.size], 0, 0, 128, 128)
        }

        override fun demoRender() = render()

        override val toggled: Boolean
            get() = SuperSecretSettings.jamCat
        override val height: Int = 128
        override val width: Int = 128
    }
}