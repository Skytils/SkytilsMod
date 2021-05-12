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

package skytils.skytilsmod.listeners

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.utils.TabListUtils
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.getText

object DungeonListener {

    val team = HashSet<DungeonTeammate>()
    private val partyCountPattern = Regex("§r {9}§r§b§lParty §r§f\\(([1-5])\\)§r")
    private val classPattern =
        Regex("§r§.(?<name>\\w+?) §r§f\\(§r§d(?<class>Archer|Berserk|Healer|Mage|Tank) (?<lvl>\\w+)§r§f\\)§r")

    @SubscribeEvent
    fun onEvent(event: Event) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.boxedTanks && !Skytils.config.showTankRadius && !Skytils.config.boxedProtectedTeammates) return
        when (event) {
            is PacketEvent.ReceiveEvent -> {
                if (event.packet is S02PacketChat) {
                    if (event.packet.chatComponent.formattedText.startsWith("§r§aDungeon starts in 1 second.")) {
                        team.clear()
                        TickTask(40) {
                            getMembers()
                        }
                    }
                }
            }
        }
    }

    private fun getMembers() {
        if (team.isNotEmpty() || !Utils.inDungeons) return
        val tabEntries = TabListUtils.tabEntries

        if (tabEntries.isEmpty() || !tabEntries[0].getText().contains("§r§b§lParty §r§f(")) {
            TickTask(5) {
                getMembers()
            }
            return
        }

        val partyCount = partyCountPattern.find(tabEntries[0].getText())?.groupValues?.get(1)?.toInt()

        for (i in 0 until partyCount!!) {
            val pos = 1 + i * 4
            val text = tabEntries[pos].getText()
            val matcher = classPattern.find(text)
            if (matcher == null) {
                println("Skipping over entry $text due to it not matching")
                continue
            }
            team.add(
                DungeonTeammate(
                    matcher.groups["name"]!!.value,
                    DungeonClass.getClassFromName(
                        matcher.groups["class"]!!.value
                    ), matcher.groups["lvl"]!!.value
                )
            )
        }
    }

    class DungeonTeammate(val playerName: String, val dungeonClass: DungeonClass, val classLevel: String) {
        val player: EntityPlayer?
            get() {
                return mc.theWorld.playerEntities.find {
                    it.name == playerName && it.uniqueID.version() == 4
                }
            }
    }

    sealed class DungeonClass {
        object ARCHER : DungeonClass()
        object BERSERK : DungeonClass()
        object MAGE : DungeonClass()
        object HEALER : DungeonClass()
        object TANK : DungeonClass()

        companion object {
            fun getClassFromName(name: String): DungeonClass {
                return when (name.lowercase()) {
                    "archer" -> ARCHER
                    "berserk" -> BERSERK
                    "mage" -> MAGE
                    "healer" -> HEALER
                    "tank" -> TANK
                    else -> MAGE
                }
            }
        }
    }

}