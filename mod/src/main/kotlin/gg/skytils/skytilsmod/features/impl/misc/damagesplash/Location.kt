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
package gg.skytils.skytilsmod.features.impl.misc.damagesplash

import net.minecraft.entity.Entity
import javax.vecmath.Point3d
import javax.vecmath.Tuple3d
import kotlin.math.roundToInt

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
class Location(x: Double, y: Double, z: Double) : Point3d(x, y, z) {
    constructor(entity: Entity) : this(entity.posX, entity.posY, entity.posZ)

    fun add(x: Double, y: Double, z: Double): Location {
        this.x += x
        this.y += y
        this.z += z
        return this
    }

    fun subtract(x: Double, y: Double, z: Double): Location {
        this.x -= x
        this.y -= y
        this.z -= z
        return this
    }

    override fun clone(): Location {
        return Location(x, y, z)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other is Tuple3d) {
            return x == other.x && y == other.y && z == other.z
        } else false
    }

    override fun toString(): String {
        return "[${x.roundToInt()}, ${y.roundToInt()}, ${z.roundToInt()}]"
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}