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
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.commands.BaseCommand
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.CooldownTracker
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.WrongUsageException

object TrackCooldownCommand : BaseCommand("trackcooldown", listOf("cooldowntracker")) {
    override fun getCommandUsage(player: EntityPlayerSP): String = "/trackcooldown <cooldown> <ability>"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (!Skytils.config.itemCooldownDisplay) return UChat.chat("$failPrefix §cYou must turn on Item Cooldown Display to use this command!")
        if (args.size < 2) UChat.chat("$prefix ${getCommandUsage(player)}")
        val seconds = args[0].toDoubleOrNull() ?: throw WrongUsageException("You must specify a valid number")
        val ability = args.drop(1).joinToString(" ")
        if (ability.isBlank()) throw WrongUsageException("You must specify valid arguments.")
        if (CooldownTracker.itemCooldowns[ability] == seconds) {
            CooldownTracker.itemCooldowns.remove(ability)
            PersistentSave.markDirty<CooldownTracker>()
            UChat.chat("$successPrefix Removed the cooldown for $ability.")
        } else {
            CooldownTracker.itemCooldowns[ability] = seconds
            PersistentSave.markDirty<CooldownTracker>()
            UChat.chat("$successPrefix Set the cooldown for $ability to $seconds seconds.")
        }
    }
}