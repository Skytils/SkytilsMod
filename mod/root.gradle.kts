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

version = "2.0.0-alpha.4"

preprocess {
    val fabric12105 = createNode("1.21.5-fabric", 12105, "yarn")
    val fabric12110 = createNode("1.21.10-fabric", 12110, "yarn")
    val fabric12111 = createNode("1.21.11-fabric", 12111, "yarn")

    fabric12111.link(fabric12110)
    fabric12110.link(fabric12105, file("versions/1.21.11-1.21.5.txt"))
}