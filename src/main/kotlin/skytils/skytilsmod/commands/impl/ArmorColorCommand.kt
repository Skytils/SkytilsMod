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
package skytils.skytilsmod.commands.impl

import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DisplayName
import gg.essential.api.commands.SubCommand
import gg.essential.universal.wrappers.message.UTextComponent
import net.minecraft.command.SyntaxErrorException
import net.minecraft.command.WrongUsageException
import net.minecraft.item.ItemArmor
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.ArmorColor
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.colors.CustomColor

object ArmorColorCommand : Command("armorcolor") {

    override val commandAliases: Set<Alias> = setOf(Alias("armourcolour"), Alias("armorcolour"), Alias("armourcolor"))

    @SubCommand("clearall")
    fun clearAll() {
        handleClearAll()
    }

    fun handleClearAll() {
        ArmorColor.armorColors.clear()
        PersistentSave.markDirty<ArmorColor>()
        EssentialAPI.getMinecraftUtil().sendMessage(UTextComponent("§aCleared all your custom armor colors!"))
    }

    @SubCommand("clear")
    fun clear() {
        handleClear()
    }

    fun handleClear() {
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        val item = mc.thePlayer.heldItem
            ?: throw WrongUsageException("You must hold a leather armor piece to use this command")
        if (item.item !is ItemArmor) throw WrongUsageException("You must hold a leather armor piece to use this command")
        if ((item.item as ItemArmor).armorMaterial != ItemArmor.ArmorMaterial.LEATHER) throw WrongUsageException("You must hold a leather armor piece to use this command")
        val extraAttributes = ItemUtil.getExtraAttributes(item)
        if (extraAttributes == null || !extraAttributes.hasKey("uuid")) throw WrongUsageException("This item does not have a UUID!")
        val uuid = extraAttributes.getString("uuid")
        if (ArmorColor.armorColors.containsKey(uuid)) {
            ArmorColor.armorColors.remove(uuid)
            PersistentSave.markDirty<ArmorColor>()
            EssentialAPI.getMinecraftUtil()
                .sendMessage(UTextComponent("§aCleared the custom color for your " + item.displayName + "§a!"))
        } else EssentialAPI.getMinecraftUtil().sendMessage(UTextComponent("§cThat item doesn't have a custom color!"))
    }

    @SubCommand("set")
    fun set(@DisplayName("hex color") hex: String) {
        handleSet(hex)
    }

    fun handleSet(hex: String) {
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        val item = mc.thePlayer.heldItem
            ?: throw WrongUsageException("You must hold a leather armor piece to use this command")
        if (item.item !is ItemArmor) throw WrongUsageException("You must hold a leather armor piece to use this command")
        if ((item.item as ItemArmor).armorMaterial != ItemArmor.ArmorMaterial.LEATHER) throw WrongUsageException("You must hold a leather armor piece to use this command")
        val extraAttributes = ItemUtil.getExtraAttributes(item)
        if (extraAttributes == null || !extraAttributes.hasKey("uuid")) throw WrongUsageException("This item does not have a UUID!")
        val uuid = extraAttributes.getString("uuid")
        val color: CustomColor = try {
            Utils.customColorFromString(hex)
        } catch (e: IllegalArgumentException) {
            throw SyntaxErrorException("Unable to get a color from inputted string.")
        }
        ArmorColor.armorColors[uuid] = color
        PersistentSave.markDirty<ArmorColor>()
        EssentialAPI.getMinecraftUtil()
            .sendMessage(UTextComponent("§aSet the color of your " + item.displayName + "§a to " + hex + "!"))
    }
}