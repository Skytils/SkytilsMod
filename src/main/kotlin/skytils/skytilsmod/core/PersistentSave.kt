/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.concurrent.fixedRateTimer
import kotlin.reflect.KClass

abstract class PersistentSave(protected val saveFile: File, interval: Long = 30_000) {

    var dirty = false

    val gson: Gson = Skytils.gson
    val mc: Minecraft = Skytils.mc

    abstract fun read(reader: InputStreamReader)

    abstract fun write(writer: OutputStreamWriter)

    abstract fun setDefault(writer: OutputStreamWriter)

    private fun readSave() {
        try {
            if (!this.saveFile.exists()) {
                this.saveFile.parentFile.mkdirs()
                this.saveFile.createNewFile()
            }
            this.saveFile.reader().use {
                read(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                this.saveFile.writer().use {
                    setDefault(it)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun writeSave() {
        try {
            if (!this.saveFile.exists()) {
                this.saveFile.parentFile.mkdirs()
                this.saveFile.createNewFile()
            }
            this.saveFile.writer().use { writer ->
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

        inline fun <reified T : PersistentSave> markDirty() {
            markDirty(T::class)
        }

        init {
            Runtime.getRuntime().addShutdownHook(Thread({
                for (save in SAVES) {
                    if (save.dirty) save.writeSave()
                }
            }, "Skytils-PersistentSave-Shutdown"))
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
    }

}
