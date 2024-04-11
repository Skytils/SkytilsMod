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

package gg.skytils.skytilsmod.features.impl.handlers

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.SendChatMessageEvent
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorCommandHandler
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorLoadController
import gg.skytils.skytilsmod.utils.ObservableAddEvent
import gg.skytils.skytilsmod.utils.ObservableClearEvent
import gg.skytils.skytilsmod.utils.ObservableRemoveEvent
import gg.skytils.skytilsmod.utils.ObservableSet
import net.minecraft.command.ICommand
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CommandManager {
    val cch by lazy {
        ClientCommandHandler.instance as AccessorCommandHandler
    }

    val loadController by lazy {
        Loader.instance().modController as AccessorLoadController
    }

    val aliasMap = mutableMapOf<ICommand, String>()

    fun setup(listeningSet: ObservableSet<ICommand>) {
        listeningSet.addObserver { _, arg ->
            when (arg) {
                is ObservableAddEvent<*> -> {
                    registerCommandHelper(arg.element as ICommand)
                }
                is ObservableRemoveEvent<*> -> {
                    cch.commandMap.remove(aliasMap.remove(arg.element))
                }
                is ObservableClearEvent<*> -> {
                    aliasMap.entries.removeAll {
                        cch.commandMap.remove(it.value)
                        true
                    }
                }
            }
        }

        listeningSet.forEach {
            registerCommandHelper(it)
        }
    }

    fun registerCommandHelper(command: ICommand) {
        val clazzName = command.javaClass.name
        val pkg = clazzName.substringBeforeLast('.')
        val owners = loadController.packageOwners[pkg].distinct()
        if (owners.size != 1) {
            println("WARNING! Command $clazzName has ${owners.size}; owners: $owners")
        }

        val owner = owners.firstOrNull()

        val prefix = owner?.modId ?: owner?.name ?: "unknown"

        val helper = "${prefix}:${command.commandName}"
        cch.commandMap[helper] = command

        aliasMap[command] = helper
    }

    @SubscribeEvent
    fun onSendChat(event: SendChatMessageEvent) {
        if (event.message.startsWith("/server:")) {
            event.isCanceled = true
            UChat.say('/' + event.message.substringAfter("/server:"))
            mc.ingameGUI.chatGUI.addToSentMessages(event.message)
        }
    }
}