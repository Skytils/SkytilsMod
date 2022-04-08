/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.features.impl.handlers

import com.google.gson.JsonObject
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.core.PersistentSave
import sharttils.sharttilsmod.utils.Utils
import sharttils.sharttilsmod.utils.graphics.colors.CustomColor
import java.io.*

class ArmorColor : PersistentSave(File(Sharttils.modDir, "armorcolors.json")) {

    override fun read(reader: InputStreamReader) {
        armorColors.clear()
        val dataObject = gson.fromJson(reader, JsonObject::class.java)
        for ((key, value) in dataObject.entrySet()) {
            val color = Utils.customColorFromString(value.asString)
            armorColors[key] = color
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val obj = JsonObject()
        for ((key, value) in armorColors) {
            obj.addProperty(key, value.toString())
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonObject(), writer)
    }

    companion object {
        @JvmField
        val armorColors = HashMap<String, CustomColor>()
    }
}