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

import io.ktor.client.plugins.compression.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.CoroutineScope
import org.brotli.dec.BrotliInputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

object BrotliEncoder : ContentEncoder {
    override val name: String = "br"

    override fun CoroutineScope.decode(source: ByteReadChannel): ByteReadChannel {
        val bombChecker = DecompressionBombChecker(100)
        return bombChecker.wrapOutput(BrotliInputStream(bombChecker.wrapInput(source.toInputStream()))).toByteReadChannel()
    }

    override fun CoroutineScope.encode(source: ByteReadChannel): ByteReadChannel = throw UnsupportedOperationException("Cannot encode Brotli")

    /*
     * Copyright (C) 2024 Square, Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    internal class DecompressionBombChecker(private val maxRatio: Long) {
        private var inputByteCount = 0L
        private var outputByteCount = 0L

        fun wrapInput(source: InputStream): InputStream {
            return object : FilterInputStream(source) {
                override fun read(): Int {
                    return super.read().also {
                        if (it != -1) inputByteCount += it
                    }
                }

                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    return super.read(b, off, len).also {
                        if (it != -1) inputByteCount += it
                    }
                }
            }
        }

        fun wrapOutput(source: InputStream): InputStream {
            return object : FilterInputStream(source) {
                override fun read(): Int {
                    return super.read().also {
                        addOutputByteAndCheck(it)
                    }
                }

                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    return super.read(b, off, len).also {
                        addOutputByteAndCheck(it)
                    }
                }
            }
        }

        private fun addOutputByteAndCheck(i: Int) {
            if (i == -1) return
            outputByteCount += i
            if (outputByteCount > inputByteCount * maxRatio) {
                throw IOException("decompression bomb? outputByteCount=$outputByteCount, inputByteCount=$inputByteCount exceeds max ratio of $maxRatio")
            }
        }
    }
}