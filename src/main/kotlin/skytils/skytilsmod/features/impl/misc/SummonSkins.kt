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

package skytils.skytilsmod.features.impl.misc

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.launch
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.client
import skytils.skytilsmod.utils.graphics.DynamicResource
import javax.imageio.ImageIO

object SummonSkins {
    // maps name to url
    val skinMap = HashMap<String, String>()

    // maps name to dynamic resource
    val skintextureMap = HashMap<String, DynamicResource>()

    init {
        loadSkins()
    }

    fun loadSkins() = Skytils.IO.launch {
        skintextureMap.clear()
        skinMap.forEach {
            skintextureMap[it.key] =
                DynamicResource(it.key, ImageIO.read(client.get(it.value).bodyAsChannel().toInputStream()))
        }
    }
}