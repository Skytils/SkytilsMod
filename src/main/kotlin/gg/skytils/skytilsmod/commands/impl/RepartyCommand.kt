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
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.command.WrongUsageException
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import org.incendo.cloud.annotations.Flag

@Commands
object RepartyCommand {

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
    var party = ArrayList<String>(5)

    @JvmField
    var repartyFailList = ArrayList<String>(5)

    @JvmField
    var partyThread: Thread? = null

    @Command("reparty|rp")
    fun performReparty(
        @Flag("fail", aliases = ["f"])
        fail: Boolean = false,
    ) {
        if (!Utils.isOnHypixel) throw WrongUsageException("You must be on Hypixel to use this command.")
        if (fail) {
            partyThread = Thread {
                try {
                    Skytils.sendMessageQueue.add("/p ${repartyFailList.joinToString(" ")}")
                    val members = repartyFailList.joinToString(
                        separator = """
    §f
    - §e
    """.trimIndent()
                    )
                    UChat.chat(
                        """
                            #§9§m-----------------------------
                            #§aPartying:§f
                            #- §e$members
                            #§9§m-----------------------------
                        """.trimMargin("#")
                    )
                    failInviting = true
                    while (failInviting) {
                        Thread.sleep(10)
                    }
                    if (repartyFailList.size > 0) {
                        val repartyFails = repartyFailList.joinToString(
                            separator =
                            """
    
    - §c
    """.trimIndent()
                        )
                        UChat.chat(
                            """
                                #§9§m-----------------------------
                                #§aFailed to invite:§f
                                #- §c$repartyFails
                                #§9§m-----------------------------
                            """.trimMargin("#")
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
    §f
    - §e
    """.trimIndent()
                )
                UChat.chat(
                    """
§9§m-----------------------------
§aRepartying:§f
- §e$members
§9§m-----------------------------
    """.trimIndent()
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
    
    - §c
    """.trimIndent(),
                    )
                    UChat.chat(
                        """
                            #§9§m-----------------------------
                            #§aFailed to invite:§f
                            #- §c$repartyFails
                            #§9§m-----------------------------
                        """.trimMargin("#")
                    )
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        partyThread!!.start()
    }
}