package gg.skytils.skytilsmod.features.impl.handlers

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.json
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.mixins.accessor.CommandNodeAccessor
import net.minecraft.client.network.ClientCommandSource
import java.io.Reader
import java.io.Writer

object CommandAliases : PersistentSave(Skytils.modDir.resolve("commandaliases.json")) {
    val aliases = mutableMapOf<String, String>()
    var currentDispatcher: CommandDispatcher<ClientCommandSource>? = null

    fun onDispatcherUpdated(dispatcher: CommandDispatcher<ClientCommandSource>) {
        currentDispatcher = dispatcher
        injectAliasesIntoDispatcher(aliases.keys, dispatcher)
    }

    fun injectAliasesIntoDispatcher(aliasKeys: Set<String>, dispatcher: CommandDispatcher<ClientCommandSource>) {
        for (alias in aliasKeys) {
            val parts = alias.split(" ")
            var node = dispatcher.root.getChild(parts[0])
                ?: LiteralArgumentBuilder.literal<ClientCommandSource>(parts[0]).build()
                    .also { dispatcher.root.addChild(it) }
            for (part in parts.drop(1)) {
                node = node.getChild(part)
                    ?: LiteralArgumentBuilder.literal<ClientCommandSource>(part).build()
                        .also { node.addChild(it) }
            }
        }
    }

    fun removeAliasesFromDispatcher(removed: Set<String>, dispatcher: CommandDispatcher<ClientCommandSource>) {
        for (alias in removed) {
            val parts = alias.split(" ")
            val parent = if (parts.size == 1) dispatcher.root
                         else dispatcher.findNode(parts.dropLast(1)) ?: continue
            val accessor = parent as CommandNodeAccessor
            accessor.childrenMap.remove(parts.last())
            accessor.literalsMap.remove(parts.last())
        }
    }

    override fun read(reader: Reader) {
        aliases.clear()
        aliases.putAll(json.decodeFromString(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(aliases))
    }
}
