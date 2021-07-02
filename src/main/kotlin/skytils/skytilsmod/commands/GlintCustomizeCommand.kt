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
package skytils.skytilsmod.commands

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.SyntaxErrorException
import net.minecraft.command.WrongUsageException
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils

object GlintCustomizeCommand : CommandBase() {
    override fun getCommandName(): String {
        return "glintcustomize"
    }

    override fun getCommandAliases(): List<String> {
        return listOf("customizeglint")
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "glintcustomize <override/color>"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        return emptyList()
    }


    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        val player = sender as EntityPlayerSP
        val item = player.heldItem ?: throw WrongUsageException("You need to hold an item that you wish to customize!")
        val itemId = ItemUtil.getSkyBlockItemID(item) ?: throw WrongUsageException("That isn't a valid item!")
        if (args.isEmpty()) {
            throw WrongUsageException(getCommandUsage(sender))
        }
        val originalMessage = java.lang.String.join(" ", *args)
        when (args[0].lowercase()) {
            "override" -> {
                when {
                    originalMessage.contains("on") -> {
                        GlintCustomizer.overrides[itemId] = true
                        PersistentSave.markDirty<GlintCustomizer>()
                        sender.addChatMessage(ChatComponentText("§aForced an enchant glint for your item."))
                        return
                    }
                    originalMessage.contains("off") -> {
                        GlintCustomizer.overrides[itemId] = false
                        PersistentSave.markDirty<GlintCustomizer>()
                        sender.addChatMessage(ChatComponentText("§aForce disabled an enchant glint for your item."))
                        return
                    }
                    originalMessage.contains("clearall") -> {
                        GlintCustomizer.overrides.clear()
                        PersistentSave.markDirty<GlintCustomizer>()
                        sender.addChatMessage(ChatComponentText("§aRemoved all your glint overrides."))
                        return
                    }
                    originalMessage.contains("clear") -> {
                        GlintCustomizer.overrides.remove(itemId)
                        PersistentSave.markDirty<GlintCustomizer>()
                        sender.addChatMessage(ChatComponentText("§aCleared glint overrides for your item."))
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
                            sender.addChatMessage(ChatComponentText("§aForced an enchant glint color for your item."))
                        } catch (e: NumberFormatException) {
                            throw SyntaxErrorException("Unable to get a color from inputted string.")
                        }
                        return
                    }
                    originalMessage.contains("clearall") -> {
                        GlintCustomizer.glintColors.clear()
                        PersistentSave.markDirty<GlintCustomizer>()
                        sender.addChatMessage(ChatComponentText("§aRemoved all your custom glint colors."))
                        return
                    }
                    originalMessage.contains("clear") -> {
                        GlintCustomizer.glintColors.remove(itemId)
                        PersistentSave.markDirty<GlintCustomizer>()
                        sender.addChatMessage(ChatComponentText("§aCleared the custom glint color for your item."))
                        return
                    }
                    else -> {
                        throw WrongUsageException("glintcustomize color <set/clearall/clear>")
                    }
                }
            }
            else -> {
                throw WrongUsageException(getCommandUsage(sender))
            }
        }
    }
}