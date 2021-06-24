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

import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.DataFetcher
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class BlockAbility : PersistentSave(File(Skytils.modDir, "blockability.json")) {
    //@SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.entityPlayer !== mc.thePlayer) return
        val item = event.entityPlayer.heldItem ?: return
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (shouldBlockAbility(item)) event.isCanceled = true
        } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            val block = event.world.getBlockState(event.pos).block
            if (!interactables.contains(block) || Utils.inDungeons && (block === Blocks.coal_block || block === Blocks.stained_hardened_clay)) {
                if (shouldBlockAbility(item)) event.isCanceled = true
            }
        }
    }

    private fun shouldBlockAbility(item: ItemStack?): Boolean {
        if (item != null) {
            val extraAttr = ItemUtil.getExtraAttributes(item)
            val itemId = ItemUtil.getSkyBlockItemID(extraAttr)
            if (extraAttr != null && itemId != null) {
                if (Skytils.config.blockMathHoeClicks && itemId.startsWith("THEORETICAL_HOE")) {
                    return true
                }
                return if (Skytils.config.blockUselessZombieSword && mc.thePlayer.health >= mc.thePlayer.maxHealth && itemId.contains(
                        "ZOMBIE_SWORD"
                    )
                ) {
                    true
                } else blockedItems.contains(itemId)
            }
        }
        return false
    }

    override fun read(reader: FileReader) {
        blockedItems.clear()
        blockedItems.addAll(
            gson.fromJson(
                reader,
                JsonArray::class.java
            ).asJsonArray.map { it.asString }
        )
    }

    override fun write(writer: FileWriter) {
        val arr = JsonArray()
        for (itemId in blockedItems) {
            arr.add(JsonPrimitive(itemId))
        }
        gson.toJson(arr, writer)
    }

    override fun setDefault(writer: FileWriter) {
        gson.toJson(JsonArray(), writer)
    }


    companion object {
        val blockedItems = HashSet<String>()
        val interactables = setOf(
            Blocks.acacia_door,
            Blocks.anvil,
            Blocks.beacon,
            Blocks.bed,
            Blocks.birch_door,
            Blocks.brewing_stand,
            Blocks.command_block,
            Blocks.crafting_table,
            Blocks.chest,
            Blocks.dark_oak_door,
            Blocks.daylight_detector,
            Blocks.daylight_detector_inverted,
            Blocks.dispenser,
            Blocks.dropper,
            Blocks.enchanting_table,
            Blocks.ender_chest,
            Blocks.furnace,
            Blocks.hopper,
            Blocks.jungle_door,
            Blocks.lever,
            Blocks.noteblock,
            Blocks.powered_comparator,
            Blocks.unpowered_comparator,
            Blocks.powered_repeater,
            Blocks.unpowered_repeater,
            Blocks.standing_sign,
            Blocks.wall_sign,
            Blocks.trapdoor,
            Blocks.trapped_chest,
            Blocks.wooden_button,
            Blocks.stone_button,
            Blocks.oak_door,
            Blocks.skull
        )
    }
}