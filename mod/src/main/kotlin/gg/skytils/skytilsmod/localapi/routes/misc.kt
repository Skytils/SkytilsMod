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

package gg.skytils.skytilsmod.localapi.routes

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.localapi.LocalAPI
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.registerMiscRoutes() = apply {
    get("/version") {
        context.respond(LocalAPIVersion.VERSION)
    }
}

@Serializable
data class LocalAPIVersion(
    val modVersion: String,
    val apiVersion: Int
) {
    companion object {
        val VERSION = LocalAPIVersion(Skytils.VERSION, LocalAPI.version)
    }
}