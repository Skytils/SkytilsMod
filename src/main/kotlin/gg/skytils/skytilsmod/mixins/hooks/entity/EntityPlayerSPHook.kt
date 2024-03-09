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
package gg.skytils.skytilsmod.mixins.hooks.entity

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.AddChatMessageEvent
import gg.skytils.skytilsmod.events.impl.ItemTossEvent
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.item.EntityItem
import net.minecraft.util.IChatComponent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

fun onAddChatMessage(message: IChatComponent, ci: CallbackInfo) {
    if (AddChatMessageEvent(message).postAndCatch()) ci.cancel()
}

fun onDropItem(dropAll: Boolean, cir: CallbackInfoReturnable<EntityItem?>) {
    val stack = mc.thePlayer.inventory.getCurrentItem()
    if (stack != null && ItemTossEvent(stack, dropAll).postAndCatch()) cir.returnValue = null
}

fun onKeybindCheck(keyBinding: KeyBinding): Boolean {
    return keyBinding === mc.gameSettings.keyBindSprint && Utils.inSkyblock && Skytils.config.alwaysSprint
}
