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

package gg.skytils.skytilsmod.utils

import com.aayushatharva.brotli4j.decoder.BrotliInputStream
import io.ktor.client.plugins.compression.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.CoroutineScope

class BrotliEncoder(val useNative: Boolean) : ContentEncoder {
    override val name: String = "br"

    override fun CoroutineScope.decode(source: ByteReadChannel): ByteReadChannel {
        val bombChecker = DecompressionBombChecker(100)
        val wrapped = bombChecker.wrapInput(source.toInputStream())

        val inputStream = if (useNative) {
            BrotliInputStream(wrapped)
        } else {
            org.brotli.dec.BrotliInputStream(wrapped)
        }

        return bombChecker.wrapOutput(
            inputStream
        ).toByteReadChannel()
    }

    override fun CoroutineScope.encode(source: ByteReadChannel): ByteReadChannel = throw UnsupportedOperationException("Cannot encode Brotli")
}