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

package gg.skytils.skytilsmod.features.impl.handlers

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.DevTools
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.Reader
import java.io.Writer

object EnchantNames : PersistentSave(File(Skytils.modDir, "enchantnames.json")) {
    private val enchantRegex =
        Regex("(?<color>(?:§[0-9a-fzl]){1,2})(?<enchant> ?[\\w ]+[\\w \\-]*?)(?<level> [IVXLCDM0-9]{1,3})(?<suffix>§[9d], )?")
    val replacements = hashMapOf<String, String>()

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        event.toolTip.replaceAll {
            var newline = it
            enchantRegex.findAll(
                it
            ).forEach { result ->
                val color = result.groups["color"]!!.value
                val enchant = result.groups["enchant"]!!.value
                val level = result.groups["level"]!!.value
                val suffix = result.groups["suffix"]?.value ?: ""
                if (DevTools.getToggle("enchantNames")) {
                    println(enchant)
                    println(result.groups)
                }
                newline = newline.replace(
                    result.value,
                    buildString {
                        append(color)
                        if (DevTools.getToggle("enchantNames")) append("{")
                        if (replacements[enchant] != null)
                            append("§o${enchant.replaceEnchantNames()}")
                        else
                            append(enchant)
                        append(level)
                        if (DevTools.getToggle("enchantNames")) append("}")
                        append(suffix)
                    }
                )
            }
            newline
        }
    }

    private fun String.replaceEnchantNames(): String {
        replacements[this]?.let { replacement ->
            return replacement
        }
        return this
    }

    override fun read(reader: Reader) {
        replacements.clear()
        replacements.putAll(
            json.decodeFromString<Map<String, String>>(reader.readText())
        )
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(replacements))
    }

    override fun setDefault(writer: Writer) {
        writer.write("{}")
    }
}