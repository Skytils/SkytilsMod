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
package skytils.skytilsmod.mixins.hooks.entity

import net.minecraft.entity.item.EntityItem
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.common.MinecraftForge
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.AddChatMessageEvent
import skytils.skytilsmod.events.ItemTossEvent

fun onAddChatMessage(message: IChatComponent, ci: CallbackInfo) {
    try {
        if (MinecraftForge.EVENT_BUS.post(AddChatMessageEvent(message))) ci.cancel()
    } catch (e: Throwable) {
        mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at AddChatMessageEvent. Please report this on the Discord server."))
        e.printStackTrace()
    }
}

fun onDropItem(dropAll: Boolean, cir: CallbackInfoReturnable<EntityItem?>) {
    try {
        val stack = mc.thePlayer.inventory.getCurrentItem()
        if (stack != null && MinecraftForge.EVENT_BUS.post(ItemTossEvent(stack, dropAll))) cir.returnValue = null
    } catch (e: Throwable) {
        mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at PlayerDropItemEvent. Please report this on the Discord server."))
        e.printStackTrace()
    }
}