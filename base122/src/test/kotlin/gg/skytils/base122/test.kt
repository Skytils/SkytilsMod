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

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Test {
    @Test
    fun test7BitChunks() {
        val bytes = byteArrayOf(
            0b11111110.toByte(),
            0b00000011,
            0b11111000.toByte(),
            0b00001111,
            0b11100000.toByte(),
            0b00111111,
            0b10000000.toByte(),
            0b01111111
        )
        val result = breakInto7BitChunks(bytes)
        result.forEachIndexed { index, l ->
            val expectedValue = 0b01111111 * ((index + 1) % 2).toLong()
            assertEquals(expectedValue, l, "Unexpected value at index $index (${l.toString(2)})")
        }
    }

    @Test
    fun testUneven7BitChunks() {

        val bytes = byteArrayOf(
            0b11111110.toByte(),
            0b00000011,
            0b11111000.toByte(),
            0b00001111,
            0b11100001.toByte()
        )
        val result = breakInto7BitChunks(bytes)
        val expectedValues = byteArrayOf(
            0b01111111,
            0b00000000,
            0b01111111,
            0b00000000,
            0b01111111,
            0b00000001
        )
        result.forEachIndexed { index, l ->
            val expectedValue = expectedValues[index].toLong()
            assertEquals(expectedValue, l, "Unexpected value at index $index (${l.toString(2)})")
        }
    }
}