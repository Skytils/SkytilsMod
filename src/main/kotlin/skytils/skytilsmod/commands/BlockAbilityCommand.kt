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
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.BlockAbility
import skytils.skytilsmod.utils.ItemUtil

object BlockAbilityCommand : CommandBase() {
    override fun getCommandName(): String {
        return "blockability"
    }

    override fun getCommandAliases(): List<String> {
        return listOf("disableability")
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/blockability [clearall]"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        return emptyList()
    }


    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        val player = sender as EntityPlayerSP
        if (args.isEmpty()) {
            val item = player.heldItem
            if (item == null) {
                sender.addChatMessage(ChatComponentText("§cYou need to hold the item with the ability that you want to block!"))
                return
            }
            val itemId = ItemUtil.getSkyBlockItemID(item)
            if (itemId == null || !ItemUtil.hasRightClickAbility(item)) {
                sender.addChatMessage(ChatComponentText("§cThat isn't a valid item!"))
                return
            }
            if (BlockAbility.blockedItems.contains(itemId)) {
                BlockAbility.blockedItems.remove(itemId)
                PersistentSave.markDirty<BlockAbility>()
                sender.addChatMessage(ChatComponentText("§aRemoved the block on $itemId!"))
            } else {
                BlockAbility.blockedItems.add(itemId)
                PersistentSave.markDirty<BlockAbility>()
                sender.addChatMessage(ChatComponentText("§aYou are now blocking abilities for $itemId!"))
            }
            return
        }
        val subcommand = args[0].lowercase()
        if (subcommand == "clearall") {
            BlockAbility.blockedItems.clear()
            PersistentSave.markDirty<BlockAbility>()
            sender.addChatMessage(ChatComponentText("§aCleared all your custom ability blocks!"))
        } else {
            player.addChatMessage(ChatComponentText(getCommandUsage(sender)))
        }
    }
}