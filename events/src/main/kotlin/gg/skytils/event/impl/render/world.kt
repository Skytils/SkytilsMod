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

package gg.skytils.event.impl.render

import gg.skytils.event.Event
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.ObjectAllocator
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

/**
 * [gg.skytils.event.mixins.render.MixinWorldRenderer.afterRender]
 */
class WorldDrawEvent(
    val allocator: ObjectAllocator,
    val tickCounter: RenderTickCounter,
    val camera: Camera,
    val gameRenderer: GameRenderer,
    val positionMatrix: Matrix4f,
    val projectionMatrix: Matrix4f,
    val entityVertexConsumers: VertexConsumerProvider.Immediate
) : Event() {
    val matrices = MatrixStack().also {
        it.multiplyPositionMatrix(positionMatrix)
    }
}