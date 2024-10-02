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
package gg.skytils.skytilsmod.features.impl.dungeons.solvers

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.DataFetcher
import gg.skytils.skytilsmod.events.impl.skyblock.DungeonEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.funny.Funny
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.*
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import kotlin.math.floor

object TriviaSolver {
    private val questionStartRegex = Regex("§r§f {32}§r§6§lQuestion #\\d§r")
    private val answerRegex = Regex("§r§6 (?<type>[ⓐⓑⓒ]) §a(?<answer>[\\w ]+)§r")

    val triviaSolutions = hashMapOf<String, List<String>>()

    private var lines = mutableListOf<String>()
    private var trackLines = false
    private var fullQuestion: String? = null
    private var correctAnswers = mutableListOf<String>()
    private var correctAnswer: String? = null

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type == 2.toByte() || !Skytils.config.triviaSolver || !Utils.inDungeons || !DungeonListener.missingPuzzles.contains("Quiz")) return
        val formatted = event.message.formattedText

        if (formatted == "§r§4[STATUE] Oruo the Omniscient§r§f: §r§fI am §r§4Oruo the Omniscient§r§f. I have lived many lives. I have learned all there is to know.§r" && triviaSolutions.size == 0) {
            UChat.chat("$failPrefix §cSkytils failed to load solutions for Quiz.")
            DataFetcher.reloadData()
        }

        if (questionStartRegex.matches(formatted)) {
            reset(trackLines = true)
        }

        if (trackLines) {
            lines.add(formatted)

            answerRegex.find(formatted)?.destructured?.let { (type, answer) ->
                if (type == "ⓐ") {
                    fullQuestion = lines.subList(1, lines.size - 2).joinToString(" ") { it.stripControlCodes().trim() }

                    if (fullQuestion == "What SkyBlock year is it?") {
                        val currentTime =
                            (if (DungeonTimer.dungeonStartTime > 0L) DungeonTimer.dungeonStartTime else System.currentTimeMillis()) / 1000.0
                        val diff = floor(currentTime - 1560276000)
                        val year = (diff / 446400 + 1).toInt()
                        correctAnswers.add("Year $year")
                    } else {
                        triviaSolutions.entries.find {
                            fullQuestion == it.key
                        }?.let {
                            correctAnswers.addAll(it.value)
                        }
                    }
                }

                if (!SuperSecretSettings.bennettArthur || Funny.ticks % 2 == 0) {
                    if (!correctAnswers.any { it == answer }) {
                        event.message = ChatComponentText(formatted.replace("§a", "§c"))
                    } else correctAnswer = answer
                }

                if (type == "ⓒ") {
                    trackLines = false
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderArmorStandPre(event: RenderLivingEvent.Pre<EntityArmorStand>) {
        val answer = correctAnswer ?: return
        if (!Skytils.config.triviaSolver || !DungeonListener.missingPuzzles.contains("Quiz") || event.entity !is EntityArmorStand) return

        val name = event.entity.customNameTag

        if (name.isNotEmpty() && name.containsAny("ⓐ", "ⓑ", "ⓒ") && !name.contains(answer)) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        reset()
    }

    @SubscribeEvent
    fun onPuzzleReset(event: DungeonEvent.PuzzleEvent.Reset) {
        if (event.puzzle == "Quiz") {
            reset()
        }
    }

    private fun reset(trackLines: Boolean = false) {
        lines.clear()
        this.trackLines = trackLines
        fullQuestion = null
        correctAnswers.clear()
    }
}