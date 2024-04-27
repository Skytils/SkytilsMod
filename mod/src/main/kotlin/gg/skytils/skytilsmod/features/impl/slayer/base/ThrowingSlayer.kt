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

package gg.skytils.skytilsmod.features.impl.slayer.base

import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.EntityJoinWorldEvent

/**
 * Represents a slayer which can throw a thing
 *
 * Subtype of [Slayer]
 */
abstract class ThrowingSlayer<T : EntityLiving>(entity: T, name: String, nameStart: String) : Slayer<T>(
    entity, name, nameStart,
) {
    var thrownLocation: BlockPos? = null
    var thrownEntity: EntityArmorStand? = null

    open fun entityJoinWorld(event: EntityJoinWorldEvent) {}

    abstract fun blockChange(event: BlockChangeEvent)
}