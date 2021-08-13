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

import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.utils.Utils

object RepartyCommand : BaseCommand("reparty", listOf("rp")) {

    @JvmField
    var gettingParty = false

    @JvmField
    var Delimiter = 0

    @JvmField
    var disbanding = false

    @JvmField
    var inviting = false

    @JvmField
    var failInviting = false

    @JvmField
    var party: MutableList<String> = ArrayList()

    @JvmField
    var repartyFailList: MutableList<String> = ArrayList()

    @JvmField
    var partyThread: Thread? = null

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (!Utils.isOnHypixel) throw WrongUsageException("You must be on Hypixel to use this command.")
        if (args.isNotEmpty() && (args[0].startsWith("fail") || args[0] == "f")) {
            partyThread = Thread {
                val player = mc.thePlayer
                try {
                    Skytils.sendMessageQueue.add("/p " + java.lang.String.join(" ", repartyFailList))
                    val members = repartyFailList.joinToString(
                        separator = """
    ${EnumChatFormatting.WHITE}
    - ${EnumChatFormatting.YELLOW}
    """.trimIndent(),
                    )
                    player.addChatMessage(
                        ChatComponentText(
                            """
${EnumChatFormatting.BLUE}${EnumChatFormatting.STRIKETHROUGH}-----------------------------
${EnumChatFormatting.GREEN}Partying:${EnumChatFormatting.WHITE}
- ${EnumChatFormatting.YELLOW}$members
${EnumChatFormatting.BLUE}${EnumChatFormatting.STRIKETHROUGH}-----------------------------
    """.trimIndent()
                        )
                    )
                    failInviting = true
                    while (failInviting) {
                        Thread.sleep(10)
                    }
                    if (repartyFailList.size > 0) {
                        val repartyFails = repartyFailList.joinToString(
                            separator =
                            """
    
    - ${EnumChatFormatting.RED}
    """.trimIndent(),
                        )
                        player.addChatMessage(
                            ChatComponentText(
                                """
${EnumChatFormatting.BLUE}${EnumChatFormatting.STRIKETHROUGH}-----------------------------
${EnumChatFormatting.GREEN}Failed to invite:${EnumChatFormatting.WHITE}
- ${EnumChatFormatting.RED}$repartyFails
${EnumChatFormatting.BLUE}${EnumChatFormatting.STRIKETHROUGH}-----------------------------
    """.trimIndent()
                            )
                        )
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            partyThread!!.start()
            return
        }
        party.clear()
        repartyFailList.clear()
        partyThread = Thread {
            val player = mc.thePlayer
            try {
                Skytils.sendMessageQueue.add("/pl")
                gettingParty = true
                while (gettingParty) {
                    Thread.sleep(10)
                }
                if (party.size == 0) return@Thread
                Skytils.sendMessageQueue.add("/p disband")
                disbanding = true
                while (disbanding) {
                    Thread.sleep(10)
                }
                val members = party.joinToString(
                    separator =
                    """
    ${EnumChatFormatting.WHITE}
    - ${EnumChatFormatting.YELLOW}
    """.trimIndent()
                )
                player.addChatMessage(
                    ChatComponentText(
                        """
${EnumChatFormatting.BLUE}${EnumChatFormatting.STRIKETHROUGH}-----------------------------
${EnumChatFormatting.GREEN}Repartying:${EnumChatFormatting.WHITE}
- ${EnumChatFormatting.YELLOW}$members
${EnumChatFormatting.BLUE}${EnumChatFormatting.STRIKETHROUGH}-----------------------------
    """.trimIndent()
                    )
                )
                repartyFailList = ArrayList(party)
                for (invitee in party) {
                    Skytils.sendMessageQueue.add("/p $invitee")
                    inviting = true
                    while (inviting) {
                        Thread.sleep(10)
                    }
                    Thread.sleep(100)
                }
                while (inviting) {
                    Thread.sleep(10)
                }
                if (repartyFailList.size > 0) {
                    val repartyFails = repartyFailList.joinToString(
                        separator =
                        """
    
    - ${EnumChatFormatting.RED}
    """.trimIndent(),
                    )
                    player.addChatMessage(
                        ChatComponentText(
                            """
${EnumChatFormatting.BLUE}${EnumChatFormatting.STRIKETHROUGH}-----------------------------
${EnumChatFormatting.GREEN}Failed to invite:${EnumChatFormatting.WHITE}
- ${EnumChatFormatting.RED}$repartyFails
${EnumChatFormatting.BLUE}${EnumChatFormatting.STRIKETHROUGH}-----------------------------
    """.trimIndent()
                        )
                    )
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        partyThread!!.start()
    }
}