/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.commands.impl

import gg.essential.universal.UChat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.SyntaxErrorException
import net.minecraft.command.WrongUsageException
import sharttils.sharttilsmod.commands.BaseCommand
import sharttils.sharttilsmod.core.PersistentSave
import sharttils.sharttilsmod.features.impl.handlers.GlintCustomizer
import sharttils.sharttilsmod.utils.ItemUtil
import sharttils.sharttilsmod.utils.Utils

object GlintCustomizeCommand : BaseCommand("glintcustomize", listOf("customizeglint")) {
    override fun getCommandUsage(player: EntityPlayerSP) = "glintcustomize <override/color>"

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
                        GlintCustomizer.overrides[itemId] = true
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("§aForced an enchant glint for your item.")
                        return
                    }
                    originalMessage.contains("off") -> {
                        GlintCustomizer.overrides[itemId] = false
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("§aForce disabled an enchant glint for your item.")
                        return
                    }
                    originalMessage.contains("clearall") -> {
                        GlintCustomizer.overrides.clear()
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("§aRemoved all your glint overrides.")
                        return
                    }
                    originalMessage.contains("clear") -> {
                        GlintCustomizer.overrides.remove(itemId)
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("§aCleared glint overrides for your item.")
                        return
                    }
                    else -> {
                        throw WrongUsageException("glintcustomize override <on/off/clear/clearall>")
                    }
                }
            }
            "color" -> {
                when {
                    originalMessage.contains("set") -> {
                        if (args.size != 3) throw WrongUsageException("You must specify a valid hex color!")
                        try {
                            GlintCustomizer.glintColors[itemId] = Utils.customColorFromString(args[2])
                            PersistentSave.markDirty<GlintCustomizer>()
                            UChat.chat("§aForced an enchant glint color for your item.")
                        } catch (e: NumberFormatException) {
                            throw SyntaxErrorException("Unable to get a color from inputted string.")
                        }
                        return
                    }
                    originalMessage.contains("clearall") -> {
                        GlintCustomizer.glintColors.clear()
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("§aRemoved all your custom glint colors.")
                        return
                    }
                    originalMessage.contains("clear") -> {
                        GlintCustomizer.glintColors.remove(itemId)
                        PersistentSave.markDirty<GlintCustomizer>()
                        UChat.chat("§aCleared the custom glint color for your item.")
                        return
                    }
                    else -> {
                        throw WrongUsageException("glintcustomize color <set/clearall/clear>")
                    }
                }
            }
            else -> {
                throw WrongUsageException(getCommandUsage(player))
            }
        }
    }
}