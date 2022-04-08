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

package sharttils.sharttilsmod.features.impl.misc

import sharttils.sharttilsmod.utils.APIUtil.getImage
import sharttils.sharttilsmod.utils.graphics.DynamicResource
import java.net.URL

object SummonSkins {
    // maps name to url
    val skinMap = HashMap<String, String>()

    // maps name to dynamic resource
    val skintextureMap = HashMap<String, DynamicResource>()

    init {
        loadSkins()
    }

    fun loadSkins() {
        skintextureMap.clear()
        skinMap.forEach {
            skintextureMap[it.key] = DynamicResource(it.key, URL(it.value).getImage())
        }
    }
}