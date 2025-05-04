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
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.GlintCustomizer
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.command.SyntaxErrorException
import net.minecraft.command.WrongUsageException
import net.minecraft.item.ItemStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands

@Commands
object GlintCustomizeCommand {
    private fun getCurrentItemId(): String {
        if (!Utils.inSkyblock) throw WrongUsageException("§cYou must be in Skyblock to use this command!")
        val item: ItemStack = mc.thePlayer?.heldItem ?: throw WrongUsageException("§cYou need to hold an item that you wish to customize!")
        return ItemUtil.getSkyBlockItemID(item) ?: throw WrongUsageException("§cThat isn't a valid SkyBlock item (missing SB ID)!")
    }

    @Command("glintcustomize|customizeglint override <state>")
    fun overrideSet(
        @Argument("state", description = "The state to set the override to (true/on or false/off)")
        enabled: Boolean
    ) {
        val itemId = getCurrentItemId()
        GlintCustomizer.getGlintItem(itemId).override = enabled
        PersistentSave.markDirty<GlintCustomizer>()

        val stateString = if (enabled) "'on'" else "'off'"
        UChat.chat("$successPrefix §aForced the enchant glint $stateString for your item.")
    }


    @Command("glintcustomize|customizeglint override clear")
    fun overrideClear() {
        val itemId = getCurrentItemId()
        if (GlintCustomizer.glintItems.containsKey(itemId)) {
            GlintCustomizer.getGlintItem(itemId).override = null
            PersistentSave.markDirty<GlintCustomizer>()
            UChat.chat("$successPrefix §aCleared glint override setting for your item.")
        } else {
            UChat.chat("$failPrefix §cNo glint override was set for this item.")
        }
    }

    @Command("glintcustomize|customizeglint override clearall")
    fun overrideClearAll() {
        GlintCustomizer.glintItems.values.forEach {
            it.override = null
        }
        PersistentSave.markDirty<GlintCustomizer>()
        UChat.chat("$successPrefix §aRemoved all your glint overrides.")
    }

    @Command("glintcustomize|customizeglint color set <color>")
    fun colorSet(
        @Argument("color", description = "The hex color code (e.g., #FF0000) or color name")
        colorString: String
    ) {
        val itemId = getCurrentItemId()
        try {
            val customColor = Utils.customColorFromString(colorString)
            GlintCustomizer.getGlintItem(itemId).color = customColor
            PersistentSave.markDirty<GlintCustomizer>()
            UChat.chat("$successPrefix §aSet the enchant glint color for your item to $colorString.")
        } catch (e: IllegalArgumentException) {
            throw SyntaxErrorException("$failPrefix §cUnable to get a color from '$colorString'. Use hex (e.g. #FF00AA) or color names.")
        }
    }

    @Command("glintcustomize|customizeglint color clear")
    fun colorClear() {
        val itemId = getCurrentItemId()
        if (GlintCustomizer.glintItems.containsKey(itemId)) {
            GlintCustomizer.getGlintItem(itemId).color = null
            PersistentSave.markDirty<GlintCustomizer>()
            UChat.chat("$successPrefix §aCleared the custom glint color for your item.")
        } else {
            UChat.chat("$failPrefix §cNo custom glint color was set for this item.")
        }
    }

    @Command("glintcustomize|customizeglint color clearall")
    fun colorClearAll() {
        GlintCustomizer.glintItems.values.forEach {
            it.color = null
        }
        PersistentSave.markDirty<GlintCustomizer>()
        UChat.chat("$successPrefix §aRemoved all your custom glint colors.")
    }
}