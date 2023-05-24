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

package gg.skytils.skytilsmod.mixins.hooks.gui

import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiEditSign

val prompts = listOf(
    "Enter query",
    "auction bid",
    "Enter amount",
    "Enter price",
    "Your auction",
    "Enter the amount",
    "Auction", // Auction time
).map { "^^^^^^^^^^^^^^^" to it }

fun isConfirmableSign(gui: AccessorGuiEditSign) =
    prompts.any { it in gui.tileSign.signText.asSequence().map { it.unformattedText }.zipWithNext() }