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

import com.google.gson.JsonObject
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import java.io.File
import java.io.FileReader
import java.io.FileWriter


object EnchantNames : PersistentSave(File(Skytils.modDir, "enchantnames.json")) {
    private val enchantRegex = Regex("§[0-9a-fz]([\\w \\-])+(?:§9, )?")
    val replacements = HashMap<String, String>()

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        val newToolTip = event.toolTip.map {
            var newline = it
            enchantRegex.findAll(it).forEach { result ->
                val enchant = result.value.substring(2).substringBeforeLast(",").substringBeforeLast(" ")
                if (replacements[enchant] == null) return@forEach
                newline = newline.replace(
                    result.value,
                    "${result.value.substring(0, 2)}§o${enchant.replaceEnchantNames()}${
                        result.value.substring(result.value.indexOf(enchant) + enchant.length)
                    }"
                )
            }
            newline
        }
        event.toolTip.clear()
        event.toolTip.addAll(newToolTip)
    }

    private fun String.replaceEnchantNames(): String {
        replacements[this]?.let { replacement ->
            return replacement
        }
        return this
    }

    override fun read(reader: FileReader) {
        replacements.clear()
        val obj = gson.fromJson(reader, JsonObject::class.java)
        for ((key, value) in obj.entrySet()) {
            replacements[key] = value.asString
        }
    }

    override fun write(writer: FileWriter) {
        val obj = JsonObject()
        for ((key, value) in replacements) {
            obj.addProperty(key, value)
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
        gson.toJson(JsonObject(), writer)
    }
}