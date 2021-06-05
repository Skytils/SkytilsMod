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
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.commands.RepartyCommand
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import skytils.skytilsmod.features.impl.handlers.CooldownTracker
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.NumberUtil.addSuffix
import skytils.skytilsmod.utils.NumberUtil.romanToDecimal

object DungeonListener {

    val team = ConcurrentHashSet<DungeonTeammate>()
    val deads = ConcurrentHashSet<DungeonTeammate>()
    val missingPuzzles = ConcurrentHashSet<String>()

    private val partyCountPattern = Regex("§r {9}§r§b§lParty §r§f\\(([1-5])\\)§r")
    private val classPattern =
        Regex("§r§.(?<name>\\w+?) §r§f\\(§r§d(?<class>Archer|Berserk|Healer|Mage|Tank) (?<lvl>\\w+)§r§f\\)§r")
    private val missingPuzzlePattern = Regex("§r (?<puzzle>.+): §r§7\\[§r§6§l✦§r§7]§r")

    private var ticks = 0

    @SubscribeEvent
    fun onPacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inDungeons) return
        Utils.checkThreadAndQueue {
            if (event.packet is S02PacketChat) {
                if (event.packet.chatComponent.unformattedText.startsWith("Dungeon starts in 1 second.")) {
                    team.clear()
                    deads.clear()
                    missingPuzzles.clear()
                    TickTask(40) {
                        getMembers()
                    }
                } else if (event.packet.chatComponent.unformattedText.stripControlCodes()
                        .trim() == "> EXTRA STATS <"
                ) {
                    if (Skytils.config.dungeonDeathCounter) {
                        TickTask(6) {
                            mc.ingameGUI.chatGUI.printChatMessage(
                                ChatComponentText("§c☠ §lDeaths:§r ${team.sumOf { it.deaths }}\n${
                                    team.sortedByDescending { it.deaths }.filter { it.deaths > 0 }.joinToString(
                                        separator = "\n"
                                    ) {
                                        "  §c☠ ${it.playerName}:§r ${it.deaths}"
                                    }
                                }"
                                )
                            )
                        }
                    }
                    if (Skytils.config.autoRepartyOnDungeonEnd) {
                        RepartyCommand.processCommand(mc.thePlayer, emptyArray())
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!Utils.inDungeons) return
        if (event.phase != TickEvent.Phase.START) return
        if (ticks % 4 == 0) {
            val localMissingPuzzles = HashSet<String>()
            for (pi in TabListUtils.tabEntries) {
                val name = pi.getText()
                if (name.contains("✦")) {
                    val matcher = missingPuzzlePattern.find(name)
                    if (matcher != null) {
                        val puzzleName = matcher.groups["puzzle"]!!.value
                        if (puzzleName != "???") {
                            localMissingPuzzles.add(puzzleName)
                        }
                        continue
                    }
                }
            }
            missingPuzzles.clear()
            missingPuzzles.addAll(localMissingPuzzles)
            ticks = 0
        }
        if (ticks % 2 == 0) {
            if (DungeonTimer.scoreShownAt == -1L) {
                val tabEntries = TabListUtils.tabEntries
                for (teammate in team) {
                    if (tabEntries.size <= teammate.tabEntryIndex) continue
                    val entry = tabEntries[teammate.tabEntryIndex].getText()
                    if (!entry.contains(teammate.playerName)) continue
                    teammate.player = mc.theWorld.playerEntities.find {
                        it.name == teammate.playerName && it.uniqueID.version() == 4
                    }
                    teammate.dead = entry.contains("§r§f(§r§cDEAD§r§f)§r")
                    if (teammate.dead) {
                        if (deads.add(teammate)) {
                            teammate.deaths++
                            if (Skytils.config.dungeonDeathCounter) {
                                TickTask(1) {
                                    mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§bThis is §e${teammate.playerName}§b's §e${teammate.deaths.addSuffix()}§b death out of §e${team.sumOf { it.deaths }}§b total deaths."))
                                }
                            }
                        }
                    } else {
                        deads.remove(teammate)
                    }
                }
            }
        }
        ticks++
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
        println("There are $partyCount members in this party")
        for (i in 0 until partyCount!!) {
            val pos = 1 + i * 4
            val text = tabEntries[pos].getText()
            val matcher = classPattern.find(text)
            if (matcher == null) {
                println("Skipping over entry $text due to it not matching")
                continue
            }
            val name = matcher.groups["name"]!!.value
            val dungeonClass = matcher.groups["class"]!!.value
            val classLevel = matcher.groups["lvl"]!!.value.romanToDecimal()
            println("Parsed teammate $name, they are a $dungeonClass $classLevel")
            team.add(
                DungeonTeammate(
                    name,
                    DungeonClass.getClassFromName(
                        dungeonClass
                    ), classLevel,
                    pos
                )
            )
        }
        CooldownTracker.updateCooldownReduction()
    }

    class DungeonTeammate(
        val playerName: String,
        val dungeonClass: DungeonClass,
        val classLevel: Int,
        val tabEntryIndex: Int
    ) {
        var player: EntityPlayer? = null
        var dead = false
        var deaths = 0


        fun canRender() = player != null && player!!.health > 0 && !dead
    }

    enum class DungeonClass(val className: String) {
        ARCHER("Archer"),
        BERSERK("Berserk"),
        MAGE("Mage"),
        HEALER("Healer"),
        TANK("Tank");

        companion object {
            fun getClassFromName(name: String): DungeonClass {
                return values().find { it.className.lowercase() == name.lowercase() } ?: MAGE
            }
        }
    }

}