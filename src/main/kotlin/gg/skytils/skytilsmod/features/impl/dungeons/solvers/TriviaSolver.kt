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
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.misc.Funny
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.startsWithAny
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.block.BlockButtonStone
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
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
    val triviaSolutions = hashMapOf<String, List<String>>()
    var triviaAnswers: List<String>? = null
    var triviaAnswer: String? = null

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type == 2.toByte()) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        val formatted = event.message.formattedText
        if (Skytils.config.triviaSolver && Utils.inDungeons) {
            if (unformatted.startsWith("[STATUE] Oruo the Omniscient: ") && unformatted.contains("answered Question #") && unformatted.endsWith(
                    "correctly!"
                )
            ) triviaAnswer = null
            if (unformatted == "[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know." && triviaSolutions.size == 0) {
                UChat.chat("$failPrefix §cSkytils failed to load solutions for Trivia.")
                DataFetcher.reloadData()
            }
            if (unformatted.trim() == "What SkyBlock year is it?") {
                val currentTime =
                    (if (DungeonTimer.dungeonStartTime > 0L) DungeonTimer.dungeonStartTime else System.currentTimeMillis()) / 1000.0
                val diff = floor(currentTime - 1560276000)
                val year = (diff / 446400 + 1).toInt()
                triviaAnswers = listOf("Year $year")
            } else {
                triviaSolutions.entries.find {
                    unformatted.contains(it.key)
                }?.let {
                    triviaAnswers = it.value
                }
            }

            if (triviaAnswers != null && formatted.trim().startsWithAny("§r§6 ⓐ", "§r§6 ⓑ", "§r§6 ⓒ")) {
                triviaAnswers!!.find {
                    formatted.endsWith("§a$it§r") && (!SuperSecretSettings.bennettArthur || Funny.ticks % 2 == 0)
                }.also {
                    if (it == null) {
                        event.message = ChatComponentText(formatted.replace("§a", "§c"))
                    } else {
                        triviaAnswer = it
                    }
                }
            }
        }
    }

    //@SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!Utils.inDungeons || !Skytils.config.triviaSolver || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return
        val block = event.world.getBlockState(event.pos)
        if (block.block === Blocks.stone_button) {
            if (triviaAnswer != null) {
                var answerLabel: EntityArmorStand? = null
                for (e in mc.theWorld.loadedEntityList) {
                    if (e !is EntityArmorStand) continue
                    if (!e.hasCustomName()) continue
                    val name = e.customNameTag
                    if (name.contains(triviaAnswer!!) && (name.contains("ⓐ") || name.contains("ⓑ") || name.contains("ⓒ"))) {
                        answerLabel = e
                        break
                    }
                }
                if (answerLabel != null) {
                    println("Found Answer Marker " + answerLabel.customNameTag + " at " + answerLabel.posX + ", " + answerLabel.posY + ", " + answerLabel.posZ)
                    val buttonBlock = BlockPos(answerLabel.posX, 70.0, answerLabel.posZ)
                    val blockBehind = BlockPos(event.pos.offset(block.getValue(BlockButtonStone.FACING).opposite))
                    if (mc.theWorld.getBlockState(buttonBlock).block === Blocks.double_stone_slab && mc.theWorld.getBlockState(
                            blockBehind
                        ).block === Blocks.double_stone_slab && buttonBlock != blockBehind
                    ) {
                        var isRight = false
                        for (dir in EnumFacing.HORIZONTALS) {
                            if (buttonBlock.offset(dir) == event.pos) {
                                isRight = true
                                break
                            }
                        }
                        if (!isRight) {
                            println("Wrong button clicked, position: " + event.pos.x + ", " + event.pos.y + ", " + event.pos.z)
                            if (!(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
                                event.isCanceled = true
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderArmorStandPre(event: RenderLivingEvent.Pre<EntityArmorStand?>) {
        if (Skytils.config.triviaSolver && triviaAnswer != null) {
            if (event.entity is EntityArmorStand) {
                val name = event.entity.customNameTag
                if (name.isNotEmpty() && name.contains("ⓐ") || name.contains("ⓑ") || name.contains("ⓒ")) {
                    if (!name.contains(triviaAnswer!!)) {
                        event.isCanceled = true
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        triviaAnswer = null
    }
}