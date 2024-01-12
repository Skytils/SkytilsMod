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
package gg.skytils.skytilsmod.commands.stats

import gg.essential.universal.wrappers.message.UMessage
import gg.skytils.hypixel.types.skyblock.Member
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.commands.BaseCommand
import gg.skytils.skytilsmod.core.API
import gg.skytils.skytilsmod.utils.MojangUtil
import kotlinx.coroutines.launch
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import skytils.hylin.extension.nonDashedString
import java.lang.Exception
import java.util.*

abstract class StatCommand(
    name: String,
    private val needProfile: Boolean = true,
    aliases: List<String> = emptyList()
) :
    BaseCommand(name, aliases) {

    override fun getCommandUsage(player: EntityPlayerSP): String = "/${this.commandName} [player]"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        Skytils.IO.launch {
            val username = if (args.isEmpty()) mc.thePlayer.name else args[0]
            printMessage("§aGetting data for ${username}...")
            val uuid = try {
                (if (args.isEmpty()) mc.thePlayer.uniqueID else MojangUtil.getUUIDFromUsername(username))
            } catch (e: MojangUtil.MojangException) {
                printMessage("$failPrefix §cFailed to get UUID, reason: ${e.message}")
                return@launch
            } ?: return@launch
            if (needProfile) {
                val profile = try {
                    API.getSelectedSkyblockProfile(uuid)?.members?.get(uuid.nonDashedString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    printMessage(
                        "$failPrefix §cUnable to retrieve profile information: ${
                            e.message
                        }"
                    )
                    return@launch
                } ?: return@launch
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

    protected open suspend fun displayStats(username: String, uuid: UUID) {
        error("function displayStats for command ${this.commandName} is not initalized!")
    }
}