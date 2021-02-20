package skytils.skytilsmod.utils.graphics.colors;

import java.util.*;


/**
 * Represents an ordered set of colors
 *
 * Taken from Wynntils under GNU AGPL v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class ColorSet<T extends CustomColor> {
    private final T[] colors;
    private final String[] names;
    private final Map<String, T> nameMap;

    public ColorSet(T[] colors, String[] names) {
        this.colors = colors;
        this.names = new String[names.length];

        assert this.colors.length == this.names.length;

        nameMap = new HashMap<>(colors.length);
        for (int i = 0; i < colors.length; ++i) {
            String name = names[i].trim().replace(' ', '_').toUpperCase(Locale.ROOT);
            nameMap.put(name.replace("_", ""), this.colors[i]);
            this.names[i] = name;
        }
    }

    /**
     * Map text formatting colour codes to the respective CustomColor in the set
     *
     * Returns null if invalid.
     */
    public T fromCode(int code) {
        if (code < 0 || colors.length <= code) {
            return null;
        }
        return colors[code];
    }

    /**
     * Returns the colour code for a CustomColor if it is in the set, -1 if it isn't.
     */
    public int getCode(CustomColor c) {
        if (c == null) return -1;

        for (int i = 0; i < colors.length; ++i) {
            if (colors[i].equals(c)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Shorthand for `getCode(fromName(name))`
     */
    public int getCode(String name) {
        return getCode(fromName(name));
    }

    /**
     * Return the name colour in the set, or `null` if `c` isn't in the set.
     */
    public String getName(CustomColor c) {
        int code = getCode(c);
        if (code == -1) return null;
        return names[code];
    }

    /**
     * Shorthand for `getName(fromCode(code))`
     */
    public String getName(int code) {
        if (code < 0 || names.length <= code) return null;
        return names[code];
    }

    /**
     * Return the colour in the set corresponding to the name given
     */
    public T fromName(String name) {
        if (name == null) return null;

        name = name.trim().replace(' ', '_').replace("_", "").toUpperCase(Locale.ROOT);
        return nameMap.getOrDefault(name, null);
    }

    /**
     * Returns the canonical name for a common colour (All caps, space -> underscore, will be a field name).
     * Null if this isn't the name of a common colour.
     */
    public String canonicalize(String name) {
        return getName(fromName(name));
    }

    public boolean has(String name) {
        return fromName(name) != null;
    }

    public boolean has(int code) {
        return 0 <= code && code < colors.length;
    }

    public boolean has(CustomColor c) {
        return getCode(c) != -1;
    }

    public T valueOf(String name) {
        return fromName(name);
    }

    public T valueOf(int code) {
        return fromCode(code);
    }

    public T valueOf(CustomColor c) {
        return fromCode(getCode(c));  // Becomes null if not in the set, and also returns reference to color in set
    }

    /**
     * `size() - 1` is the maximum value for `fromCode`.
     *
     * @return The number of colours in the set
     */
    public int size() {
        return colors.length;
    }

    /**
     * @return the aliases for the colour with a given code
     */
//    public Set<String> getAliases(int code) {
//        return Collections.unmodifiableSet(aliases[code]);
//    }

    /**
     * @return a copy of the colours in the set (that can be modified)
     */
    public CustomColor[] copySet() {
        CustomColor[] colors = new CustomColor[this.colors.length];
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = new CustomColor(this.colors[i]);
        }
        return colors;
    }

    /**
     * @return the colours as integers
     */
    public int[] asInts() {
        int[] colors = new int[this.colors.length];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = this.colors[i].toInt();
        }
        return colors;
    }

}
