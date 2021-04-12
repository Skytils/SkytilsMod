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

package skytils.skytilsmod.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemUtil {
    private static final Pattern RARITY_PATTERN = Pattern.compile("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>[A-Z]+)");
    private static final Pattern PET_PATTERN = Pattern.compile("§7\\[Lvl \\d+\\] (?<color>§[0-9a-fk-or]).+");
    public static final int NBT_INTEGER = 3;
    public static final int NBT_STRING = 8;
    public static final int NBT_LIST = 9;
    public static final int NBT_COMPOUND = 10;

    /**
     * Returns the display name of a given item
     * @author Mojang
     * @param item the Item to get the display name of
     * @return the display name of the item
     */
    public static String getDisplayName(ItemStack item) {
        String s = item.getItem().getItemStackDisplayName(item);

        if (item.getTagCompound() != null && item.getTagCompound().hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = item.getTagCompound().getCompoundTag("display");

            if (nbttagcompound.hasKey("Name", 8)) {
                s = nbttagcompound.getString("Name");
            }
        }

        return s;
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock item
     *
     * @author BiscuitDevelopment
     * @param item the Skyblock item to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock item
     */
    public static String getSkyBlockItemID(ItemStack item) {
        if (item == null) {
            return null;
        }

        NBTTagCompound extraAttributes = getExtraAttributes(item);
        if (extraAttributes == null) {
            return null;
        }

        if (!extraAttributes.hasKey("id", ItemUtil.NBT_STRING)) {
            return null;
        }

        return extraAttributes.getString("id");
    }

    /**
     * Returns the {@code ExtraAttributes} compound tag from the item's NBT data.
     *
     * @author BiscuitDevelopment
     * @param item the item to get the tag from
     * @return the item's {@code ExtraAttributes} compound tag or {@code null} if the item doesn't have one
     */
    public static NBTTagCompound getExtraAttributes(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return null;
        }

        return item.getSubCompound("ExtraAttributes", false);
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock Extra Attributes NBT Compound
     *
     * @author BiscuitDevelopment
     * @param extraAttributes the NBT to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock NBT
     */
    public static String getSkyBlockItemID(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {
            String itemId = extraAttributes.getString("id");

            if (!itemId.equals("")) {
                return itemId;
            }
        }

        return null;
    }


    /**
     * Returns a string list containing the nbt lore of an ItemStack, or
     * an empty list if this item doesn't have a lore. The returned lore
     * list is unmodifiable since it has been converted from an NBTTagList.
     *
     * @author BiscuitDevelopment
     * @param itemStack the ItemStack to get the lore from
     * @return the lore of an ItemStack as a string list
     */
    public static List<String> getItemLore(ItemStack itemStack) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("display", ItemUtil.NBT_COMPOUND)) {
            NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");

            if (display.hasKey("Lore", ItemUtil.NBT_LIST)) {
                NBTTagList lore = display.getTagList("Lore", ItemUtil.NBT_STRING);

                List<String> loreAsList = new ArrayList<>();
                for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
                    loreAsList.add(lore.getStringTagAt(lineNumber));
                }

                return Collections.unmodifiableList(loreAsList);
            }
        }

        return Collections.emptyList();
    }

    public static boolean hasRightClickAbility(ItemStack itemStack) {
        for (String line : ItemUtil.getItemLore(itemStack)) {
            String stripped = StringUtils.stripControlCodes(line);
            if (stripped.startsWith("Item Ability:") && stripped.endsWith("RIGHT CLICK")) return true;
        }
        return false;
    }

    /**
     * Returns the rarity of a given Skyblock item
     * Modified
     * @author BiscuitDevelopment
     * @param item the Skyblock item to check
     * @return the rarity of the item if a valid rarity is found, {@code INVALID} if no rarity is found, {@code null} if item is {@code null}
     */
    public static ItemRarity getRarity(ItemStack item) {
        if (item == null || !item.hasTagCompound())  {
            return null;
        }

        NBTTagCompound display = item.getSubCompound("display", false);

        if (display == null || !display.hasKey("Lore")) {
            return null;
        }

        NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);
        String name = display.getString("Name");

        // Determine the item's rarity
        for (int i = 0; i < lore.tagCount(); i++) {
            String currentLine = lore.getStringTagAt(i);

            Matcher rarityMatcher = RARITY_PATTERN.matcher(currentLine);
            Matcher petRarityMatcher = PET_PATTERN.matcher(name);
            if (rarityMatcher.find()) {
                String rarity = rarityMatcher.group("rarity");

                for (ItemRarity itemRarity : ItemRarity.values()) {
                    if (rarity.startsWith(itemRarity.getName())) {
                        return itemRarity;
                    }
                }
            } else if (petRarityMatcher.find()) {
                String color = petRarityMatcher.group("color");

                return ItemRarity.byBaseColor(color);
            }
        }

        // If the item doesn't have a valid rarity, return null
        return null;
    }

    public static boolean isPet(ItemStack item) {
        if (item == null || !item.hasTagCompound())  {
            return false;
        }

        NBTTagCompound display = item.getSubCompound("display", false);

        if (display == null || !display.hasKey("Lore")) {
            return false;
        }

        String name = display.getString("Name");

        Matcher petRarityMatcher = PET_PATTERN.matcher(name);

        if (petRarityMatcher.find()) {
            return true;
        } else {
            return false;
        }
    }

}
