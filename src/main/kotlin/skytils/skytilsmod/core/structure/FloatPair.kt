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
package skytils.skytilsmod.core.structure

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.mutable.MutableFloat

/**
 * Taken from SkyblockAddons under MIT License
 * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author BiscuitDevelopment
 */
class FloatPair(x: Float, y: Float) {
    private val x: MutableFloat = MutableFloat(x)
    private val y: MutableFloat = MutableFloat(y)

    constructor(x: Int, y: Int) : this(
        x / sr.scaledHeight.toFloat(),
        y / sr.scaledHeight.toFloat()
    )

    fun getX(): Float {
        return x.value
    }

    fun getY(): Float {
        return y.value
    }

    fun setY(y: Float) {
        this.y.setValue(y)
    }

    fun setX(x: Float) {
        this.x.setValue(x)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other.javaClass != javaClass) {
            return false
        }
        val otherFloatPair = other as FloatPair
        return EqualsBuilder().append(getX(), otherFloatPair.getX()).append(getY(), otherFloatPair.getY()).isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(83, 11).append(getX()).append(getY()).toHashCode()
    }

    override fun toString(): String {
        return getX().toString() + "|" + getY()
    }

    fun cloneCoords(): FloatPair {
        return FloatPair(getX(), getY())
    }

    companion object {
        private val sr = ScaledResolution(Minecraft.getMinecraft())
    }

}