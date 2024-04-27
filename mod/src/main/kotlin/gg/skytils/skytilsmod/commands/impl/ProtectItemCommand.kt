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
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.commands.BaseCommand
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.protectitems.strategy.impl.FavoriteStrategy
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.WrongUsageException

object ProtectItemCommand : BaseCommand("protectitem") {
    override fun getCommandUsage(player: EntityPlayerSP): String = "/protectitem <clearall>"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        val subcommand = args.getOrNull(0)?.lowercase()
        if (subcommand == "clearall") {
            FavoriteStrategy.favoriteUUIDs.clear()
            FavoriteStrategy.favoriteItemIds.clear()
            PersistentSave.markDirty<FavoriteStrategy.FavoriteStrategySave>()
            UChat.chat("$successPrefix §aCleared all your protected items!")
            return
        }
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        val item = player.heldItem
            ?: throw WrongUsageException("You must hold an item to use this command")
        val extraAttributes = ItemUtil.getExtraAttributes(item)
            ?: throw WrongUsageException("This isn't a Skyblock Item? Where'd you get it from cheater...")
        if (extraAttributes.hasKey("uuid") && subcommand != "itemid") {
            val uuid = extraAttributes.getString("uuid")
            if (FavoriteStrategy.favoriteUUIDs.remove(uuid)) {
                PersistentSave.markDirty<FavoriteStrategy.FavoriteStrategySave>()
                UChat.chat("$successPrefix §cI will no longer protect your ${item.displayName}§a!")
            } else {
                FavoriteStrategy.favoriteUUIDs.add(uuid)
                PersistentSave.markDirty<FavoriteStrategy.FavoriteStrategySave>()
                UChat.chat("$successPrefix §aI will now protect your ${item.displayName}!")
            }
        } else {
            val itemId =
                ItemUtil.getSkyBlockItemID(item) ?: throw WrongUsageException("This item doesn't have a Skyblock ID.")
            if (FavoriteStrategy.favoriteItemIds.remove(itemId)) {
                PersistentSave.markDirty<FavoriteStrategy.FavoriteStrategySave>()
                UChat.chat("$successPrefix §cI will no longer protect all of your ${itemId}s!")
            } else {
                FavoriteStrategy.favoriteItemIds.add(itemId)
                PersistentSave.markDirty<FavoriteStrategy.FavoriteStrategySave>()
                UChat.chat("$successPrefix §aI will now protect all of your ${itemId}s!")
            }
        }
    }
}