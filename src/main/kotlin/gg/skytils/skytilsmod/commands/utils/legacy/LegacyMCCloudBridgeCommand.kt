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

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.commands.SkytilsCommandSender
import gg.skytils.skytilsmod.commands.SkytilsCommands
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import org.incendo.cloud.Command
import org.incendo.cloud.exception.CommandExecutionException
import org.incendo.cloud.exception.CommandParseException
import java.util.concurrent.CompletionException

class LegacyMCCloudBridgeCommand(val command: Command<SkytilsCommandSender>): CommandBase() {
    override fun getCommandName(): String = command.rootComponent().name()

    override fun getCommandUsage(sender: ICommandSender): String = "/$commandName"

    override fun processCommand(
        sender: ICommandSender,
        args: Array<String>
    ) {
        val input = "${commandName}${args.joinToString(separator = " ", prefix = " ")}"
        runCatching {
            SkytilsCommands.commandExecutor().executeCommand(sender, input).join()
        }.onFailure {
            val exception = (it as? CompletionException)?.cause ?: it
            when (exception) {
                is CommandException -> {
                    throw CommandExceptionWrapper(exception, "$failPrefix §c${exception.message} ($input)")
                }
                is CommandParseException -> {
                    throw CommandExceptionWrapper(exception, "$failPrefix §cFailed to parse $input: ${exception::class.java.simpleName} ${exception.message}")
                }
                is CommandExecutionException -> {
                    exception.printStackTrace()
                    throw CommandExceptionWrapper(exception, "$failPrefix §cFailed to execute $input: ${exception.message}")
                }
                else -> {
                    exception.printStackTrace()
                    throw CommandExceptionWrapper(exception, "$failPrefix §cAn error occurred while executing the command. See logs for more details. ($input)")
                }
            }
        }
    }

    override fun getRequiredPermissionLevel(): Int = 0

    override fun getCommandAliases(): List<String> = command.rootComponent().aliases().toList()

    override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean {
        return SkytilsCommands.testPermission(sender, command.commandPermission()).allowed()
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos?
    ): List<String>? {
        val input = "${commandName}${args.joinToString(separator = " ", prefix = " ")}"
        return runCatching {
            SkytilsCommands.suggestionFactory().suggestImmediately(sender, input).list().map {
                it.suggestion()
            }
        }.onFailure {
            it.printStackTrace()
            UChat.chat("$failPrefix §cAn error occurred while tab completing the command. (${input})")
        }.getOrNull()
    }

    class CommandExceptionWrapper(override val cause: Throwable, override val message: String) : CommandException(message)
}