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

package skytils.skytilsmod.features.impl.misc.damagesplash;

import net.minecraft.entity.Entity;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class Location extends Point3d {

    public Location(double x, double y, double z) {
        super(x, y, z);
    }

    public Location(Entity entity){
        super(entity.posX, entity.posY, entity.posZ);
    }

    public Location add(double x, double y, double z){
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    public Location subtract( double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;

        return this;
    }

    public Location clone() {
        return new Location(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if(obj instanceof Tuple3d) {
            return equals((Tuple3d) obj);
        }
        return false;
    }

    public boolean equals(Tuple3d other) {
        if (other == null ) return false;
        return x == other.x && y == other.y && z == other.z;
    }

    public String toString() {
        return "[" + (int) Math.round(this.x) + ", " + (int) Math.round(this.y) + ", " + (int) Math.round(this.z) + "]";
    }
}

