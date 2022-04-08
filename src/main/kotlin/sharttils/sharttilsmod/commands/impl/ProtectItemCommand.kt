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
import net.minecraft.command.WrongUsageException
import sharttils.sharttilsmod.commands.BaseCommand
import sharttils.sharttilsmod.core.PersistentSave
import sharttils.sharttilsmod.features.impl.protectitems.strategy.impl.FavoriteStrategy
import sharttils.sharttilsmod.utils.ItemUtil
import sharttils.sharttilsmod.utils.Utils

object ProtectItemCommand : BaseCommand("protectitem") {
    override fun getCommandUsage(player: EntityPlayerSP): String = "/protectitem <clearall>"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        val subcommand = args.getOrNull(0)?.lowercase()
        if (subcommand == "clearall") {
            FavoriteStrategy.favoriteItems.clear()
            PersistentSave.markDirty<FavoriteStrategy.FavoriteStrategySave>()
            UChat.chat("§aCleared all your protected items!")
            return
        }
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        val item = player.heldItem
            ?: throw WrongUsageException("You must hold an item to use this command")
        val extraAttributes = ItemUtil.getExtraAttributes(item)
        if (extraAttributes == null || !extraAttributes.hasKey("uuid")) throw WrongUsageException("This item does not have a UUID!")
        val uuid = extraAttributes.getString("uuid")
        if (FavoriteStrategy.favoriteItems.remove(uuid)) {
            PersistentSave.markDirty<FavoriteStrategy.FavoriteStrategySave>()
            UChat.chat("§aI will no longer protect your ${item.displayName}§a!")
        } else {
            FavoriteStrategy.favoriteItems.add(uuid)
            PersistentSave.markDirty<FavoriteStrategy.FavoriteStrategySave>()
            UChat.chat("§aI will now protect your ${item.displayName}!")
        }
    }
}