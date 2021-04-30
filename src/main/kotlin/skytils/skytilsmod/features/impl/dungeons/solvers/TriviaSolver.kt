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
package skytils.skytilsmod.features.impl.dungeons.solvers

import net.minecraft.block.BlockButtonStone
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.DataFetcher
import skytils.skytilsmod.utils.Utils
import kotlin.math.floor

/**
 * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license and modified by the Skytils team
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
class TriviaSolver {
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        val unformatted = StringUtils.stripControlCodes(event.message.unformattedText)
        if (Skytils.config.triviaSolver && Utils.inDungeons) {
            if (unformatted.startsWith("[STATUE] Oruo the Omniscient: ") && unformatted.contains("answered Question #") && unformatted.endsWith(
                    "correctly!"
                )
            ) triviaAnswer = null
            if (unformatted == "[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know." && triviaSolutions.size == 0) {
                mc.thePlayer.addChatMessage(ChatComponentText("§cSkytils failed to load solutions for Trivia."))
                DataFetcher.reloadData()
            }
            if (unformatted.contains("What SkyBlock year is it?")) {
                val currentTime = System.currentTimeMillis() / 1000.0
                val diff = floor(currentTime - 1560276000)
                val year = (diff / 446400 + 1).toInt()
                triviaAnswers = arrayOf("Year $year")
            } else {
                for (question in triviaSolutions.keys) {
                    if (unformatted.contains(question)) {
                        triviaAnswers = triviaSolutions[question]
                        break
                    }
                }
            }
            // Set wrong answers to red and remove click events
            if (triviaAnswers != null && (unformatted.contains("ⓐ") || unformatted.contains("ⓑ") || unformatted.contains(
                    "ⓒ"
                ))
            ) {
                var answer: String? = null
                var isSolution = false
                for (solution in triviaAnswers!!) {
                    if (unformatted.contains(solution)) {
                        isSolution = true
                        answer = solution
                        break
                    }
                }
                if (!isSolution) {
                    val letter = unformatted[5]
                    val option = unformatted.substring(6)
                    event.message =
                        ChatComponentText("     " + EnumChatFormatting.GOLD + letter + EnumChatFormatting.RED + option)
                    return
                } else {
                    triviaAnswer = answer
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
            if (event.entity is EntityArmorStand && event.entity.hasCustomName()) {
                val name = event.entity.customNameTag
                if (name.contains("ⓐ") || name.contains("ⓑ") || name.contains("ⓒ")) {
                    if (!name.contains(triviaAnswer!!)) {
                        event.isCanceled = true
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        triviaAnswer = null
    }

    companion object {
        var triviaSolutions = HashMap<String, Array<String>>()
        var triviaAnswers: Array<String>? = null
        var triviaAnswer: String? = null
        private val mc = Minecraft.getMinecraft()
    }
}