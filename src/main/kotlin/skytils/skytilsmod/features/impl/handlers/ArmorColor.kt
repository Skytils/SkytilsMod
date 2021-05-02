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
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.colors.CustomColor
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class ArmorColor : PersistentSave(File(Skytils.modDir, "armorcolors.json")) {

    override fun read(reader: FileReader) {
        armorColors.clear()
        var dataObject = gson.fromJson(reader, JsonObject::class.java)
        for ((key, value) in dataObject.entrySet()) {
            val color = Utils.customColorFromString(value.asString)
            armorColors[key] = color
        }
    }

    override fun write(writer: FileWriter) {
        val obj = JsonObject()
        for ((key, value) in armorColors) {
            obj.addProperty(key, value.toString())
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
        gson.toJson(JsonObject(), writer)
    }

    companion object {
        @JvmField
        val armorColors = HashMap<String, CustomColor>()
    }
}