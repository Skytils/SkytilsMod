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
package gg.skytils.skytilsmod.features.impl.farming

import gg.essential.universal.UChat
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.screen.ScreenMouseInputEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.Skytils.prefix
import gg.skytils.skytilsmod.Skytils.successPrefix
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod.core.DataFetcher
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.client.gui.GuiChat
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.util.ChatComponentText

object FarmingFeatures : EventSubscriber {
    val hungerHikerItems = LinkedHashMap<String, String>()
    var trapperCooldownExpire = -1L
    var animalFound = false
    var acceptTrapperCommand = ""

    private val targetHeightRegex =
        Regex("^The target is around (?<blocks>\\d+) blocks (?<type>above|below), at a (?<angle>\\d+) degrees angle!$")
    var targetMinY = 0
    var targetMaxY = 0

    override fun setup() {
        register(::onChat)
        register(::onReceivePacket)
        register(::onTick)
        register(::onWorldChange)
        register(::onMouseInputPost)
    }

    fun onChat(event: ChatMessageReceivedEvent) {
        if (SBInfo.mode != "farming_1") return

        val formatted = event.message.formattedText
        val unformatted = event.message.unformattedText.stripControlCodes()

        if (Skytils.config.acceptTrapperTask) {
            if (formatted.contains("§a§l[YES]")) {
                val listOfSiblings = event.message.siblings
                acceptTrapperCommand =
                    listOfSiblings.find { it.unformattedText.contains("[YES]") }?.chatStyle?.chatClickEvent?.value ?: ""
                UChat.chat("$prefix §bOpen chat then click anywhere on screen to accept the task.")
            }
        }
        if (Skytils.config.trapperPing) {
            if (unformatted.startsWith("[NPC] Trevor The Trapper: You can find your")) {
                trapperCooldownExpire = System.currentTimeMillis() +
                        if (MayorInfo.currentMayor == "Finnegan") 30000 else 60000
                animalFound = false
            } else if (unformatted.startsWith("Return to the Trapper soon to get a new animal to hunt!")) {
                if (trapperCooldownExpire > 0 && System.currentTimeMillis() > trapperCooldownExpire) {
                    Utils.playLoudSound("note.pling", 1.0)
                    UChat.chat("$prefix §bTrapper cooldown has already expired!")
                    trapperCooldownExpire = -1
                }
                animalFound = true
            }
        }
        if (Skytils.config.talbotsTheodoliteHelper) {
            if (unformatted.startsWith("[NPC] Trevor The Trapper: You can find your")) {
                targetMinY = -1
                targetMaxY = -1
            } else if (unformatted.startsWith("You are at the exact height!")) {
                targetMinY = mc.thePlayer.posY.toInt()
                targetMaxY = mc.thePlayer.posY.toInt()
            } else {
                val match = targetHeightRegex.find(unformatted)
                if (match != null) {
                    val blocks = match.groups["blocks"]!!.value.toInt()
                    val below = match.groups["type"]!!.value == "below"
                    val y = mc.thePlayer.posY.toInt() + blocks * if (below) -1 else 1
                    val filler = if (blocks == 5) 2 else 0
                    val minY = y - 2 - if (below) 0 else filler
                    val maxY = y + 2 + if (below) filler else 0

                    if (minY <= targetMaxY && maxY >= targetMinY) {
                        targetMinY = minY.coerceAtLeast(targetMinY)
                        targetMaxY = maxY.coerceAtMost(targetMaxY)
                    } else {
                        targetMinY = minY
                        targetMaxY = maxY
                    }

                    UChat.chat("§r§aThe target is at §6Y §r§e$targetMinY${if (targetMinY != targetMaxY) "-$targetMaxY" else ""} §7($blocks blocks ${match.groups["type"]!!.value}, ${match.groups["angle"]!!.value} angle)")

                    event.cancelled = true
                }
            }
        }

        if (Skytils.config.hungryHikerSolver && formatted.startsWith("§e[NPC] Hungry Hiker§f: ")) {
            if (hungerHikerItems.isEmpty()) {
                UChat.chat("$failPrefix §cSkytils did not load any solutions.")
                DataFetcher.reloadData()
                return
            }
            val solution = hungerHikerItems.getOrDefault(hungerHikerItems.keys.find { s: String ->
                unformatted.contains(s)
            }, null)
            tickTimer(4) {
                if (solution != null) {
                    UChat.chat("$successPrefix §aThe Hiker needs: §l§2 $solution§a!")
                } else {
                    if (unformatted.contains("I asked for") || unformatted.contains("The food I want")) {
                        println("Missing Hiker item: $unformatted")
                        mc.thePlayer.addChatMessage(
                            ChatComponentText(
                                String.format(
                                    "$failPrefix §cSkytils couldn't determine the Hiker item. There were %s solutions loaded.",
                                    hungerHikerItems.size
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun onReceivePacket(event: PacketReceiveEvent<*>) {
        if (!Utils.inSkyblock) return
        if (event.packet is S45PacketTitle) {
            val packet = event.packet
            if (packet.message != null) {
                val unformatted = packet.message.unformattedText.stripControlCodes()
                if (Skytils.config.hideFarmingRNGTitles && unformatted.contains("DROP!")) {
                    event.cancelled = true
                }
            }
        }
    }

    fun onTick(event: TickEvent) {
        if (!Utils.inSkyblock || !Skytils.config.trapperPing) return
        if (trapperCooldownExpire > 0 && mc.thePlayer != null) {
            if (System.currentTimeMillis() > trapperCooldownExpire && animalFound) {
                trapperCooldownExpire = -1
                UChat.chat("$prefix §bTrapper cooldown has now expired!")
                for (i in 0..4) {
                    SoundQueue.addToQueue(SoundQueue.QueuedSound("note.pling", 1f, ticks = i * 4, isLoud = true))
                }
            }
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
        trapperCooldownExpire = -1
    }

    fun onMouseInputPost(event: ScreenMouseInputEvent) {
        if (!Utils.inSkyblock) return
        if (event.button == 0 && event.screen is GuiChat) {
            if (Skytils.config.acceptTrapperTask && acceptTrapperCommand.isNotBlank()) {
                Skytils.sendMessageQueue.add(acceptTrapperCommand)
                acceptTrapperCommand = ""
            }
        }
    }
}