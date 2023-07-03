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
package gg.skytils.skytilsmod.features.impl.handlers

import com.mojang.authlib.exceptions.AuthenticationException
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.client
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.utils.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.minecraft.event.HoverEvent
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object MayorInfo {

    val mayorData = HashSet<Mayor>()

    var currentMayor: String? = null
    var mayorPerks = HashSet<String>()
    var isLocal = true
    var jerryMayor: Mayor? = null
    var newJerryPerks = 0L
    private var lastCheckedElectionOver = 0L
    private var lastFetchedMayorData = 0L
    private var lastSentData = 0L
    val baseURL
        get() = "https://${Skytils.domain}/api/mayor"

    private val jerryNextPerkRegex = Regex("§7Next set of perks in §e(?<h>\\d+?)h (?<m>\\d+?)m")

    init {
        TickTask(60 * 20, repeats = true) {
            if (!Utils.inSkyblock || mc.currentServerData?.serverIP?.lowercase()
                    ?.contains("alpha") == true
            ) return@TickTask
            if (currentMayor == "Jerry" && System.currentTimeMillis() > newJerryPerks) {
                if (jerryMayor != null && Skytils.config.displayJerryPerks) {
                    SoundQueue.addToQueue("random.orb", 0.8f, 1f, 1, true)
                    SoundQueue.addToQueue("random.orb", 0.8f, 1f, 2, true)
                    SoundQueue.addToQueue("random.orb", 0.8f, 1f, 3, true)
                }
                jerryMayor = null
                fetchJerryData()
            }
            if (System.currentTimeMillis() - lastFetchedMayorData > 24 * 60 * 60 * 1000 || isLocal) {
                fetchMayorData()
            }
            if (System.currentTimeMillis() - lastCheckedElectionOver > 60 * 60 * 1000) {
                val elected = TabListUtils.tabEntries.find {
                    it.second.startsWith("§r §r§fWinner: §r§a")
                }.run { this?.second?.substring(19, this.second.length - 2) } ?: currentMayor
                if (currentMayor != elected) {
                    isLocal = true
                    currentMayor = elected
                    mayorPerks.clear()
                }
                lastCheckedElectionOver = System.currentTimeMillis()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        if (mc.currentServerData?.serverIP?.lowercase()
                ?.contains("alpha") == true
        ) return
        if (event.message.unformattedText == "§eEverybody unlocks §6exclusive §eperks! §a§l[HOVER TO VIEW]") {
            val hoverEvent = event.message.chatStyle.chatHoverEvent
            if (hoverEvent?.action != HoverEvent.Action.SHOW_TEXT) return
            println(hoverEvent.value.formattedText)
            val lines = hoverEvent.value.formattedText.split("\n").takeIf {
                it.size >= 2
            } ?: return
            val color = lines[0].takeIf {
                it.stripControlCodes().startsWith("Mayor ")
            }?.substring(0, 2)
            isLocal = true
            currentMayor = lines[0].substringAfterLast(" ")
            mayorPerks.clear()
            fetchMayorData()
            val perks = hashSetOf<String>()
            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.indexOf("§") != 0 || line.lastIndexOf("§") != 2) continue
                if (color != null) {
                    if (line.startsWith("§r$color")) {
                        perks.add(line.stripControlCodes())
                    }
                } else if (!line.startsWith("§r§7") && !line.startsWith("§r§8")) {
                    perks.add(line.stripControlCodes())
                }
            }
            println("Got perks from chat: $perks")
            mayorPerks.addAll(perks)
            sendMayorData(currentMayor, mayorPerks)
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Post) {
        if (!Utils.inSkyblock) return
        if (mc.currentServerData?.serverIP?.lowercase()
                ?.contains("alpha") == true
        ) return
        if (event.container is ContainerChest) {
            val chestName = event.chestName
            if (chestName == "Calendar and Events" || ((chestName == "Mayor $currentMayor" && mayorPerks.size == 0) || (chestName.startsWith(
                    "Mayor "
                ) && (currentMayor == null || !chestName.contains(
                    currentMayor!!
                ))))
            ) {
                val item = event.slot.stack
                if (item?.item === Items.skull && (item.displayName.contains("Mayor $currentMayor") || (currentMayor == null && item.displayName.contains(
                        "Mayor "
                    ) && !item.displayName.contains("Election")))
                ) {
                    if (currentMayor == null) {
                        isLocal = true
                        currentMayor = item.displayName.stripControlCodes().substringAfter("Mayor ")
                        mayorPerks.clear()
                        fetchMayorData()
                    }
                    val color = item.displayName.substring(0, 2)
                    val lore = ItemUtil.getItemLore(item)
                    if ((lore.contains("§8Perks List") || lore.contains("§7The mayor has been elected")) && (lore.contains(
                            "§7The listed perks are"
                        ) || lore.contains("§7This perk is available to all"))
                    ) {
                        val perks = HashSet<String>()
                        for (line in lore) {
                            if (line.startsWith(color) && line.indexOf("§") == line.lastIndexOf("§")) {
                                perks.add(line.stripControlCodes())
                            }
                        }
                        println("Got Perks: $perks")
                        mayorPerks.addAll(perks)
                        sendMayorData(currentMayor, mayorPerks)
                    }
                }
            }
            if (currentMayor == "Jerry" && ((chestName == "Mayor Jerry" && event.slot.slotNumber == 11) || (chestName == "Calendar and Events" || event.slot.slotNumber == 46)) && event.slot.hasStack) {
                val lore = ItemUtil.getItemLore(event.slot.stack)
                if (!lore.contains("§9Perkpocalypse Perks:")) return
                val endingIn = lore.find { it.startsWith("§7Next set of perks in") } ?: return
                val perks =
                    lore.subList(lore.indexOf("§9Perkpocalypse Perks:"), lore.size - 1).filter { it.startsWith("§b") }
                        .map { it.stripControlCodes() }.ifEmpty { return }
                val mayor = mayorData.find {
                    it.perks.any { p ->
                        perks.contains(p.name)
                    }
                } ?: return
                val matcher = jerryNextPerkRegex.find(endingIn) ?: return
                val timeLeft =
                    matcher.groups["h"]!!.value.toInt().hours + matcher.groups["m"]!!.value.toInt().minutes
                val nextPerksNoRound = System.currentTimeMillis() + timeLeft.inWholeMilliseconds
                val nextPerks = round(nextPerksNoRound / 300000.0).toLong() * 300000L
                if (jerryMayor != mayor || abs(nextPerks - newJerryPerks) > 60000) {
                    println("Jerry has ${mayor.name}'s perks ($perks) and is ending in $newJerryPerks ($${endingIn.stripControlCodes()})")
                    sendJerryData(mayor, nextPerks)
                }
                newJerryPerks = nextPerks
                jerryMayor = mayor
            }
        }
    }

    fun fetchMayorData() = Skytils.IO.launch {
        val res = client.get(baseURL).body<Mayor>()
        if (res.name == currentMayor || currentMayor == null || mayorPerks.size == 0)
            isLocal = false
        if (!isLocal) {
            TickTask(1) {
                currentMayor = res.name
                lastFetchedMayorData = System.currentTimeMillis()
                if (currentMayor != "Jerry") jerryMayor = null
                mayorPerks.clear()
                mayorPerks.addAll(res.perks.map { it.name })
            }
        }
    }

    fun sendMayorData(mayor: String?, perks: HashSet<String>) = Skytils.IO.launch {
        if (mayor == null || perks.size == 0) return@launch
        if (lastSentData - System.currentTimeMillis() < 300000) lastSentData = System.currentTimeMillis()
        try {
            val serverId = UUID.randomUUID().toString().replace("-", "")
            val url =
                "$baseURL/new?username=${mc.session.username}&serverId=${serverId}&mayor=${mayor}${
                    perks.joinToString(separator = "") {
                        "&perks[]=${
                            URLEncoder.encode(
                                it,
                                "UTF-8"
                            )
                        }"
                    }
                }"
            val commentForDecompilers =
                "This sends a request to Mojang's auth server, used for verification. This is how we verify you are the real user without your session details. This is the exact same system Optifine uses."
            mc.sessionService.joinServer(mc.session.profile, mc.session.token, serverId)
            if (DevTools.getToggle("mayor")) println(url)
            println(client.get(url).bodyAsText())
        } catch (e: AuthenticationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun fetchJerryData() = Skytils.IO.launch {
        val res = client.get("$baseURL/jerry").body<JerrySession>()
        TickTask(1) {
            newJerryPerks = res.nextSwitch
            jerryMayor = res.mayor
        }
    }

    fun sendJerryData(mayor: Mayor?, nextSwitch: Long) = Skytils.IO.launch {
        if (mayor == null || nextSwitch <= System.currentTimeMillis()) return@launch
        try {
            val serverId = UUID.randomUUID().toString().replace("-".toRegex(), "")
            val commentForDecompilers =
                "This sends a request to Mojang's auth server, used for verification. This is how we verify you are the real user without your session details. This is the exact same system Optifine uses."
            mc.sessionService.joinServer(mc.session.profile, mc.session.token, serverId)
            val url =
                "$baseURL/jerry/perks?username=${mc.session.username}&serverId=${serverId}&nextPerks=${nextSwitch}&mayor=${mayor.name}&currTime=${System.currentTimeMillis()}"
            println(client.get(url).bodyAsText())
        } catch (e: AuthenticationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

@Serializable
class Mayor(val name: String, val role: String, val perks: List<MayorPerk>, val special: Boolean)

@Serializable
class MayorPerk(val name: String, val description: String)

@Serializable
data class JerrySession(
    val nextSwitch: Long,
    val mayor: Mayor,
    val perks: List<MayorPerk>
)