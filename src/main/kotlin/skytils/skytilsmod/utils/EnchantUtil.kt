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

package skytils.skytilsmod.utils

import com.google.gson.*
import net.minecraft.nbt.NBTTagCompound
import java.lang.reflect.Type

object EnchantUtil {
    val enchants = arrayListOf<Enchant>()
}

interface Enchant {
    val loreName: String
    val nbtName: String
    val goodLevel: Int
    val maxLevel: Int

    class Serializer : JsonDeserializer<Enchant> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Enchant {
            json as JsonObject

            if (json.has("stackLevel")) {
                return StackingEnchant(
                    json["loreName"].asString,
                    json["nbtName"].asString,
                    json["goodLevel"].asInt,
                    json["maxLevel"].asInt,
                    json["nbtNum"].asString,
                    json["statLabel"].asString,
                    json["stackLevel"].asJsonArray.map { it.asInt }
                )
            } else {
                return NormalEnchant(
                    json["loreName"].asString,
                    json["nbtName"].asString,
                    json["goodLevel"].asInt,
                    json["maxLevel"].asInt,
                    json["nbtName"].asString.startsWith("ultimate_")
                )
            }
        }
    }
}

data class NormalEnchant(
    override val loreName: String,
    override val nbtName: String,
    override val goodLevel: Int,
    override val maxLevel: Int,
    val isUltimate: Boolean
) : Enchant

data class StackingEnchant(
    override val loreName: String,
    override val nbtName: String,
    override val goodLevel: Int,
    override val maxLevel: Int,
    val nbtNum: String,
    val statLabel: String,
    val stackLevel: List<Int>
) : Enchant