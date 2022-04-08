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

package sharttils.sharttilsmod.features.impl.protectitems.strategy.impl

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.core.PersistentSave
import sharttils.sharttilsmod.features.impl.dungeons.DungeonFeatures
import sharttils.sharttilsmod.features.impl.protectitems.strategy.ItemProtectStrategy
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object FavoriteStrategy : ItemProtectStrategy() {
    val favoriteItems = hashSetOf<String>()
    val save = FavoriteStrategySave

    override fun worthProtecting(item: ItemStack, extraAttr: NBTTagCompound?, type: ProtectType): Boolean {
        if (type == ProtectType.HOTBARDROPKEY && DungeonFeatures.hasClearedText) return false
        return favoriteItems.contains(extraAttr?.getString("uuid"))
    }

    override val isToggled: Boolean = true

    object FavoriteStrategySave : PersistentSave(File(Sharttils.modDir, "favoriteitems.json")) {
        override fun read(reader: InputStreamReader) {
            favoriteItems.addAll(gson.fromJson<HashSet<String>>(reader, HashSet::class.java)!!)
        }

        override fun write(writer: OutputStreamWriter) {
            gson.toJson(favoriteItems, writer)
        }

        override fun setDefault(writer: OutputStreamWriter) {
            writer.write("[]")
        }

    }
}