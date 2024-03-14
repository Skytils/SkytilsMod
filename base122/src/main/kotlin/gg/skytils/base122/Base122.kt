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

@file:OptIn(ExperimentalUnsignedTypes::class)
package gg.skytils.base122

internal val illegalCharacters = ubyteArrayOf(
    0u,  //null
    10u, //newline
    13u, //carriage return
    34u, //double quote
    38u, //ampersand
    92u  //backslash
)
internal val shortenedMarker = 0b111

fun breakInto7BitChunks(input: ByteArray) =
    input.asUByteArray().asIterable().chunked(7).flatMap { byteList ->
        val bytes = byteList.mapIndexed { index, byte ->
            val a = byte.toLong() shl ((byteList.size - index - 1) * 8)
            a
        }.sum()
        val chunks = byteList.size * 8 / 7
        val rem = chunks.rem(8)
        val chunkList = (0 until chunks).map {
            val offset = (chunks - it - 1) * 7 + rem
            val mask = (0b01111111).toLong() shl offset
            (bytes.and(mask) shr offset).toByte()
        }
        val remainder = if (rem != 0) {
            listOf((bytes and ((0b1 shl rem) - 1L)).toByte())
        } else {
            emptyList()
        }
        chunkList + remainder
    }

fun encode(input: ByteArray) =
    sequence {
        breakInto7BitChunks(input)
            .toByteArray()
            .inputStream()
            .let { bytes ->
                while (bytes.available() > 0) {
                    val bits = bytes.read().toUByte()

                    val illegalIndex = illegalCharacters.indexOf(bits)
                    if (illegalIndex != -1) {
                        // Illegal char markers
                        var first = 0b11000010
                        var second = 0b10000000

                        // Illegal characters are encoded as two bytes
                        // so we get the next chunk of seven bits.
                        val nextBits = if (bytes.available() > 0) {
                            first = first or ((0b111 and shortenedMarker) shl 2)
                            bytes.read().toUByte()
                        } else {
                            first = first or ((0b111 and illegalIndex) shl 2)
                            bits
                        }

                        val firstBit = if ((nextBits and 0b01000000u) > 0u) {
                            1
                        } else {
                            0
                        }
                        first = first or firstBit
                        second = second or (nextBits and 0b00111111u).toInt()
                        yield(first.toUByte())
                        yield(second.toUByte())
                    } else {
                        yield(bits)
                    }
                }
            }
    }