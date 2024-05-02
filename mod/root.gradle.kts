/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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
plugins {
    kotlin("jvm") apply false
    id("gg.essential.multi-version.root")
}

version = "2.0.0"

preprocess {
    val forge10809 = createNode("1.8.9-forge", 10809, "mcp")
    val forge11602 = createNode("1.16.2-forge", 11602, "mcp")
    val fabric11602 = createNode("1.16.2-fabric", 11602, "yarn")
    val fabric12004 = createNode("1.20.4-fabric", 12004, "yarn")

    fabric12004.link(fabric11602)
    fabric11602.link(forge11602)
    forge11602.link(forge10809)
}