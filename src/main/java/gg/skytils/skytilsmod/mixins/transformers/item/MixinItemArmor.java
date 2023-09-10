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

package gg.skytils.skytilsmod.mixins.transformers.item;

import gg.skytils.skytilsmod.mixins.hooks.item.ItemArmorHookKt;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemArmor.class)
public abstract class MixinItemArmor extends Item {

    @Inject(method = "getColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getTagCompound()Lnet/minecraft/nbt/NBTTagCompound;"), cancellable = true)
    private void replaceArmorColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ItemArmorHookKt.replaceArmorColor(stack, cir);
    }

    @Inject(method = "getColorFromItemStack", at = @At("HEAD"), cancellable = true)
    private void replaceStackArmorColor(ItemStack stack, int renderPass, CallbackInfoReturnable<Integer> cir) {
        if (renderPass > 0) return;
        ItemArmorHookKt.replaceArmorColor(stack, cir);
    }

    @Inject(method = "hasColor", at = @At("HEAD"), cancellable = true)
    private void hasCustomArmorColor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ItemArmorHookKt.hasCustomArmorColor(stack, cir);
    }
}
