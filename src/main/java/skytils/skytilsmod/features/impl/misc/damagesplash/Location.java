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

