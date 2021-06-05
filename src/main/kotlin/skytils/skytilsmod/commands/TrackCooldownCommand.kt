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
package skytils.skytilsmod.commands

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.BlockAbility
import skytils.skytilsmod.features.impl.handlers.CooldownTracker
import skytils.skytilsmod.utils.ItemUtil

object TrackCooldownCommand : CommandBase() {
    override fun getCommandName(): String {
        return "trackcooldown"
    }

    override fun getCommandAliases(): List<String> {
        return listOf("cooldowntracker")
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/trackcooldown <cooldown> <ability>"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        return emptyList()
    }


    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.size <= 2) throw WrongUsageException("You must specify valid arguments.")
        val seconds = args[0].toDoubleOrNull() ?: throw WrongUsageException("You must specify a valid number")
        val ability = args.drop(1).joinToString(" ")
        if (ability.isBlank()) throw WrongUsageException("You must specify valid arguments.")
        if (CooldownTracker.itemCooldowns[ability] == seconds) {
            CooldownTracker.itemCooldowns.remove(ability)
            PersistentSave.markDirty(CooldownTracker::class)
            sender.addChatMessage(ChatComponentText("Removed the cooldown for $ability."))
        } else {
            CooldownTracker.itemCooldowns[ability] = seconds
            PersistentSave.markDirty(CooldownTracker::class)
            sender.addChatMessage(ChatComponentText("Set the cooldown for $ability to $seconds seconds."))
        }
    }
}