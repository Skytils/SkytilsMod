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
package skytils.skytilsmod.features.impl.handlers

import com.mojang.authlib.exceptions.AuthenticationException
import net.minecraft.event.HoverEvent
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.SoundQueue
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.utils.*
import java.io.IOException
import java.net.URLEncoder
import java.time.ZonedDateTime
import java.util.*
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

object MayorInfo {

    val mayorData = HashSet<Mayor>()

    var currentMayor: String? = null
    var mayorPerks = HashSet<String>()
    var isLocal = true
    var jerryMayor: Mayor? = null
    var newJerryPerks = 0L
    private var ticks = 0
    private var lastCheckedElectionOver = 0L
    private var lastFetchedMayorData = 0L
    private var lastSentData = 0L
    const val baseURL = "https://sbe-stole-skytils.design/api/mayor"

    private val jerryNextPerkRegex = Regex("§7Next set of perks in §e(?<h>\\d+?)h (?<m>\\d+?)m")

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock || event.phase != TickEvent.Phase.START) return
        if (ticks % (60 * 20) == 0) {
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
                var elected = currentMayor
                for (pi in TabListUtils.tabEntries) {
                    val name = pi.getText()
                    if (name.startsWith("§r §r§fWinner: §r§a")) {
                        elected = name.substring(19, name.length - 2)
                        break
                    }
                }
                if (currentMayor != elected) {
                    isLocal = true
                    currentMayor = elected
                    mayorPerks.clear()
                }
                lastCheckedElectionOver = System.currentTimeMillis()
            }
            ticks = 0
        }
        ticks++
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        if (event.message.unformattedText == "§eEverybody unlocks §6exclusive §eperks! §a§l[HOVER TO VIEW]" && event.message.siblings.size == 1) {
            val hoverEvent = event.message.siblings[0].chatStyle.chatHoverEvent
            if (hoverEvent != null && hoverEvent.action == HoverEvent.Action.SHOW_TEXT) {
                val value = hoverEvent.value
                println(value.formattedText)
                val lines = value.formattedText.split("\n".toRegex()).toTypedArray()
                if (lines.size < 2) return
                var color = ""
                if (lines[0].stripControlCodes().startsWith("Mayor ")) {
                    color = lines[0].substring(0, 2)
                }
                isLocal = true
                currentMayor = lines[0].substring(lines[0].lastIndexOf(" ") + 1)
                mayorPerks.clear()
                fetchMayorData()
                val perks = HashSet<String>()
                for (i in 1 until lines.size) {
                    val line = lines[i]
                    if (!line.contains("§") || line.indexOf("§") != 0 || line.lastIndexOf("§") != 2) continue
                    if (color.isNotEmpty()) {
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
    }

    @OptIn(ExperimentalTime::class)
    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Post) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest) {
            val chest = event.container
            val chestName = chest.lowerChestInventory.displayName.unformattedText
            if (currentMayor == "Jerry" && chestName == "Mayor Jerry" && event.slot.slotNumber == 11 && event.slot.hasStack && jerryMayor == null) {
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
                    Duration.hours(matcher.groups["h"]!!.value.toInt()) + Duration.minutes(matcher.groups["m"]!!.value.toInt())
                newJerryPerks =
                    (ZonedDateTime.now().withSecond(0).withNano(0)
                        .toEpochSecond() * 1000) + timeLeft.inWholeMilliseconds
                jerryMayor = mayor
                println("Jerry has ${mayor.name}'s perks ($perks) and is ending in $newJerryPerks ($${endingIn.stripControlCodes()})")
                sendJerryData(mayor, newJerryPerks)
            } else if ((chestName == "Mayor $currentMayor" && mayorPerks.size == 0) || (chestName.startsWith("Mayor ") && (currentMayor == null || !chestName.contains(
                    currentMayor!!
                )))
            ) {
                val slot = event.slot
                val item = slot.stack
                if (item != null && item.item === Items.skull && (item.displayName.contains("Mayor $currentMayor") || currentMayor == null && item.displayName.contains(
                        "Mayor "
                    ) && !item.displayName.contains("Election"))
                ) {
                    if (currentMayor == null) {
                        isLocal = true
                        currentMayor = chestName.substring(6)
                        mayorPerks.clear()
                        fetchMayorData()
                    }
                    val color = item.displayName.substring(0, 2)
                    val lore = ItemUtil.getItemLore(item)
                    if (lore.contains("§8Perks List") && lore.contains("§7The listed perks are")) {
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
        }
    }

    fun fetchMayorData() {
        thread(name = "Skytils-FetchMayor") {
            val res = APIUtil.getJSONResponse(baseURL)
            if (res.has("name") && res.has("perks")) {
                if (res["name"].asString == currentMayor) isLocal = false
                if (!isLocal) {
                    currentMayor = res["name"].asString
                    lastFetchedMayorData = System.currentTimeMillis()
                    mayorPerks.clear()
                    val perks = res["perks"].asJsonArray
                    for (i in 0 until perks.size()) {
                        mayorPerks.add(perks[i].asJsonObject.get("name").asString)
                    }
                }
            }
        }
    }

    fun sendMayorData(mayor: String?, perks: HashSet<String>) {
        if (mayor == null || perks.size == 0) return
        if (lastSentData - System.currentTimeMillis() < 300000) lastSentData = System.currentTimeMillis()
        thread(name = "Skytils-SendMayor") {
            try {
                val serverId = UUID.randomUUID().toString().replace("-".toRegex(), "")
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
                println(APIUtil.getJSONResponse(url))
            } catch (e: AuthenticationException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun fetchJerryData() {
        thread(name = "Skytils-FetchJerry") {
            val res = APIUtil.getJSONResponse("$baseURL/jerry")
            if (res.has("nextSwitch") && res.has("mayor") && res.has("perks")) {
                newJerryPerks = res["nextSwitch"].asLong
                jerryMayor = mayorData.find { it.name == res["mayor"].asJsonObject["name"].asString }
            }
        }
    }

    fun sendJerryData(mayor: Mayor?, nextSwitch: Long) {
        if (nextSwitch <= 0 || mayor == null) return
        thread(name = "Skytils-SendJerry") {
            try {
                val serverId = UUID.randomUUID().toString().replace("-".toRegex(), "")
                val url =
                    StringBuilder("$baseURL/jerry/perks?username=${mc.session.username}&serverId=${serverId}&nextPerks=${nextSwitch}&mayor=${mayor.name}")
                val commentForDecompilers =
                    "This sends a request to Mojang's auth server, used for verification. This is how we verify you are the real user without your session details. This is the exact same system Optifine uses."
                mc.sessionService.joinServer(mc.session.profile, mc.session.token, serverId)
                println(APIUtil.getJSONResponse(url.toString()))
            } catch (e: AuthenticationException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

class Mayor(val name: String, val role: String, val perks: List<MayorPerk>, val special: Boolean)
class MayorPerk(val name: String, val description: String)