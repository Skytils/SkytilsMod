package gg.skytils.skytilsmod.core

import java.io.Reader
import java.io.Writer
import java.nio.file.Path
import kotlin.concurrent.fixedRateTimer
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer
import kotlin.reflect.KClass

abstract class PersistentSave(private val saveLocation: Path) {
    protected var dirty = false

    private fun init() {
        saves.add(this)
    }

    init {
        init()
    }

    abstract fun read(reader: Reader)

    abstract fun write(writer: Writer)

    private fun readSave() {
        try {
            saveLocation.createParentDirectories()
                .takeIf { !it.exists() }?.createFile()

            saveLocation.reader().use { read(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                saveLocation.writer().use { write(it) }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun writeSave() {
        try {
            saveLocation.createParentDirectories()
                .takeIf { !it.exists() }?.createFile()

            saveLocation.writer().use { write(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val saves = mutableSetOf<PersistentSave>()

        fun markDirty(clazz: KClass<out PersistentSave>) {
            val save = saves.find { it::class == clazz } ?: throw IllegalAccessException("PersistentSave not found")
            save.dirty = true
        }

        inline fun <reified T : PersistentSave> markDirty() {
            markDirty(T::class)
        }

        fun loadData() {
            saves.forEach(PersistentSave::readSave)
        }

        private fun saveAll() =
            saves.forEach { if (it.dirty) it.writeSave() }

        init {
            fixedRateTimer("Skytils-PersistentSave-Write", period = 30000L) {
                saveAll()
            }
            Runtime.getRuntime().addShutdownHook(Thread(::saveAll, "Skytils-PersistentSave-Shutdown"))
        }
    }
}