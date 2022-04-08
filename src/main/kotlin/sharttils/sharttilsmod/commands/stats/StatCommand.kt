/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.commands.stats

import gg.essential.universal.wrappers.message.UMessage
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import sharttils.hylin.mojang.AshconException
import sharttils.hylin.request.HypixelAPIException
import sharttils.hylin.skyblock.Member
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.Sharttils.Companion.mc
import sharttils.sharttilsmod.commands.BaseCommand
import java.util.*

abstract class StatCommand(
    name: String,
    private val needApiKey: Boolean = true,
    private val needProfile: Boolean = true,
    aliases: List<String> = emptyList()
) :
    BaseCommand(name, aliases) {

    val key: String
        get() = Sharttils.config.apiKey

    override fun getCommandUsage(player: EntityPlayerSP): String = "/${this.commandName} [player]"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (needApiKey && key.isEmpty()) {
            printMessage("§cYou must have an API key set to use this command!")
            return
        }
        Sharttils.threadPool.submit {
            val username = if (args.isEmpty()) mc.thePlayer.name else args[0]
            printMessage("§aGetting data for ${username}...")
            val uuid = try {
                (if (args.isEmpty()) mc.thePlayer.uniqueID else Sharttils.hylinAPI.getUUIDSync(username))
            } catch (e: AshconException) {
                printMessage("§cFailed to get UUID, reason: ${e.message}")
                return@submit
            } ?: return@submit
            if (needProfile) {
                val profile = try {
                    Sharttils.hylinAPI.getLatestSkyblockProfileForMemberSync(uuid)
                } catch (e: HypixelAPIException) {
                    printMessage("§cUnable to retrieve profile information: ${e.message?.replace(Sharttils.config.apiKey, "*".repeat(Sharttils.config.apiKey.length))}")
                    return@submit
                } ?: return@submit
                displayStats(username, uuid, profile)
            } else displayStats(username, uuid)
        }
    }

    protected fun printMessage(component: IChatComponent) {
        UMessage(component).chat()
    }

    protected fun printMessage(msg: String) {
        printMessage(ChatComponentText(msg))
    }

    protected open fun displayStats(username: String, uuid: UUID, profileData: Member) {
        error("function displayStats for command ${this.commandName} is not initalized!")
    }

    protected open fun displayStats(username: String, uuid: UUID) {
        error("function displayStats for command ${this.commandName} is not initalized!")
    }
}