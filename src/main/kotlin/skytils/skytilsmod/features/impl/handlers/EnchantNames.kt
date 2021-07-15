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
import skytils.skytilsmod.utils.DevTools
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class EnchantNames : PersistentSave(File(Skytils.modDir, "enchantnames.json")) {
    companion object {
        private val enchantRegex = Regex("§[0-9a-fzl]([\\w \\-]+)(?:§[9d], )?")
        val replacements = HashMap<String, String>()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        val newToolTip = event.toolTip.map {
            var newline = it
            enchantRegex.findAll(it).forEach { result ->
                val enchant = result.groups[1]!!.value.substringBeforeLast(",").substringBeforeLast(" ")
                if (DevTools.getToggle("enchantNames")) {
                    println(enchant)
                    println(result.groups)
                }
                newline = newline.replace(
                    result.value,
                    buildString {
                        append(
                            result.value.substring(
                                0,
                                2
                            )
                        )
                        if (DevTools.getToggle("enchantNames")) append("{")
                        if (replacements[enchant] != null)
                            append("§o${enchant.replaceEnchantNames()}")
                        else
                            append(enchant)
                        append(result.value.substringAfterLast(enchant))
                        if (DevTools.getToggle("enchantNames")) append("}")
                    }
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