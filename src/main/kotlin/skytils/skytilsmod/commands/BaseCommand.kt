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

package skytils.skytilsmod.commands

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

abstract class BaseCommand(private val name: String, private val aliases: List<String> = emptyList()) : CommandBase() {
    final override fun getCommandName(): String = name
    final override fun getCommandAliases(): List<String> = aliases
    final override fun getRequiredPermissionLevel() = 0

    override fun getCommandUsage(sender: ICommandSender): String = "/$commandName"

    abstract override fun processCommand(sender: ICommandSender, args: Array<String>)
}