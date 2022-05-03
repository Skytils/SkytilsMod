/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.graphics.colors.CustomColor
import java.io.File
import java.io.Reader
import java.io.Writer

class GlintCustomizer : PersistentSave(File(Skytils.modDir, "customizedglints.json")) {

    override fun read(reader: Reader) {
        glintItems.clear()
        glintItems.putAll(json.decodeFromString<Map<String, CustomGlint>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        glintItems.entries.removeAll { (_, glint) ->
            glint.override == null && glint.color == null
        }
        writer.write(json.encodeToString(glintItems))
    }

    override fun setDefault(writer: Writer) {
        writer.write("{}")
    }

    @Serializable
    data class CustomGlint(
        var override: Boolean?,
        @Serializable(with = CustomColor.Serializer::class)
        var color: CustomColor?
    )

    companion object {
        @JvmField
        val glintItems = HashMap<String, CustomGlint>()
    }
}