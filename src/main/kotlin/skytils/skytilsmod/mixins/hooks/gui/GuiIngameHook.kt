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
package skytils.skytilsmod.mixins.hooks.gui

import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.SetActionBarEvent
import skytils.skytilsmod.utils.RenderUtil.renderRarity
import skytils.skytilsmod.utils.Utils


var recordPlaying: String? = null
var recordPlayingUpFor: Int = 0
var recordIsPlaying: Boolean = false

fun onSetActionBar(message: String, isPlaying: Boolean, ci: CallbackInfo): Boolean {
    try {
        val event = SetActionBarEvent(message, isPlaying)
        if (MinecraftForge.EVENT_BUS.post(event)) {
            ci.cancel()
            return false
        }
        if (message != event.message || isPlaying != event.isPlaying) {
            ci.cancel()
            recordPlaying = event.message
            recordPlayingUpFor = 60
            recordIsPlaying = event.isPlaying
            return true
        }
    } catch (e: Throwable) {
        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at SetActionBarEvent. Please report this on the Discord server."))
        e.printStackTrace()
    }
    return false
}

fun renderRarityOnHotbar(
    index: Int,
    xPos: Int,
    yPos: Int,
    partialTicks: Float,
    player: EntityPlayer,
    ci: CallbackInfo
) {
    if (Utils.inSkyblock && Skytils.config.showItemRarity) {
        renderRarity(player.inventory.mainInventory[index], xPos, yPos)
    }
}