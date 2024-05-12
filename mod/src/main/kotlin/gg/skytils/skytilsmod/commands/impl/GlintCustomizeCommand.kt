/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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
package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.Skytils.successPrefix
import gg.skytils.skytilsmod.commands.BaseCommand
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.GlintCustomizer
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.SyntaxErrorException
import net.minecraft.command.WrongUsageException

object GlintCustomizeCommand : BaseCommand("glintcustomize", listOf("customizeglint")) {
    override fun getCommandUsage(player: EntityPlayerSP) = "/glintcustomize <override/color>"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        val item = player.heldItem ?: throw WrongUsageException("You need to hold an item that you wish to customize!")
        val itemId = ItemUtil.getSkyBlockItemID(item) ?: throw WrongUsageException("That isn't a valid item!")
        if (args.isEmpty()) {
            throw WrongUsageException(getCommandUsage(player))
        }
        val originalMessage = args.joinToString(" ")
        when (args[0].lowercase()) {
            "override" -> {
                when {
                    originalMessage.contains("on") -> {
                        GlintCustomizer.getGlintItem(itemId).override = true
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("$successPrefix §aForced an enchant glint for your item.")
                        return
                    }
                    originalMessage.contains("off") -> {
                        GlintCustomizer.getGlintItem(itemId).override = false
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("$successPrefix §aForce disabled an enchant glint for your item.")
                        return
                    }
                    originalMessage.contains("clearall") -> {
                        GlintCustomizer.glintItems.values.forEach {
                            it.override = null
                        }
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("$successPrefix §aRemoved all your glint overrides.")
                        return
                    }
                    originalMessage.contains("clear") -> {
                        GlintCustomizer.getGlintItem(itemId).override = null
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("$successPrefix §aCleared glint overrides for your item.")
                        return
                    }
                    else -> {
                        throw WrongUsageException("/glintcustomize override <on/off/clear/clearall>")
                    }
                }
            }
            "color" -> {
                when {
                    originalMessage.contains("set") -> {
                        if (args.size != 3) throw WrongUsageException("You must specify a valid hex color!")
                        try {
                            GlintCustomizer.getGlintItem(itemId).color = Utils.customColorFromString(args[2])
                            PersistentSave.markDirty<GlintCustomizer>()
                            UChat.chat("$successPrefix §aForced an enchant glint color for your item.")
                        } catch (e: NumberFormatException) {
                            throw SyntaxErrorException("$failPrefix Unable to get a color from inputted string.")
                        }
                        return
                    }
                    originalMessage.contains("clearall") -> {
                        GlintCustomizer.glintItems.values.forEach {
                            it.color = null
                        }
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("$successPrefix §aRemoved all your custom glint colors.")
                        return
                    }
                    originalMessage.contains("clear") -> {
                        GlintCustomizer.getGlintItem(itemId).color = null
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("$successPrefix §aCleared the custom glint color for your item.")
                        return
                    }
                    else -> {
                        throw WrongUsageException("/glintcustomize color <set/clearall/clear>")
                    }
                }
            }
            else -> {
                throw WrongUsageException(getCommandUsage(player))
            }
        }
    }
}