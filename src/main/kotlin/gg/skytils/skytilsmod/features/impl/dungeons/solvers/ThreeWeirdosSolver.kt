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
import net.minecraftforge.client.event.RenderWorldLastEvent
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
        if (!Skytils.config.threeWeirdosSolver || !Utils.inDungeons || !DungeonListener.missingPuzzles.contains("Three Weirdos")) return
        val formatted = event.message.formattedText
        if (formatted.startsWith("§a§lPUZZLE SOLVED!")) {
            if (formatted.contains("wasn't fooled by ")) {
                riddleNPC = null
                riddleChest = null
            }
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
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Skytils.config.threeWeirdosSolver || !Utils.inDungeons || riddleNPC == null) return
        if (riddleChest == null) {
            val riddleLabel = mc.theWorld.getEntities(
                EntityArmorStand::class.java
            ) { entity: EntityArmorStand? ->
                if (entity == null) return@getEntities false
                if (!entity.hasCustomName()) return@getEntities false
                entity.customNameTag.contains(riddleNPC!!)
            }.firstOrNull()
            if (riddleLabel != null) {
                println("Chest Finder: Found Riddle NPC " + riddleLabel.customNameTag + " at " + riddleLabel.position)
                val npcPos = riddleLabel.position
                for (direction in EnumFacing.HORIZONTALS) {
                    val potentialPos = npcPos.offset(direction)
                    if (mc.theWorld.getBlockState(potentialPos).block === Blocks.chest) {
                        riddleChest = potentialPos
                        print("Correct position is at: $potentialPos")
                        break
                    }
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