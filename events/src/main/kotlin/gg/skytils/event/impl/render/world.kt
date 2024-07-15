/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

import gg.skytils.event.CancellableEvent
import gg.skytils.event.Event
import net.minecraft.util.MovingObjectPosition

/**
 * [gg.skytils.event.mixins.render.MixinGameRenderer.renderWorld]
 */
class WorldDrawEvent(val partialTicks: Float) : Event()

class SelectionBoxDrawEvent(val target: MovingObjectPosition, val partialTicks: Float) : CancellableEvent()