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
import net.minecraft.client.Minecraft
import net.minecraft.event.HoverEvent
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.utils.*
import java.io.IOException
import java.net.URLEncoder
import java.util.*

class MayorInfo {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock || event.phase != TickEvent.Phase.START) return
        if (ticks % (60*20) == 0) {
            if (System.currentTimeMillis() - lastFetchedMayorData > 24 * 60 * 60 * 1000 || isLocal) {
                fetchMayorData()
            }
            if (System.currentTimeMillis() - lastCheckedElectionOver > 60 * 60 * 1000) {
                var elected = currentMayor
                for (pi in TabListUtils.tabEntries) {
                    val name = mc.ingameGUI.tabList.getPlayerName(pi)
                    if (name.startsWith("§r §r§fWinner: §r§a")) {
                        elected = name.substring(19, name.length - 2)
                        break
                    }
                }
                if (currentMayor != null) {
                    if (currentMayor != elected) {
                        isLocal = true
                        currentMayor = elected
                        mayorPerks.clear()
                    }
                    lastCheckedElectionOver = System.currentTimeMillis()
                }
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
                if (StringUtils.stripControlCodes(lines[0]).startsWith("Mayor ")) {
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
                            perks.add(StringUtils.stripControlCodes(line))
                        }
                    } else if (!line.startsWith("§r§7") && !line.startsWith("§r§8")) {
                        perks.add(StringUtils.stripControlCodes(line))
                    }
                }
                println("Got perks from chat: $perks")
                mayorPerks.addAll(perks)
                sendMayorData(currentMayor, mayorPerks)
            }
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Post) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest) {
            val chest = event.container
            val chestName = chest.lowerChestInventory.displayName.unformattedText
            if ((chestName == "Mayor $currentMayor" && mayorPerks.size == 0) || (chestName.startsWith("Mayor ") && (currentMayor == null || isLocal))) {
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
                                perks.add(StringUtils.stripControlCodes(line))
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

    companion object {
        var currentMayor: String? = null
        var mayorPerks = HashSet<String>()
        var isLocal = true
        private val mc = Minecraft.getMinecraft()
        private var ticks = 0
        private var lastCheckedElectionOver = 0L
        private var lastFetchedMayorData = 0L
        private var lastSentData = 0L
        const val baseURL = "https://sbe-stole-skytils.design/api/mayor"
        fun fetchMayorData() {
            Thread({
                val res = APIUtil.getJSONResponse(baseURL)
                if (res.has("name") && res.has("perks")) {
                    if (res["name"].asString == currentMayor) isLocal = false
                    currentMayor = res["name"].asString
                    lastFetchedMayorData = System.currentTimeMillis()
                    mayorPerks.clear()
                    val perks = res["perks"].asJsonArray
                    for (i in 0 until perks.size()) {
                        mayorPerks.add(perks[i].asString)
                    }
                }
            }, "Skytils-FetchMayor").start()
        }

        fun sendMayorData(mayor: String?, perks: HashSet<String>) {
            if (mayor == null || perks.size == 0) return
            if (lastSentData - System.currentTimeMillis() < 300_000) lastSentData = System.currentTimeMillis()
            Thread({
                try {
                    val serverId = UUID.randomUUID().toString().replace("-".toRegex(), "")
                    val url =
                        StringBuilder(baseURL + "/new?username=" + mc.session.username + "&serverId=" + serverId + "&mayor=" + mayor)
                    for (perk in perks) {
                        url.append("&perks[]=").append(URLEncoder.encode(perk, "UTF-8"))
                    }
                    val commentForDecompilers =
                        "This sends a request to Mojang's auth server, used for verification. This is how we verify you are the real user without your session details. This is the exact same system Optifine uses."
                    mc.sessionService.joinServer(mc.session.profile, mc.session.token, serverId)
                    println(APIUtil.getJSONResponse(url.toString()))
                } catch (e: AuthenticationException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }, "Skytils-SendMayor").start()
        }
    }
}