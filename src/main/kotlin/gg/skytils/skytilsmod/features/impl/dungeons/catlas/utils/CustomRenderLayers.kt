/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorRenderLayer
import net.minecraft.client.gl.Defines
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.LayeringTransform
import net.minecraft.client.render.OutputTarget
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderSetup
import net.minecraft.util.Identifier

object CustomRenderLayers {

    /**
     * A RenderPipeline that inherits all properties from a base pipeline but with
     * NO_DEPTH_TEST (depth always passes) and depth writes disabled.
     * Used for ESP rendering that should be visible through walls.
     */
    private class EspPipeline(base: RenderPipeline, id: String) : RenderPipeline(
        Identifier.of("skytils", id),
        base.vertexShader,
        base.fragmentShader,
        base.shaderDefines,
        base.samplers,
        base.uniforms,
        base.blendFunction,
        DepthTestFunction.NO_DEPTH_TEST,
        base.polygonMode,
        base.isCull,
        base.isWriteColor,
        base.isWriteAlpha,
        false, // writeDepth: false so ESP layers don't occlude other geometry
        base.colorLogic,
        base.vertexFormat,
        base.vertexFormatMode,
        base.depthBiasScaleFactor,
        base.depthBiasConstant,
        base.sortKey
    )

    /**
     * Lines render layer for ESP — always-pass depth test so lines are visible through walls.
     * Based on LINES_TRANSLUCENT with VIEW_OFFSET_Z_LAYERING + ITEM_ENTITY_TARGET (matches vanilla).
     */
    val espLines: RenderLayer by lazy {
        val pipeline = EspPipeline(RenderPipelines.LINES_TRANSLUCENT, "esp_lines")
        val setup = RenderSetup.builder(pipeline)
            .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .build()
        AccessorRenderLayer.of("skytils:esp_lines", setup)
    }

    /**
     * Filled-box render layer for ESP — always-pass depth test so boxes are visible through walls.
     * Based on DEBUG_FILLED_BOX with translucent + VIEW_OFFSET_Z_LAYERING (matches vanilla).
     */
    val espFilledBoxLayer: RenderLayer by lazy {
        val pipeline = EspPipeline(RenderPipelines.DEBUG_FILLED_BOX, "esp_filled_box")
        val setup = RenderSetup.builder(pipeline)
            .translucent()
            .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .build()
        AccessorRenderLayer.of("skytils:esp_filled_box", setup)
    }
}
