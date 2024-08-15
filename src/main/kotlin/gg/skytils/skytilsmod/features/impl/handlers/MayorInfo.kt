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
import gg.skytils.skytilsmod.Skytils.Companion.json
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.utils.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.IOException
import java.util.*
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object MayorInfo {

    val mayorData = HashSet<Mayor>()

    var currentMayor: String? = null
    var mayorPerks = HashSet<String>()
    var currentMinister: String? = null
    var ministerPerk: String? = null
    var allPerks = HashSet<String>()

    var jerryMayor: Mayor? = null
    var newJerryPerks = 0L
    private var lastCheckedElectionOver = 0L
    private var lastFetchedMayorData = 0L

    private val jerryNextPerkRegex = Regex("§7Next set of perks in §e(?<h>\\d+?)h (?<m>\\d+?)m")

    init {
        tickTimer(60 * 20, repeats = true) {
            if (!Utils.inSkyblock || mc.currentServerData?.serverIP?.lowercase()
                    ?.contains("alpha") == true
            ) return@tickTimer
            if (currentMayor == "Jerry" && System.currentTimeMillis() > newJerryPerks) {
                if (jerryMayor != null && Skytils.config.displayJerryPerks) {
                    SoundQueue.addToQueue("random.orb", 0.8f, 1f, 1, true)
                    SoundQueue.addToQueue("random.orb", 0.8f, 1f, 2, true)
                    SoundQueue.addToQueue("random.orb", 0.8f, 1f, 3, true)
                }
                jerryMayor = null
                fetchJerryData()
            }
            if (System.currentTimeMillis() - lastFetchedMayorData > 24 * 60 * 60 * 1000) {
                fetchMayorData()
            }
            if (System.currentTimeMillis() - lastCheckedElectionOver > 60 * 60 * 1000) {
                val elected = TabListUtils.tabEntries.find {
                    it.second.startsWith("§r §r§fWinner: §r§a")
                }.run { this?.second?.substring(19, this.second.length - 2) } ?: currentMayor
                if (currentMayor != elected) {
                    fetchMayorData()
                }
                lastCheckedElectionOver = System.currentTimeMillis()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        if (event.message.unformattedText == "§eEverybody unlocks §6exclusive §eperks! §a§l[HOVER TO VIEW]") {
            fetchMayorData()
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
            if (((chestName == "Mayor Jerry" && event.slot.slotNumber == 13) || (chestName == "Calendar and Events" && event.slot.slotNumber == 46)) && event.slot.hasStack) {
                val lore = ItemUtil.getItemLore(event.slot.stack)
                if (!lore.contains("§9Perkpocalypse Perks:")) return
                val endingIn = lore.asReversed().find { it.startsWith("§7Next set of perks in") } ?: return
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
        val res = client.get("https://api.hypixel.net/resources/skyblock/election").body<JsonObject>()
        val mayorObj = res["mayor"] as JsonObject
        val newMayor = json.decodeFromJsonElement<Mayor>(mayorObj)
        val newMinister = mayorObj["minister"]?.let { json.decodeFromJsonElement<Minister>(it) }
        tickTimer(1) {
            currentMayor = newMayor.name
            currentMinister = newMinister?.name
            lastFetchedMayorData = System.currentTimeMillis()
            if (currentMayor != "Jerry") jerryMayor = null
            mayorPerks.clear()
            mayorPerks.addAll(newMayor.perks.map { it.name })
            allPerks.clear()
            allPerks.addAll(mayorPerks)
            newMinister?.perk?.name?.let {
                ministerPerk = it
                allPerks.add(it)
            }
        }
    }

    fun fetchJerryData() = Skytils.IO.launch {
        val res = client.get("https://${Skytils.domain}/api/mayor/jerry").body<JerrySession>()
        tickTimer(1) {
            newJerryPerks = res.nextSwitch
            jerryMayor = res.mayor
        }
    }

    fun sendJerryData(mayor: Mayor?, nextSwitch: Long) = Skytils.IO.launch {
        if (mayor == null || nextSwitch <= System.currentTimeMillis()) return@launch
        if (!Skytils.trustClientTime) {
            println("Client's time isn't trusted, skip sending jerry data.")
            return@launch
        }
        try {
            val serverId = UUID.randomUUID().toString().replace("-".toRegex(), "")
            val commentForDecompilers =
                "This sends a request to Mojang's auth server, used for verification. This is how we verify you are the real user without your session details. This is the exact same system Optifine uses."
            mc.sessionService.joinServer(mc.session.profile, mc.session.token, serverId)
            val url =
                "https://${Skytils.domain}/api/mayor/jerry/perks?username=${mc.session.username}&serverId=${serverId}&nextPerks=${nextSwitch}&mayor=${mayor.name}&currTime=${System.currentTimeMillis()}"
            println(client.get(url).bodyAsText())
        } catch (e: AuthenticationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

@Serializable
class Mayor(val name: String, val perks: List<MayorPerk>)

@Serializable
class MayorPerk(val name: String, val description: String)

@Serializable
class Minister(val name: String, val perk: MayorPerk)

@Serializable
data class JerrySession(
    val nextSwitch: Long,
    val mayor: Mayor,
    val perks: List<MayorPerk>
)
