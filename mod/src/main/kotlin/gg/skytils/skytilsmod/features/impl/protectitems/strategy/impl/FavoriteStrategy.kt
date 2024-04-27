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

package gg.skytils.skytilsmod.features.impl.protectitems.strategy.impl

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.protectitems.strategy.ItemProtectStrategy
import gg.skytils.skytilsmod.utils.ItemUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import java.io.File
import java.io.Reader
import java.io.Writer

object FavoriteStrategy : ItemProtectStrategy() {
    val favoriteUUIDs = hashSetOf<String>()
    val favoriteItemIds = hashSetOf<String>()
    val save = FavoriteStrategySave

    override fun worthProtecting(item: ItemStack, extraAttr: NBTTagCompound?, type: ProtectType): Boolean {
        if (type == ProtectType.HOTBARDROPKEY && DungeonFeatures.hasClearedText) return false
        return favoriteUUIDs.contains(extraAttr?.getString("uuid")) || favoriteItemIds.contains(
            ItemUtil.getSkyBlockItemID(
                extraAttr
            )
        )
    }

    override val isToggled: Boolean = true

    object FavoriteStrategySave : PersistentSave(File(Skytils.modDir, "favoriteitems.json")) {
        override fun read(reader: Reader) {
            val data = json.decodeFromString<JsonElement>(reader.readText())
            if (data is JsonObject) {
                json.decodeFromJsonElement<Schema>(data).also {
                    favoriteUUIDs.addAll(it.favoriteUUIDs)
                    favoriteItemIds.addAll(it.favoriteItemIds)
                }
            } else if (data is JsonArray) {
                favoriteUUIDs.addAll(json.decodeFromJsonElement<Set<String>>(data))
            }
        }

        override fun write(writer: Writer) {
            writer.write(json.encodeToString(Schema(favoriteUUIDs, favoriteItemIds)))
        }

        override fun setDefault(writer: Writer) {
            writer.write(json.encodeToString(Schema()))
        }

        @Serializable
        data class Schema(val favoriteUUIDs: Set<String> = emptySet(), val favoriteItemIds: Set<String> = emptySet())
    }
}