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

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import java.util.OptionalDouble

object CustomRenderLayers {
    val espLines = RenderLayer.of(
        "catlas:lines",
        VertexFormats.LINES,
        VertexFormat.DrawMode.LINES,
        1536,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.LINES_PROGRAM)
            .lineWidth(RenderPhase.LineWidth(OptionalDouble.empty()))
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .writeMaskState(RenderPhase.ALL_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
            .build(false)
    )

    val espFilledBoxLayer = RenderLayer.of(
        "catlas:debug_filled_box",
        VertexFormats.POSITION_COLOR,
        VertexFormat.DrawMode.TRIANGLE_STRIP,
        1536,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.POSITION_COLOR_PROGRAM)
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
            .build(false)
    )
}