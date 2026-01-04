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
package gg.skytils.skytilsmod.utils

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import gg.skytils.skytilsmod.utils.ItemRarity.Companion.RARITY_PATTERN
import gg.skytils.skytilsmod.utils.multiplatform.nbt
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.ProfileComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.text.Text
import java.util.Optional
import java.util.UUID
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max

object ItemUtil {
    private val PET_PATTERN = "(?:§e⭐ )?§7\\[Lvl \\d+](?: §8\\[.*])? (?<color>§[0-9a-fk-or]).+".toRegex()
    const val NBT_INTEGER = 3
    private const val NBT_STRING = 8
    private const val NBT_LIST = 9
    private const val NBT_COMPOUND = 10

    /**
     * Returns the display name of a given item
     *
     * @param item the Item to get the display name of
     * @return the display name of the item
     */
    @JvmStatic
    fun getDisplayName(item: ItemStack): String {
        var s = item.item.getName(item)
            .formattedText
        item.nbt?.let { nbt ->
            if (nbt.contains("display")) {
                nbt.getCompound("display")
                    .getOrNull()
                    ?.getString("Name")
                    ?.getOrNull()
                    ?.let { name -> s = name }
            }
        }
        return s
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock item
     *
     * @param item the Skyblock item to check
     * @return the Skyblock Item ID of this item or `null` if this isn't a valid Skyblock item
     */
    @JvmStatic
    fun getSkyBlockItemID(item: ItemStack?): String? {
        if (item == null || item.isEmpty) {
            return null
        }
        val extraAttributes = getExtraAttributes(item) ?: return null
        return if (!extraAttributes.contains("id")) {
            null
        } else extraAttributes.getString("id")
            .getOrNull()
    }

    /**
     * Returns the `ExtraAttributes` compound tag from the item's NBT data.
     *
     * @param item the item to get the tag from
     * @return the item's `ExtraAttributes` compound tag or `null` if the item doesn't have one
     */
    @JvmStatic
    fun getExtraAttributes(item: ItemStack?): NbtCompound? =
        item?.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()

    /**
     * Returns the Skyblock Item ID of a given Skyblock Extra Attributes NBT Compound
     *
     * @param extraAttributes the NBT to check
     * @return the Skyblock Item ID of this item or `null` if the Skyblock ID could not be found
     */
    @JvmStatic
    fun getSkyBlockItemID(extraAttributes: NbtCompound?): String? =
        extraAttributes?.getString("id", "")?.takeIf(String::isNotEmpty)

    /**
     * Returns a string list containing the nbt lore of an ItemStack, or
     * an empty list if this item doesn't have a lore. The returned lore
     * list is unmodifiable since it has been converted from an NBTTagList.
     *
     * @param itemStack the ItemStack to get the lore from
     * @return the lore of an ItemStack as a string list
     */
    @JvmStatic
    fun getItemLore(itemStack: ItemStack): List<String> =
        itemStack.get(DataComponentTypes.LORE)?.lines?.map { it.formattedText } ?: emptyList()

    @JvmStatic
    fun hasRightClickAbility(itemStack: ItemStack): Boolean {
        for (line in getItemLore(itemStack)) {
            val stripped = line.stripControlCodes()
            if (stripped.startsWith("Item Ability:") && stripped.endsWith("RIGHT CLICK")) return true
        }
        return false
    }

    /**
     * Returns the rarity of a given Skyblock item
     *
     * @param item the Skyblock item to check
     * @return the rarity of the item if a valid rarity is found, `null` if no rarity is found, `null` if item is `null`
     */
    fun getRarity(item: ItemStack?): ItemRarity =
        item?.let(::getItemLore)
            ?.firstNotNullOfOrNull { line ->
                RARITY_PATTERN.find(line)
                    ?.groups?.get("rarity")?.value
                    ?.stripControlCodes()
                    ?.substringAfter("SHINY ")
                    ?.replace(' ', '_')
                    ?.run(ItemRarity::valueOf)
            } ?:
            item?.displayNameStr
                ?.let(PET_PATTERN::find)
                ?.groupValues
                ?.getOrNull(1)
                ?.run(ItemRarity::byBaseColor)
            ?: ItemRarity.NONE

    fun isPet(item: ItemStack?): Boolean =
        item?.nbt?.getCompoundOrEmpty("display")
            ?.takeUnless(NbtCompound::isEmpty)
            ?.takeIf { it.contains("Lore") }
            ?.getString("Name")
            ?.getOrNull()
            ?.run(PET_PATTERN::matches) ?: false

    private fun profileComponent(uuid: UUID, propertyMap: PropertyMap): ProfileComponent =
        //#if MC>=12110
        //$$ ProfileComponent.ofStatic(GameProfile(uuid, "", propertyMap))
        //#else
        ProfileComponent(Optional.empty(), Optional.of(uuid), propertyMap)
        //#endif

    fun setSkullTexture(item: ItemStack, texture: String, SkullOwner: String): ItemStack {
        item.set(DataComponentTypes.PROFILE, profileComponent(UUID.fromString(SkullOwner),
            PropertyMap(
                //#if MC>=12110
                //$$ com.google.common.collect.LinkedHashMultimap.create()
                //#endif
            ).apply {
                put("textures", Property("Value", texture))
            }))
        /*val textureTagCompound = NbtCompound()
        textureTagCompound.putString("Value", texture)

        val textures = NbtList()
        textures.add(textureTagCompound)

        val properties = NbtCompound()
        properties.put("textures", textures)

        val skullOwner = NbtCompound()
        skullOwner.putString("Id", SkullOwner)
        skullOwner.put("Properties", properties)

        val nbtTag = NbtCompound()
        nbtTag.put("SkullOwner", skullOwner)

//        item.nbt = nbtTag*/
        return item
    }

    fun getSkullTexture(item: ItemStack?): String? =
        item?.get(DataComponentTypes.PROFILE)?.run {
            //#if MC>=1211
            //$$ gameProfile.properties
            //#else
            properties
            //#endif
        }?.get("textures")?.first()?.value

    // TODO: fix later
    fun ItemStack.setLore(lines: List<String>): ItemStack {
//        setSubNbt("display", getOrCreateSubNbt("display", true).apply {
//            put("Lore", NbtList().apply {
//                for (line in lines) add(NbtString(line))
//            })
//        })
        this.set(DataComponentTypes.LORE, LoreComponent(lines.map { Text.of(it) }))
        return this
    }

    fun getStarCount(extraAttributes: NbtCompound) =
        max(extraAttributes.getInt("upgrade_level").getOrDefault(0), extraAttributes.getInt("dungeon_item_level").getOrDefault(0))

    fun isSalvageable(stack: ItemStack): Boolean {
        val extraAttr = getExtraAttributes(stack)
        val sbId = getSkyBlockItemID(extraAttr)
        return extraAttr != null && extraAttr.contains("baseStatBoostPercentage") && getStarCount(
            extraAttr
        ) == 0 && sbId != "ICE_SPRAY_WAND"
    }
}