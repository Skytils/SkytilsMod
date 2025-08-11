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

import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.item.ItemTooltipEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.DevTools
import gg.skytils.skytilsmod.utils.formattedText
import gg.skytils.skytilsmod.utils.multiplatform.textComponent
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.Reader
import java.io.Writer

object EnchantNames : EventSubscriber, PersistentSave(File(Skytils.modDir, "enchantnames.json")) {
    private val enchantRegex =
        Regex("(?<color>(?:§[0-9a-fzlr]){1,2})(?<enchant> ?[\\w ]+[\\w \\-]*?)(?<level> [IVXLCDM0-9]{1,3})(?<suffix>§[9d], )?")
    val replacements = hashMapOf<String, String>()

    override fun setup() {
        register(::onTooltip, EventPriority.Lowest)
    }

    fun onTooltip(event: ItemTooltipEvent) {
        if (replacements.isEmpty()) return
        event.tooltip.replaceAll { line ->
            val lineText = line.formattedText
            val matches = enchantRegex.findAll(lineText)
            if (matches.count() == 0) return@replaceAll line
            matches.fold(lineText) { current, result ->
                val color = result.groups["color"]!!.value
                val enchant = result.groups["enchant"]!!.value
                if (replacements[enchant] == null) return@fold current
                val level = result.groups["level"]!!.value
                val suffix = result.groups["suffix"]?.value ?: ""
                if (DevTools.getToggle("enchantNames")) {
                    println(enchant)
                    println(result.groups)
                }
                current.replace(
                    result.value,
                    buildString {
                        append(color)
                        if (DevTools.getToggle("enchantNames")) append("{")
                        append("§o${enchant.replaceEnchantNames()}")
                        append(level)
                        if (DevTools.getToggle("enchantNames")) append("}")
                        append(suffix)
                    }
                )
            }.let(::textComponent)
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