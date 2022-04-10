/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

import gg.essential.universal.UChat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.SyntaxErrorException
import net.minecraft.command.WrongUsageException
import net.minecraft.item.ItemArmor
import skytils.skytilsmod.Skytils.Companion.failPrefix
import skytils.skytilsmod.Skytils.Companion.prefix
import skytils.skytilsmod.Skytils.Companion.successPrefix
import skytils.skytilsmod.commands.BaseCommand
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.ArmorColor
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.colors.CustomColor

object ArmorColorCommand : BaseCommand("armorcolor", listOf("armourcolour", "armorcolour", "armourcolor")) {
    override fun getCommandUsage(player: EntityPlayerSP): String = "/armorcolor <clearall/clear/set>"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            UChat.chat("$prefix §b" + getCommandUsage(player))
            return
        }
        val subcommand = args[0].lowercase()
        if (subcommand == "clearall") {
            ArmorColor.armorColors.clear()
            PersistentSave.markDirty<ArmorColor>()
            UChat.chat("$successPrefix §aCleared all your custom armor colors!")
        } else if (subcommand == "clear") {
            if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
            val item = player.heldItem
                ?: throw WrongUsageException("You must hold a leather armor piece to use this command")
            if (item.item !is ItemArmor) throw WrongUsageException("You must hold a leather armor piece to use this command")
            if ((item.item as ItemArmor).armorMaterial != ItemArmor.ArmorMaterial.LEATHER) throw WrongUsageException("You must hold a leather armor piece to use this command")
            val extraAttributes = ItemUtil.getExtraAttributes(item)
            if (extraAttributes == null || !extraAttributes.hasKey("uuid")) throw WrongUsageException("This item does not have a UUID!")
            val uuid = extraAttributes.getString("uuid")
            if (ArmorColor.armorColors.containsKey(uuid)) {
                ArmorColor.armorColors.remove(uuid)
                PersistentSave.markDirty<ArmorColor>()
                UChat.chat("$successPrefix §aCleared the custom color for your ${item.displayName}§a!")
            } else UChat.chat("§cThat item doesn't have a custom color!")
        } else if (subcommand == "set") {
            if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
            val item = player.heldItem
                ?: throw WrongUsageException("You must hold a leather armor piece to use this command")
            if (item.item !is ItemArmor) throw WrongUsageException("You must hold a leather armor piece to use this command")
            if ((item.item as ItemArmor).armorMaterial != ItemArmor.ArmorMaterial.LEATHER) throw WrongUsageException("You must hold a leather armor piece to use this command")
            val extraAttributes = ItemUtil.getExtraAttributes(item)
            if (extraAttributes == null || !extraAttributes.hasKey("uuid")) throw WrongUsageException("This item does not have a UUID!")
            if (args.size != 2) throw WrongUsageException("You must specify a valid hex color!")
            val uuid = extraAttributes.getString("uuid")
            val color: CustomColor = try {
                Utils.customColorFromString(args[1])
            } catch (e: IllegalArgumentException) {
                throw SyntaxErrorException("$failPrefix §cUnable to get a color from inputted string.")
            }
            ArmorColor.armorColors[uuid] = color
            PersistentSave.markDirty<ArmorColor>()
            UChat.chat("$successPrefix §aSet the color of your ${item.displayName}§a to ${args[1]}!")
        } else UChat.chat(getCommandUsage(player))
    }
}