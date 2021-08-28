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
package skytils.skytilsmod.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.events.SendChatMessageEvent
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapted from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
 * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
 *
 * @author Moulberry
 */
object SBInfo {

    private val timePattern = ".+(am|pm)".toRegex()
    private val JSON_BRACKET_PATTERN = "\\{.+}".toRegex()

    var location = ""
    var date = ""
    var time = ""
    var objective: String? = ""
    var mode: String? = ""
    var currentTimeDate: Date? = null

    @JvmField
    var lastOpenContainerName: String? = null
    private var lastManualLocRaw: Long = -1
    private var lastLocRaw: Long = -1
    private var joinedWorld: Long = -1
    private var locraw: JsonObject? = null

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!Utils.inSkyblock) return
        if (event.gui is GuiChest) {
            val chest = event.gui as GuiChest
            val container = chest.inventorySlots as ContainerChest
            val containerName = container.lowerChestInventory.displayName.unformattedText
            lastOpenContainerName = containerName
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        lastLocRaw = -1
        locraw = null
        mode = null
        joinedWorld = System.currentTimeMillis()
        lastOpenContainerName = null
    }

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        val msg = event.message
        if (msg.trim().startsWith("/locraw")) {
            lastManualLocRaw = System.currentTimeMillis()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (event.message.unformattedText.contains("{") && event.message.unformattedText.contains("}")) {
            val pattern = JSON_BRACKET_PATTERN.find(event.message.unformattedText) ?: return
            try {
                val obj = Gson().fromJson(pattern.groupValues[0], JsonObject::class.java)
                if (obj.has("server")) {
                    if (System.currentTimeMillis() - lastManualLocRaw > 5000) event.isCanceled = true
                    if (obj.has("gametype") && obj.has("mode") && obj.has("map")) {
                        locraw = obj
                        mode = locraw!!["mode"].asString
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.SendEvent) {
        if (Utils.isOnHypixel && event.packet is C01PacketChatMessage) {
            if (event.packet.message.startsWith("/locraw")) {
                lastLocRaw = System.currentTimeMillis()
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null || !Utils.inSkyblock) return
        val currentTime = System.currentTimeMillis()
        if (locraw == null && currentTime - joinedWorld > 1300 && currentTime - lastLocRaw > 15000) {
            lastLocRaw = System.currentTimeMillis()
            Skytils.sendMessageQueue.add("/locraw")
        }
        try {
            val scoreboard = mc.thePlayer.worldScoreboard
            val sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1) //§707/14/20
            val scores: List<Score> = ArrayList(scoreboard.getSortedScores(sidebarObjective))
            val lines: MutableList<String> = ArrayList()
            for (i in scores.indices.reversed()) {
                val score = scores[i]
                val scorePlayerTeamOne = scoreboard.getPlayersTeam(score.playerName)
                var line = ScorePlayerTeam.formatPlayerName(scorePlayerTeamOne, score.playerName)
                line = line.stripControlCodes()
                lines.add(line)
            }
            if (lines.size >= 5) {
                date = lines[2].stripControlCodes().trim()
                //§74:40am
                val matcher = timePattern.find(lines[3])
                if (matcher != null) {
                    time = matcher.groupValues[0].stripControlCodes().trim()
                    try {
                        val timeSpace = time.replace("am", " am").replace("pm", " pm")
                        val parseFormat = SimpleDateFormat("hh:mm a")
                        currentTimeDate = parseFormat.parse(timeSpace)
                    } catch (e: ParseException) {
                    }
                }
                for (loc in lines) {
                    if (loc.contains('⏣')) {
                        location = loc.stripControlCodes().replace("[^A-Za-z0-9() -]".toRegex(), "").trim { it <= ' ' }
                        break
                    }
                }
            }
            objective = null
            var objTextLast = false
            for (line in lines) {
                if (objTextLast) {
                    objective = line
                }
                objTextLast = line == "Objective"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

enum class SkyblockIsland(val formattedName: String, val mode: String) {
    PrivateIsland("Private Island", "dynamic"),
    SpiderDen("Spider's Den", "combat_1"),
    BlazingFortress("Blazing Fortress", "combat_2"),
    TheEnd("The End", "combat_3"),
    GoldMine("Gold Mine", "mining_1"),
    DeepCaverns("Deep Caverns", "mining_2"),
    DwarvenMines("Dwarven Mines", "mining_3"),
    CrystalHollows("Crystal Hollows", "crystal_hollows"),
    FarmingIsland("The Farming Islands", "farming_1"),
    ThePark("The Park", "foraging_1"),
    Dungeon("Dungeon", "dungeon"),
    DungeonHub("Dungeon Hub", "dungeon_hub"),
    Hub("Hub", "hub"),
    DarkAuction("Dark Auction", "dark_auction"),
    JerryWorkshop("Jerry's Workshop", "winter")
}