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

package skytils.skytilsmod.core.structure;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.mutable.MutableFloat;

/**
 * Taken from SkyblockAddons under MIT License
 * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author BiscuitDevelopment
 */
public class FloatPair {
    private static final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

    private final MutableFloat x;
    private final MutableFloat y;

    public FloatPair(int x, int y) {
        this(x / (float) sr.getScaledHeight(), y / (float) sr.getScaledHeight());
    }

    public FloatPair(float x, float y) {
        this.x = new MutableFloat(x);
        this.y = new MutableFloat(y);
    }

    public float getX() {
        return x.getValue();
    }

    public float getY() {
        return y.getValue();
    }

    public void setY(float y) {
        this.y.setValue(y);
    }

    public void setX(float x) {
        this.x.setValue(x);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) { return false; }
        if (other == this) { return true; }
        if (other.getClass() != getClass()) {
            return false;
        }
        FloatPair otherFloatPair = (FloatPair)other;
        return new EqualsBuilder().append(getX(), otherFloatPair.getX()).append(getY(), otherFloatPair.getY()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(83, 11).append(getX()).append(getY()).toHashCode();
    }

    @Override
    public String toString() {
        return getX()+"|"+getY();
    }

    public FloatPair cloneCoords() {
        return new FloatPair(getX(), getY());
    }
}

