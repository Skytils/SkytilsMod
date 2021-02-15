package skytils.skytilsmod.features.impl.misc.damagesplash;

import skytils.skytilsmod.features.impl.misc.damagesplash.graphics.colors.CommonColors;

import java.util.regex.Pattern;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public enum Damage {

    CRITICAL("✧", CommonColors.CRITICAL),
    PET("♞", CommonColors.MAGENTA),
    WITHER("☠", CommonColors.BLACK),
    SKULL("✷", CommonColors.GRAY),
    TRUE("❂", CommonColors.WHITE),
    FIRE("火", CommonColors.ORANGE),
    NORMAL("", CommonColors.LIGHT_GRAY);


    private final String symbol;
    private final CommonColors color;

    Damage(String symbol, CommonColors color) {
        this.symbol = symbol;
        this.color = color;
    }

    public String getSymbol() {
        return symbol;
    }

    public CommonColors getColor() {
        return color;
    }

    public static Damage fromSymbol(String symbol) {
        for (Damage type : values()) {
            if (type.symbol.equals(symbol))
                return type;
        }
        return null;
    }

    public static Pattern compileDamagePattern() {
        StringBuilder damageTypes = new StringBuilder();

        for (Damage type : values()) {
            damageTypes.append(type.getSymbol());
        }

        return Pattern.compile("-(.*?) ([" + damageTypes.toString() + "])");
    }

}
