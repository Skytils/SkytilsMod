package skytils.skytilsmod.utils;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class MathUtil {
    /**
     * Returns the value of the first parameter, clamped to be within the lower and upper limits given by the second and
     * third parameters.
     */
    public static int clamp(int num, int min, int max) {
        if (num < min) {
            return min;
        } else {
            return num > max ? max : num;
        }
    }

    public static long clamp(long num, long min, long max) {
        if (num < min) {
            return min;
        } else {
            return num > max ? max : num;
        }
    }

    public static float clamp(float num, float min, float max) {
        if (num < min) {
            return min;
        } else {
            return num > max ? max : num;
        }
    }

    public static double clamp(double num, double min, double max) {
        if (num < min) {
            return min;
        } else {
            return num > max ? max : num;
        }
    }

    /**
     * returns par0 cast as an int, and no greater than Integer.MAX_VALUE-1024
     */
    public static int fastFloor(double value) {
        return (int)(value + 1024.0D) - 1024;
    }

    public static int ceil(float value)
    {
        int i = (int)value;
        return value > (float)i ? i + 1 : i;
    }

    public static int ceil(double value)
    {
        int i = (int)value;
        return value > (double)i ? i + 1 : i;
    }

    /**
     * Returns the greatest integer less than or equal to the float argument
     */
    public static int floor(float value)
    {
        int i = (int)value;
        return value < (float)i ? i - 1 : i;
    }
}
