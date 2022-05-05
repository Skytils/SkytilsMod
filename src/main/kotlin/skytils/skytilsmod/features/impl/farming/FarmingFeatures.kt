/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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
package skytils.skytilsmod.features.impl.farming

import gg.essential.universal.UChat
import net.minecraft.client.gui.GuiChat
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Mouse
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.failPrefix
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.Skytils.Companion.prefix
import skytils.skytilsmod.Skytils.Companion.successPrefix
import skytils.skytilsmod.core.DataFetcher
import skytils.skytilsmod.core.SoundQueue
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.stripControlCodes

class FarmingFeatures {
    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return

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
                trapperStart = System.currentTimeMillis().toDouble()
                animalFound = false
            } else if (unformatted.startsWith("Return to the Trapper soon to get a new animal to hunt!")) {
                if (trapperStart > 0 && System.currentTimeMillis() - trapperStart > 60000) { //1 minute cooldown
                    Utils.playLoudSound("note.pling", 1.0)
                    UChat.chat("$prefix §bTrapper cooldown has already expired!")
                    trapperStart = -1.0
                }
                animalFound = true
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
            TickTask(4) {
                if (solution != null) {
                    UChat.chat("$successPrefix §aThe Hiker needs: §l§2 " + solution + "§a!")
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

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        if (event.packet is S45PacketTitle) {
            val packet = event.packet
            if (packet.message != null) {
                val unformatted = packet.message.unformattedText.stripControlCodes()
                if (Skytils.config.hideFarmingRNGTitles && unformatted.contains("DROP!")) {
                    event.isCanceled = true
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!Utils.inSkyblock || !Skytils.config.trapperPing || event.phase != TickEvent.Phase.START) return
        if (trapperStart > 0 && mc.thePlayer != null) {
            if (System.currentTimeMillis() - trapperStart > 60000 && animalFound) { //1 minute cooldown
                trapperStart = -1.0
                UChat.chat("§dSkytils: Trapper cooldown has now expired!")
                for (i in 0..4) {
                    SoundQueue.addToQueue(SoundQueue.QueuedSound("note.pling", 1f, ticks = i * 4, isLoud = true))
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        trapperStart = -1.0
    }

    @SubscribeEvent
    fun onMouseInputPost(event: GuiScreenEvent.MouseInputEvent.Post) {
        if (!Utils.inSkyblock) return
        if (Mouse.getEventButton() == 0 && event.gui is GuiChat) {
            if (Skytils.config.acceptTrapperTask && acceptTrapperCommand.isNotBlank()) {
                Skytils.sendMessageQueue.add(acceptTrapperCommand)
                acceptTrapperCommand = ""
            }
        }
    }

    companion object {
        var hungerHikerItems = LinkedHashMap<String, String>()
        var trapperStart = -1.0
        var animalFound = false
        var acceptTrapperCommand = ""
    }
}