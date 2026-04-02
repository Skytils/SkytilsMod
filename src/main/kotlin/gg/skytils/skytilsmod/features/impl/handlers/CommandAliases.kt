package gg.skytils.skytilsmod.features.impl.handlers

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.json
import gg.skytils.skytilsmod.core.PersistentSave
import java.io.Reader
import java.io.Writer

// FIXME: tab completion doesn't work
// FIXME: aliases show as invalid (fixing tabe completion will probably fix this)
object CommandAliases : PersistentSave(Skytils.modDir.resolve("commandaliases.json")) {
    val aliases = mutableMapOf<String, String>()

    override fun read(reader: Reader) {
        aliases.clear()
        aliases.putAll(json.decodeFromString(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(aliases))
    }
}