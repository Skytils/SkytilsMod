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

package gg.skytils.base122

@OptIn(ExperimentalUnsignedTypes::class)
fun breakInto7BitChunks(input: ByteArray) =
    input.asUByteArray().asIterable().chunked(7).flatMap { byteList ->
        val bytes = byteList.mapIndexed { index, byte ->
            val a = byte.toLong() shl ((byteList.size - index - 1) * 8)
            a
        }.sum()
        val chunks = byteList.size * 8 / 7
        val remainder = chunks.rem(8)
        (0 until chunks).map {
            val offset = (chunks - it - 1) * 7 + remainder
            val mask = (0b01111111).toLong() shl offset
            bytes.and(mask) shr offset
        } + if (remainder != 0) {
            bytes and ((0b1 shl remainder) - 1L)
        } else {
            0L
        }
    }