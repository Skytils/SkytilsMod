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

import kotlinx.serialization.Contextual
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.graphics.colors.CustomColor
import java.io.File
import java.io.Reader
import java.io.Writer

class ArmorColor : PersistentSave(File(Skytils.modDir, "armorcolors.json")) {

    override fun read(reader: Reader) {
        armorColors.clear()
        armorColors.putAll(json.decodeFromString<Map<String, @Contextual CustomColor>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(armorColors))
    }

    override fun setDefault(writer: Writer) {
        writer.write("{}")
    }

    companion object {
        @JvmField
        val armorColors = HashMap<String, @Contextual CustomColor>()
    }
}