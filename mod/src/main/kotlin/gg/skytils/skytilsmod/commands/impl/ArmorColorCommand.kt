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
package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.Skytils.successPrefix
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.ArmorColor
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.displayNameStr
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor
import net.minecraft.item.ItemStack
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands

//#if MC>=12100
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.equipment.ArmorMaterials
//#else
//$$ import net.minecraft.item.ItemArmor
//#endif

@Commands
object ArmorColorCommand {
    @Command("armorcolor clearall")
    fun clearAll() {
        ArmorColor.armorColors.clear()
        PersistentSave.markDirty<ArmorColor>()
        UChat.chat("$successPrefix §aCleared all your custom armor colors!")
    }

    @Command("armorcolor clear")
    fun clearCurrent() {
        val (item, uuid) = getCurrentArmor()

        if (ArmorColor.armorColors.containsKey(uuid)) {
            ArmorColor.armorColors.remove(uuid)
            PersistentSave.markDirty<ArmorColor>()
            UChat.chat("$successPrefix §aCleared the custom color for your ${item.displayNameStr}§a!")
        } else UChat.chat("§cThat item doesn't have a custom color!")
    }

    @Command("armorcolor set <color>")
    fun setCurrent(
        @Greedy
        @Argument("color", description = "The color to set the armor to")
        color: String
    ) {
        val (item, uuid) = getCurrentArmor()
        val customColor: CustomColor = try {
            Utils.customColorFromString(color)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("$failPrefix §cUnable to get a color from inputted string.")
        }
        ArmorColor.armorColors[uuid] = customColor
        PersistentSave.markDirty<ArmorColor>()
        UChat.chat("$successPrefix §aSet the color of your ${item.displayNameStr}§a to $color!")
    }

    private fun getCurrentArmor(): Pair<ItemStack, String> {
        if (!Utils.inSkyblock) throw IllegalArgumentException("You must be in Skyblock to use this command!")
        val item = mc.player?.mainHandStack ?: throw IllegalArgumentException("You must hold a leather armor piece to use this command")
        //#if MC>=12100
        if (item.get(DataComponentTypes.EQUIPPABLE)?.assetId != ArmorMaterials.LEATHER.assetId)
        //#else
        //$$ if ((item.item as? ItemArmor)?.armorMaterial != ItemArmor.ArmorMaterial.LEATHER)
        //#endif
            throw IllegalArgumentException("You must hold a leather armor piece to use this command")
        val extraAttributes = ItemUtil.getExtraAttributes(item)
        //#if MC>=12100
        val uuid = extraAttributes?.getString("uuid", null) ?:
            throw IllegalArgumentException("This item does not have a UUID!")
        //#else
        //$$ if (extraAttributes == null || !extraAttributes.hasKey("uuid"))
        //$$     throw IllegalArgumentException("This item does not have a UUID!")
        //$$ val uuid = extraAttributes.getString("uuid")
        //#endif

        return item to uuid
    }
}