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
package sharttils.sharttilsmod.mixins.hooks.item

import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import sharttils.sharttilsmod.features.impl.handlers.ArmorColor
import sharttils.sharttilsmod.utils.ItemUtil.getExtraAttributes
import sharttils.sharttilsmod.utils.Utils

fun replaceArmorColor(stack: ItemStack, cir: CallbackInfoReturnable<Int>) {
    if (!Utils.inSkyblock) return
    val extraAttributes = getExtraAttributes(stack)
    if (extraAttributes != null && extraAttributes.hasKey("uuid")) {
        val uuid = extraAttributes.getString("uuid")
        if (ArmorColor.armorColors.containsKey(uuid)) {
            cir.returnValue = ArmorColor.armorColors[uuid]!!.toInt()
        }
    }
}

fun hasCustomArmorColor(stack: ItemStack, cir: CallbackInfoReturnable<Boolean>) {
    if (!Utils.inSkyblock) return
    val extraAttributes = getExtraAttributes(stack)
    if (extraAttributes != null && extraAttributes.hasKey("uuid")) {
        val uuid = extraAttributes.getString("uuid")
        if (ArmorColor.armorColors.containsKey(uuid)) {
            cir.returnValue = true
        }
    }
}
