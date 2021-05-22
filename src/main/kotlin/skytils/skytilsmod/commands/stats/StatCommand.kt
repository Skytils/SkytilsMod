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
package skytils.skytilsmod.commands.stats

import com.google.gson.JsonObject
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.utils.APIUtil

abstract class StatCommand(private val needApiKey: Boolean = true, private val needProfile: Boolean = true) :
    CommandBase() {

    val key: String
        get() = Skytils.config.apiKey

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/${this.commandName} [player]"
    }

    final override fun getRequiredPermissionLevel(): Int {
        return 0
    }


    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (needApiKey && key.isEmpty()) {
            printMessage("§cYou must have an API key set to use this command!")
            return
        }
        Skytils.threadPool.submit {
            val username = if (args.isEmpty()) mc.thePlayer.name else args[0]
            printMessage("§aGetting data for ${username}...")
            val uuid =
                if (args.isEmpty()) mc.thePlayer.uniqueID.toString().replace("-", "") else APIUtil.getUUID(username)
            if (uuid.isNullOrEmpty()) return@submit
            if (needProfile) {
                val latestProfile: String = APIUtil.getLatestProfileID(uuid, key) ?: return@submit

                val profileResponse: JsonObject =
                    APIUtil.getJSONResponse("https://api.hypixel.net/skyblock/profile?profile=$latestProfile&key=$key")
                if (!profileResponse["success"].asBoolean) {
                    printMessage("§cUnable to retrieve profile information: ${profileResponse["cause"].asString}")
                    return@submit
                }
                displayStats(username, uuid, profileResponse)
            } else displayStats(username, uuid)
        }
    }

    protected fun printMessage(component: IChatComponent) {
        mc.ingameGUI.chatGUI.printChatMessage(component)
    }

    protected fun printMessage(msg: String) {
        printMessage(ChatComponentText(msg))
    }

    abstract fun displayStats(username: String, uuid: String, profileData: JsonObject = JsonObject())
}