/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

package skytils.skytilsmod.core

import com.google.gson.Gson
import net.minecraft.client.Minecraft
import skytils.skytilsmod.Skytils
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.concurrent.fixedRateTimer
import kotlin.reflect.KClass

abstract class PersistentSave(val saveFile: File, val interval: Long = 30_000) {

    var dirty = false

    val gson: Gson = Skytils.gson
    val mc: Minecraft = Skytils.mc

    abstract fun read(reader: FileReader)

    abstract fun write(writer: FileWriter)

    abstract fun setDefault(writer: FileWriter)

    private fun readSave() {
        try {
            FileReader(this.saveFile).use { `in` ->
                read(`in`)
            }
        } catch (e: Exception) {
            try {
                FileWriter(this.saveFile).use { writer ->
                    setDefault(writer)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun writeSave() {
        try {
            FileWriter(this.saveFile).use { writer ->
                write(writer)
            }
            dirty = false
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {
        val SAVES = HashSet<PersistentSave>()

        fun markDirty(clazz: KClass<out PersistentSave>) {
            val save =
                SAVES.find { it::class == clazz } ?: throw IllegalAccessException("PersistentSave not found")
            save.dirty = true
        }
    }

    init {
        SAVES.add(this)
        readSave()
        fixedRateTimer("${this::class.simpleName}-Save", period = interval) {
            if (dirty) {
                writeSave()
            }
        }
        Runtime.getRuntime().addShutdownHook(Thread({
            if (dirty) {
                writeSave()
            }
        }, "${this::class.simpleName}-Save"))
    }

}
