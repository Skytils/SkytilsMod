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

package gg.skytils.skytilsmod.gui.elements

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.graphics.DynamicResource
import gg.skytils.skytilsmod.utils.nextOrNull
import kotlinx.coroutines.launch
import net.minecraft.util.ResourceLocation
import javax.imageio.ImageIO

data class GIFResource(
    val loc: ResourceLocation,
    val name: String = loc.resourcePath.substringAfterLast("/").substringBeforeLast("."),
    val frameDelay: Int = 1
) {
    init {
        Skytils.IO.launch {
            frames
        }
    }

    private val frames: List<DynamicResource> by lazy {
        return@lazy ImageIO.createImageInputStream(
            mc.resourceManager.getResource(loc).inputStream
        ).use { stream ->
            ImageIO.getImageReaders(stream).nextOrNull()?.run {
                input = stream
                (0..<getNumImages(true)).map {
                    DynamicResource("skytils_${name}_frame", read(it))
                }
            }
        } ?: emptyList()
    }
    private lateinit var currentFrame: DynamicResource
    private var frameCounter = 0
    private val maxCounter by lazy {
        frames.size * frameDelay
    }

    fun draw() {
        if (frames.isEmpty()) return
        if (frameCounter++ % frameDelay == 0) {
            if (frameCounter >= maxCounter) frameCounter = 0
            currentFrame = frames[frameCounter / frameDelay]
        }
        RenderUtil.renderTexture(
            currentFrame.resource,
            0,
            0,
            currentFrame.image.width,
            currentFrame.image.height
        )
    }
}