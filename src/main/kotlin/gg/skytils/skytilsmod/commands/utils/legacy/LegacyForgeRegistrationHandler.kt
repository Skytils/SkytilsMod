/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.commands.utils.legacy

import gg.skytils.skytilsmod.commands.SkytilsCommandSender
import net.minecraft.command.ICommand
import net.minecraftforge.client.ClientCommandHandler
import org.incendo.cloud.Command
import org.incendo.cloud.internal.CommandRegistrationHandler

object LegacyForgeRegistrationHandler : CommandRegistrationHandler<SkytilsCommandSender> {
    private val proxiedCommands = mutableMapOf<Command<SkytilsCommandSender>, ICommand>()

    override fun registerCommand(command: Command<SkytilsCommandSender>): Boolean {
        if (proxiedCommands.containsKey(command)) return false
        proxiedCommands[command] = ClientCommandHandler.instance.registerCommand(LegacyMCCloudBridgeCommand(command))
        return true
    }
}