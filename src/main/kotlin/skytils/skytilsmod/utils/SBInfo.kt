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
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
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
import skytils.skytilsmod.events.SendChatMessageEvent
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
 * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
 *
 * @author Moulberry
 */
class SBInfo {
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
    fun onWorldChange(event: WorldEvent.Load?) {
        lastLocRaw = -1
        locraw = null
        mode = null
        joinedWorld = System.currentTimeMillis()
        lastOpenContainerName = null
    }

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        val msg = event.message
        if (msg.trim { it <= ' ' }.startsWith("/locraw") || msg.trim { it <= ' ' }.startsWith("/locraw ")) {
            lastManualLocRaw = System.currentTimeMillis()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (event.message.unformattedText.contains("{") && event.message.unformattedText.contains("}")) {
            val matcher = JSON_BRACKET_PATTERN.matcher(event.message.unformattedText)
            if (matcher.find()) {
                try {
                    val obj = Gson().fromJson(matcher.group(), JsonObject::class.java)
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
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null || !Utils.inSkyblock) return
        val currentTime = System.currentTimeMillis()
        if (locraw == null && currentTime - joinedWorld > 1000 && currentTime - lastLocRaw > 15000) {
            lastLocRaw = System.currentTimeMillis()
            Skytils.sendMessageQueue.add("/locraw")
        }
        try {
            val scoreboard = Minecraft.getMinecraft().thePlayer.worldScoreboard
            val sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1) //§707/14/20
            val scores: List<Score> = ArrayList(scoreboard.getSortedScores(sidebarObjective))
            val lines: MutableList<String> = ArrayList()
            for (i in scores.indices.reversed()) {
                val score = scores[i]
                val scoreplayerteam1 = scoreboard.getPlayersTeam(score.playerName)
                var line = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score.playerName)
                line = StringUtils.stripControlCodes(line)
                lines.add(line)
            }
            if (lines.size >= 5) {
                date = StringUtils.stripControlCodes(lines[1]).trim { it <= ' ' }
                //§74:40am
                val matcher = timePattern.matcher(lines[2])
                if (matcher.find()) {
                    time = StringUtils.stripControlCodes(matcher.group()).trim { it <= ' ' }
                    try {
                        val timeSpace = time.replace("am", " am").replace("pm", " pm")
                        val parseFormat = SimpleDateFormat("hh:mm a")
                        currentTimeDate = parseFormat.parse(timeSpace)
                    } catch (e: ParseException) {
                    }
                }
                location =
                    StringUtils.stripControlCodes(lines[3]).replace("[^A-Za-z0-9() ]".toRegex(), "").trim { it <= ' ' }
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

    companion object {
        val instance = SBInfo()
        private val timePattern = Pattern.compile(".+(am|pm)")
        private val JSON_BRACKET_PATTERN = Pattern.compile("\\{.+}")
    }
}