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
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.core.DataFetcher
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ThreeWeirdosSolver {
    val solutions = hashSetOf<String>()

    var riddleNPC: String? = null

    @JvmField
    var riddleChest: BlockPos? = null

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type == 2.toByte()) return
        if (!Skytils.config.threeWeirdosSolver || !Utils.inDungeons || !DungeonListener.missingPuzzles.contains("Three Weirdos")) return
        val formatted = event.message.formattedText
        if (formatted.startsWith("§a§lPUZZLE SOLVED!") && "wasn't fooled by " in formatted) {
            riddleNPC = null
            riddleChest = null
        }
        if (formatted.startsWith("§e[NPC] ")) {
            if (solutions.size == 0) {
                UChat.chat("$failPrefix §cSkytils failed to load solutions for Three Weirdos.")
                DataFetcher.reloadData()
            }

            if (solutions.any {
                    SuperSecretSettings.bennettArthur || formatted.contains(it)
                }) {
                val npcName = formatted.substringAfter("§c").substringBefore("§f")
                riddleNPC = npcName
                UChat.chat("$prefix §a§l${npcName.stripControlCodes()} §2has the blessing.")

                mc.theWorld?.loadedEntityList?.find {
                    it is EntityArmorStand && riddleNPC!! in it.customNameTag
                }?.let {
                    riddleChest = EnumFacing.HORIZONTALS.map { dir -> it.position.offset(dir)  }.find {
                        mc.theWorld?.getBlockState(it)?.block == Blocks.chest
                    }
                    println("Riddle NPC ${it.customNameTag} @ ${it.position} w/ chest @ $riddleChest")
                }
            }
        }
    }


    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        riddleNPC = null
        riddleChest = null
    }
}