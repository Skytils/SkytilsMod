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

import gg.skytils.event.EventSubscriber
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor
import kotlinx.serialization.Contextual
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.Reader
import java.io.Writer

object ArmorColor : PersistentSave(File(Skytils.modDir, "armorcolors.json")), EventSubscriber {
    val armorColors = hashMapOf<String, @Contextual CustomColor>()

    override fun setup() {}

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
}