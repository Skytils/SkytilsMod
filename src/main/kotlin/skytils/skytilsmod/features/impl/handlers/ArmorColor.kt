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

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.graphics.colors.CustomColor
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class ArmorColor {
    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private val mc = Minecraft.getMinecraft()
        private var colorFile = File(Skytils.modDir, "armorcolors.json")
        @JvmField
        val armorColors = HashMap<String, CustomColor>()
        fun reloadColors() {
            armorColors.clear()
            var dataObject: JsonObject
            try {
                FileReader(colorFile).use { `in` ->
                    dataObject = gson.fromJson(`in`, JsonObject::class.java)
                    for ((key, value) in dataObject.entrySet()) {
                        val color = Utils.customColorFromString(value.asString)
                        armorColors[key] = color ?: CommonColors.BLACK
                    }
                }
            } catch (e: Exception) {
                dataObject = JsonObject()
                try {
                    FileWriter(colorFile).use { writer -> gson.toJson(dataObject, writer) }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        fun saveColors() {
            try {
                FileWriter(colorFile).use { writer ->
                    val obj = JsonObject()
                    for ((key, value) in armorColors) {
                        obj.addProperty(key, value.toString())
                    }
                    gson.toJson(obj, writer)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    init {
        reloadColors()
    }
}