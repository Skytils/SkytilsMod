package skytils.skytilsmod.utils;

import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

public enum ItemRarity {
    COMMON("COMMON", EnumChatFormatting.WHITE, new Color(255,255,255)),
    UNCOMMON("UNCOMMON", EnumChatFormatting.GREEN, new Color(85,255,85)),
    RARE("RARE", EnumChatFormatting.BLUE, new Color(85,85,255)),
    EPIC("EPIC", EnumChatFormatting.DARK_PURPLE, new Color(190,0,190)),
    LEGENDARY("LEGENDARY", EnumChatFormatting.GOLD, new Color(255,170,0)),
    MYTHIC("MYTHIC", EnumChatFormatting.LIGHT_PURPLE, new Color(255,85,255)),
    SUPREME("SUPREME", EnumChatFormatting.DARK_RED, new Color(170,0,0)),
    SPECIAL("SPECIAL", EnumChatFormatting.RED, new Color(255,85,85)),
    VERY_SPECIAL("VERY SPECIAL", EnumChatFormatting.RED, new Color(170,0,0));

    private static final ItemRarity[] VALUES = Arrays.stream(values()).sorted(Comparator.comparingInt(ItemRarity::ordinal)).toArray(size -> new ItemRarity[size]);
    private final String name;
    private final EnumChatFormatting baseColor;
    private final Color color;

    static {
        for (ItemRarity rarity : values())
        {
            VALUES[rarity.ordinal()] = rarity;
        }
    }

    private ItemRarity(String name, EnumChatFormatting baseColor, Color color) {
        this.name = name;
        this.baseColor = baseColor;
        this.color = color;
    }

    public String getName() {
        return this.name;
    }

    public EnumChatFormatting getBaseColor() {
        return this.baseColor;
    }

    public Color getColor() {
        return this.color;
    }

    public static ItemRarity byBaseColor(String color) {
        for (ItemRarity rarity : values())
        {
            if (rarity.baseColor.toString().equals(color))
            {
                return rarity;
            }
        }
        return null;
    }

    public ItemRarity getNextRarity() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }
}
