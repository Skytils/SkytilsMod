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
package skytils.skytilsmod.commands.impl

import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DisplayName
import gg.essential.api.commands.SubCommand
import gg.essential.universal.wrappers.message.UTextComponent
import net.minecraft.command.SyntaxErrorException
import net.minecraft.command.WrongUsageException
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils

object GlintCustomizeCommand : Command("glintcustomize") {
    override val commandAliases: Set<Alias> = setOf(Alias("customizeglint"))

    @SubCommand("override")
    fun override(@DisplayName("on/off/clear/clearall") type: String) {
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        val item =
            mc.thePlayer.heldItem ?: throw WrongUsageException("You need to hold an item that you wish to customize!")
        val itemId = ItemUtil.getSkyBlockItemID(item) ?: throw WrongUsageException("That isn't a valid item!")
        when (type) {
            "on" -> {
                GlintCustomizer.overrides[itemId] = true
                PersistentSave.markDirty<GlintCustomizer>()
                EssentialAPI.getMinecraftUtil().sendMessage(UTextComponent("§aForced an enchant glint for your item."))
                return
            }
            "off" -> {
                GlintCustomizer.overrides[itemId] = false
                PersistentSave.markDirty<GlintCustomizer>()
                EssentialAPI.getMinecraftUtil()
                    .sendMessage(UTextComponent("§aForce disabled an enchant glint for your item."))
                return
            }
            "clearall" -> {
                GlintCustomizer.overrides.clear()
                PersistentSave.markDirty<GlintCustomizer>()
                EssentialAPI.getMinecraftUtil().sendMessage(UTextComponent("§aRemoved all your glint overrides."))
                return
            }
            "clear" -> {
                GlintCustomizer.overrides.remove(itemId)
                PersistentSave.markDirty<GlintCustomizer>()
                EssentialAPI.getMinecraftUtil().sendMessage(UTextComponent("§aCleared glint overrides for your item."))
                return
            }
            else -> {
                throw WrongUsageException("glintcustomize override <on/off/clear/clearall>")
            }
        }
    }

    @SubCommand("color")
    fun color(@DisplayName("set/clearall/clear") type: String, @DisplayName("hex color") hex: String?) {
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        val item =
            mc.thePlayer.heldItem ?: throw WrongUsageException("You need to hold an item that you wish to customize!")
        val itemId = ItemUtil.getSkyBlockItemID(item) ?: throw WrongUsageException("That isn't a valid item!")
        when (type) {
            "set" -> {
                if (hex == null) throw WrongUsageException("You must specify a valid hex color!")
                try {
                    GlintCustomizer.glintColors[itemId] = Utils.customColorFromString(hex)
                    PersistentSave.markDirty<GlintCustomizer>()
                    EssentialAPI.getMinecraftUtil()
                        .sendMessage(UTextComponent("§aForced an enchant glint color for your item."))
                } catch (e: NumberFormatException) {
                    throw SyntaxErrorException("Unable to get a color from inputted string.")
                }
                return
            }
            "clearall" -> {
                GlintCustomizer.glintColors.clear()
                PersistentSave.markDirty<GlintCustomizer>()
                EssentialAPI.getMinecraftUtil().sendMessage(UTextComponent("§aRemoved all your custom glint colors."))
                return
            }
            "clear" -> {
                GlintCustomizer.glintColors.remove(itemId)
                PersistentSave.markDirty<GlintCustomizer>()
                EssentialAPI.getMinecraftUtil()
                    .sendMessage(UTextComponent("§aCleared the custom glint color for your item."))
                return
            }
            else -> {
                throw WrongUsageException("glintcustomize color <set/clearall/clear>")
            }
        }
    }
}